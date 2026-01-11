package com.example.appeditorsimple.nui;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Adaptador de reconocimiento de voz real usando Vosk API.
 * Captura audio del micrófono y lo procesa en tiempo real.
 */
public class VoskSpeechAdapter {

    private final NuiController controller;
    private final SpeechInputAdapter commandParser;

    private Model model;
    private Recognizer recognizer;
    private TargetDataLine microphone;
    private Thread recordingThread;

    private final AtomicBoolean isRecording = new AtomicBoolean(false);
    private final AtomicBoolean isModelLoaded = new AtomicBoolean(false);

    private static final float SAMPLE_RATE = 16000f;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHANNELS = 1;

    private VoskStatusListener statusListener;

    /**
     * Interface para notificar el estado del reconocimiento
     */
    public interface VoskStatusListener {
        void onStatusChange(String status, boolean isRecording);

        void onTextRecognized(String text, boolean isFinal);

        void onError(String error);
    }

    /**
     * Crea un nuevo adaptador de voz Vosk.
     * 
     * @param controller El NuiController para enviar comandos
     */
    public VoskSpeechAdapter(NuiController controller) {
        this.controller = controller;
        this.commandParser = new SpeechInputAdapter(controller);
    }

    /**
     * Establece el listener de estado
     */
    public void setStatusListener(VoskStatusListener listener) {
        this.statusListener = listener;
    }

    /**
     * Carga el modelo de voz desde la ruta especificada.
     * 
     * @param modelPath Ruta al directorio del modelo Vosk
     * @return true si se cargó correctamente
     */
    public boolean loadModel(String modelPath) {
        try {
            Path path = Path.of(modelPath);
            if (!Files.exists(path)) {
                notifyError("Modelo no encontrado en: " + modelPath);
                return false;
            }

            notifyStatus("Cargando modelo de voz...", false);
            model = new Model(modelPath);
            recognizer = new Recognizer(model, SAMPLE_RATE);
            isModelLoaded.set(true);
            notifyStatus("Modelo cargado - Listo para grabar", false);
            return true;

        } catch (IOException e) {
            notifyError("Error cargando modelo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Intenta cargar el modelo desde varias ubicaciones predeterminadas.
     */
    public boolean loadModelFromDefaultPaths() {
        String userHome = System.getProperty("user.home");
        String userDir = System.getProperty("user.dir");

        String[] defaultPaths = {
                // En la raíz del proyecto
                "vosk-model-es",
                "vosk-model-small-es-0.42",
                // En src/main/resources
                "src/main/resources/vosk-model-es",
                "src/main/resources/vosk-model-small-es-0.42",
                // En la carpeta del usuario
                userHome + "/vosk-model-es",
                userHome + "/vosk-model-small-es-0.42",
                userHome + "\\vosk-model-es",
                userHome + "\\vosk-model-small-es-0.42",
                // En Descargas (común para usuarios)
                userHome + "/Downloads/vosk-model-small-es-0.42",
                userHome + "\\Downloads\\vosk-model-small-es-0.42",
                userHome + "/Descargas/vosk-model-small-es-0.42",
                userHome + "\\Descargas\\vosk-model-small-es-0.42",
                // Directorio actual explícito
                userDir + "/vosk-model-small-es-0.42",
                userDir + "\\vosk-model-small-es-0.42"
        };

        for (String path : defaultPaths) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                // Verificar que parece un modelo válido (tiene subcarpetas esperadas)
                File confDir = new File(dir, "conf");
                if (confDir.exists()) {
                    System.out.println("[Vosk] Encontrado modelo en: " + path);
                    return loadModel(dir.getAbsolutePath());
                }
            }
        }

        notifyError("Modelo no encontrado. Descarga vosk-model-small-es-0.42 de alphacephei.com/vosk/models");
        return false;
    }

    /**
     * Inicia la grabación y reconocimiento de voz.
     */
    public void startRecording() {
        if (!isModelLoaded.get()) {
            notifyError("Primero carga el modelo de voz");
            return;
        }

        if (isRecording.get()) {
            return;
        }

        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                notifyError("Micrófono no soportado");
                return;
            }

            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            microphone.start();

            isRecording.set(true);
            notifyStatus("GRABANDO - Habla ahora...", true);

            // Hilo de grabación
            recordingThread = new Thread(this::recordAndRecognize);
            recordingThread.setDaemon(true);
            recordingThread.start();

        } catch (LineUnavailableException e) {
            notifyError("No se pudo acceder al micrófono: " + e.getMessage());
        }
    }

    /**
     * Detiene la grabación.
     */
    public void stopRecording() {
        isRecording.set(false);

        if (microphone != null) {
            microphone.stop();
            microphone.close();
        }

        notifyStatus("Grabacion detenida", false);
    }

    /**
     * Bucle principal de grabación y reconocimiento.
     */
    private void recordAndRecognize() {
        byte[] buffer = new byte[4096];

        while (isRecording.get()) {
            int bytesRead = microphone.read(buffer, 0, buffer.length);

            if (bytesRead > 0 && recognizer != null) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    // Resultado final
                    String result = recognizer.getResult();
                    String text = extractText(result);

                    if (!text.isEmpty()) {
                        notifyTextRecognized(text, true);
                        processRecognizedText(text);
                    }
                } else {
                    // Resultado parcial
                    String partial = recognizer.getPartialResult();
                    String text = extractPartialText(partial);

                    if (!text.isEmpty()) {
                        notifyTextRecognized(text, false);
                    }
                }
            }
        }
    }

    /**
     * Procesa el texto reconocido para detectar comandos.
     */
    private void processRecognizedText(String text) {
        System.out.println("[Vosk] Reconocido: " + text);

        // Intentar parsear como comando
        boolean isCommand = commandParser.processInput(text);

        // Si no es un comando, enviarlo como dictado
        if (!isCommand && !text.trim().isEmpty()) {
            controller.dispatch(NuiCommand.DICTAR_TEXTO, text);
        }
    }

    /**
     * Extrae el texto del JSON de resultado de Vosk.
     */
    private String extractText(String json) {
        // Formato: {"text" : "palabra palabra"}
        if (json.contains("\"text\"")) {
            int start = json.indexOf("\"text\"") + 9;
            int end = json.lastIndexOf("\"");
            if (start < end) {
                return json.substring(start, end).trim();
            }
        }
        return "";
    }

    /**
     * Extrae el texto parcial del JSON.
     */
    private String extractPartialText(String json) {
        // Formato: {"partial" : "palabra"}
        if (json.contains("\"partial\"")) {
            int start = json.indexOf("\"partial\"") + 12;
            int end = json.lastIndexOf("\"");
            if (start < end) {
                return json.substring(start, end).trim();
            }
        }
        return "";
    }

    /**
     * @return true si está grabando actualmente
     */
    public boolean isRecording() {
        return isRecording.get();
    }

    /**
     * @return true si el modelo está cargado
     */
    public boolean isModelLoaded() {
        return isModelLoaded.get();
    }

    /**
     * Libera los recursos.
     */
    public void dispose() {
        stopRecording();
        if (recognizer != null) {
            recognizer.close();
        }
        if (model != null) {
            model.close();
        }
    }

    // Métodos de notificación
    private void notifyStatus(String status, boolean recording) {
        System.out.println("[Vosk Status] " + status);
        if (statusListener != null) {
            statusListener.onStatusChange(status, recording);
        }
    }

    private void notifyTextRecognized(String text, boolean isFinal) {
        if (statusListener != null) {
            statusListener.onTextRecognized(text, isFinal);
        }
    }

    private void notifyError(String error) {
        System.err.println("[Vosk Error] " + error);
        if (statusListener != null) {
            statusListener.onError(error);
        }
    }
}
