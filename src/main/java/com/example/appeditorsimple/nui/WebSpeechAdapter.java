package com.example.appeditorsimple.nui;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 * Adaptador de reconocimiento de voz usando Web Speech API.
 * Utiliza un WebView oculto para acceder al reconocimiento de voz del
 * navegador.
 * Requiere conexión a internet pero NO requiere descargar ningún modelo.
 */
public class WebSpeechAdapter {

    private final NuiController controller;
    private final SpeechInputAdapter commandParser;
    private WebView webView;
    private WebEngine webEngine;
    private boolean isListening = false;
    private SpeechCallback callback;

    /**
     * Interface para callbacks de voz
     */
    public interface SpeechCallback {
        void onResult(String text, boolean isFinal);

        void onStateChange(String state);

        void onError(String error);
    }

    /**
     * Bridge de JavaScript a Java
     */
    public class JavaBridge {
        public void onSpeechResult(String text, boolean isFinal) {
            Platform.runLater(() -> {
                System.out.println("[WebSpeech] Resultado: " + text + " (final: " + isFinal + ")");
                if (callback != null) {
                    callback.onResult(text, isFinal);
                }
                if (isFinal && !text.trim().isEmpty()) {
                    // Intentar como comando, si no, dictar
                    boolean isCommand = commandParser.processInput(text);
                    if (!isCommand) {
                        controller.dispatch(NuiCommand.DICTAR_TEXTO, text);
                    }
                }
            });
        }

        public void onStateChange(String state) {
            Platform.runLater(() -> {
                System.out.println("[WebSpeech] Estado: " + state);
                isListening = "listening".equals(state);
                if (callback != null) {
                    callback.onStateChange(state);
                }
            });
        }

        public void onError(String error) {
            Platform.runLater(() -> {
                System.err.println("[WebSpeech] Error: " + error);
                isListening = false;
                if (callback != null) {
                    callback.onError(error);
                }
            });
        }
    }

    public WebSpeechAdapter(NuiController controller) {
        this.controller = controller;
        this.commandParser = new SpeechInputAdapter(controller);
    }

    /**
     * Inicializa el WebView con el código de Speech Recognition
     */
    public WebView initialize() {
        webView = new WebView();
        webView.setPrefSize(0, 0);
        webView.setVisible(false);

        webEngine = webView.getEngine();

        String html = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body>
                <script>
                    var recognition = null;
                    var isListening = false;

                    function initSpeech() {
                        if (!('webkitSpeechRecognition' in window) && !('SpeechRecognition' in window)) {
                            javaBridge.onError('Tu navegador no soporta reconocimiento de voz');
                            return false;
                        }

                        var SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
                        recognition = new SpeechRecognition();
                        recognition.continuous = true;
                        recognition.interimResults = true;
                        recognition.lang = 'es-ES';

                        recognition.onstart = function() {
                            isListening = true;
                            javaBridge.onStateChange('listening');
                        };

                        recognition.onend = function() {
                            isListening = false;
                            javaBridge.onStateChange('stopped');
                        };

                        recognition.onresult = function(event) {
                            var result = event.results[event.results.length - 1];
                            var text = result[0].transcript;
                            var isFinal = result.isFinal;
                            javaBridge.onSpeechResult(text, isFinal);
                        };

                        recognition.onerror = function(event) {
                            javaBridge.onError(event.error);
                        };

                        return true;
                    }

                    function startListening() {
                        if (!recognition) {
                            if (!initSpeech()) return;
                        }
                        if (!isListening) {
                            recognition.start();
                        }
                    }

                    function stopListening() {
                        if (recognition && isListening) {
                            recognition.stop();
                        }
                    }
                </script>
                </body>
                </html>
                """;

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaBridge", new JavaBridge());
                System.out.println("[WebSpeech] Inicializado correctamente");
            }
        });

        webEngine.loadContent(html);
        return webView;
    }

    /**
     * Inicia el reconocimiento de voz
     */
    public void startListening() {
        if (webEngine != null) {
            webEngine.executeScript("startListening()");
        }
    }

    /**
     * Detiene el reconocimiento de voz
     */
    public void stopListening() {
        if (webEngine != null) {
            webEngine.executeScript("stopListening()");
        }
    }

    /**
     * @return true si está escuchando
     */
    public boolean isListening() {
        return isListening;
    }

    /**
     * Establece el callback de voz
     */
    public void setCallback(SpeechCallback callback) {
        this.callback = callback;
    }
}
