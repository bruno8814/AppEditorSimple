package com.example.appeditorsimple;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import com.example.appeditorsimple.nui.NuiCommand;
import com.example.appeditorsimple.nui.NuiController;
import com.example.appeditorsimple.nui.NuiListener;
import com.example.appeditorsimple.nui.SpeechInputAdapter;
import com.example.appeditorsimple.nui.VoskSpeechAdapter;

import java.io.*;
import java.util.Optional;
import java.util.Stack;

public class HelloController implements NuiListener, VoskSpeechAdapter.VoskStatusListener {

    // ========== CAMPOS UI PRINCIPALES ==========
    @FXML
    private VBox mainContainer;

    @FXML
    private TextField campoBusqueda;

    @FXML
    private TextField campoBusqueda2;

    @FXML
    private TextArea areaTexto;

    @FXML
    private Label contador;

    @FXML
    private Button undo;

    @FXML
    private Button redo;

    @FXML
    private Button bold;

    @FXML
    private Button italic;

    @FXML
    private ColorPicker colorpicker;

    @FXML
    private Button btnExportar;

    private ProgressLabel miProgressLabel;

    private Stack<String> deshacer = new Stack<>();
    private Stack<String> rehacer = new Stack<>();

    private boolean undoRedoEnabled = false;

    private boolean estaActivoNegrita = false;
    private boolean estaActivoItalica = false;
    int ultimaPosicionBusqueda = 0;
    private FileChooser fileChooser;
    private Stage stage;

    // ========== CAMPOS NUI ==========
    @FXML
    private TextField campoVozSimulada;

    @FXML
    private Label lblEstadoNui;

    @FXML
    private Label lblTextoReconocido;

    @FXML
    private Label lblModeloStatus;

    @FXML
    private Button btnGrabar;

    @FXML
    private Button btnCargarModelo;

    private NuiController nuiController;
    private SpeechInputAdapter speechAdapter;
    private VoskSpeechAdapter voskAdapter;

    @FXML
    public void initialize() {

        campoBusqueda.setPromptText("Buscar...");
        campoBusqueda2.setPromptText("Reemplazar...");
        areaTexto.setPromptText(" Escribe tu texto aquí...");

        miProgressLabel = new ProgressLabel();
        miProgressLabel.setPrefWidth(300);

        areaTexto.textProperty().addListener((observable, oldValue, newValue) -> {

            // Si el cambio fue causado por nuestro propio código de undo/redo, lo
            // ignoramos.
            if (undoRedoEnabled) {
                return;
            }

            // si hay cambio por parte del usuario, guardamos el cambio anterior.
            deshacer.push(oldValue);

            rehacer.clear();

            contadorLabel(newValue);
            actualizarEstadoUndoRedo();

        });

        contadorLabel("");
        actualizarEstadoUndoRedo();

        // ========== INICIALIZACIÓN NUI ==========
        initNui();
    }

    /**
     * Inicializa la capa NUI (Natural User Interface)
     */
    private void initNui() {
        nuiController = new NuiController();
        nuiController.addListener(this);
        speechAdapter = new SpeechInputAdapter(nuiController);

        // Inicializar Vosk para reconocimiento de voz real
        voskAdapter = new VoskSpeechAdapter(nuiController);
        voskAdapter.setStatusListener(this);

        if (campoVozSimulada != null) {
            campoVozSimulada.setPromptText("Modo texto: escribe comando y pulsa Enter...");
        }
        if (lblEstadoNui != null) {
            lblEstadoNui.setText("Listo para grabar");
        }
        if (lblModeloStatus != null) {
            lblModeloStatus.setText("Modelo no cargado");
            lblModeloStatus.setStyle("-fx-text-fill: #f59e0b;");
        }

        System.out.println("[NUI] Sistema de voz inicializado");
    }

    // ========== MÉTODOS DE GRABACIÓN VOSK ==========

    /**
     * Alterna entre iniciar y detener la grabación de voz
     */
    @FXML
    private void onToggleRecordingAction(ActionEvent event) {
        if (!voskAdapter.isModelLoaded()) {
            actualizarEstadoNui("⚠️ Primero carga el modelo de voz", false);
            return;
        }

        if (voskAdapter.isRecording()) {
            voskAdapter.stopRecording();
            btnGrabar.setText("🎤 Iniciar Grabación");
            btnGrabar.getStyleClass().remove("btn-record-active");
        } else {
            voskAdapter.startRecording();
            btnGrabar.setText("⏹ Detener Grabación");
            btnGrabar.getStyleClass().add("btn-record-active");
        }
    }

    /**
     * Carga el modelo de voz Vosk
     */
    @FXML
    private void onCargarModeloAction(ActionEvent event) {
        // Intentar cargar desde rutas por defecto
        boolean loaded = voskAdapter.loadModelFromDefaultPaths();

        if (!loaded) {
            // Mostrar diálogo para seleccionar directorio del modelo
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Seleccionar carpeta del modelo Vosk");
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));

            Window window = btnCargarModelo.getScene().getWindow();
            File dir = chooser.showDialog(window);

            if (dir != null) {
                voskAdapter.loadModel(dir.getAbsolutePath());
            }
        }
    }

    // ========== VoskStatusListener Implementation ==========

    @Override
    public void onStatusChange(String status, boolean isRecording) {
        Platform.runLater(() -> {
            if (lblEstadoNui != null) {
                lblEstadoNui.setText(status);
                if (isRecording) {
                    lblEstadoNui.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                } else {
                    lblEstadoNui.setStyle("-fx-text-fill: #22c55e;");
                }
            }
            if (lblModeloStatus != null && status.contains("Modelo cargado")) {
                lblModeloStatus.setText("✅ Modelo cargado");
                lblModeloStatus.setStyle("-fx-text-fill: #22c55e;");
            }
        });
    }

    @Override
    public void onTextRecognized(String text, boolean isFinal) {
        Platform.runLater(() -> {
            if (lblTextoReconocido != null) {
                String prefix = isFinal ? "✓ " : "... ";
                lblTextoReconocido.setText(prefix + text);
            }
        });
    }

    @Override
    public void onError(String error) {
        Platform.runLater(() -> {
            actualizarEstadoNui("❌ " + error, false);
        });
    }

    public void contadorLabel(String texto) {
        int caracteres = texto.length();

        int numPalabras = 0;
        // Usamos trim() para quitar espacios al principio y al final.
        if (!texto.trim().isEmpty()) {
            // split("\\s+") divide el texto por uno o más espacios, tabuladores o saltos de
            // línea.
            String[] palabras = texto.trim().split("\\s+");
            numPalabras = palabras.length;
        }

        int espacios = 0;
        for (int i = 0; i < caracteres; i++) {
            if (texto.charAt(i) == ' ') {
                espacios++;
            }
        }

        String textoContador = String.format(
                "Caracteres: %d   |   Palabras: %d   |   Espacios: %d",
                caracteres,
                numPalabras,
                espacios);

        contador.setText(textoContador);

    }

    @FXML
    public void onUndoAction() {

        if (deshacer.isEmpty()) {
            return;
        }

        undoRedoEnabled = true;

        String textoAnterior = deshacer.pop();

        rehacer.push(areaTexto.getText());

        areaTexto.setText(textoAnterior);

        undoRedoEnabled = false;

        actualizarEstadoUndoRedo();
        contadorLabel(areaTexto.getText());

    }

    @FXML
    public void onRedoAction() {
        if (rehacer.isEmpty()) {
            return;
        }
        undoRedoEnabled = true;
        String textoAnterior = rehacer.pop();
        deshacer.push(areaTexto.getText());
        areaTexto.setText(textoAnterior);
        undoRedoEnabled = false;
        actualizarEstadoUndoRedo();
        contadorLabel(areaTexto.getText());
    }

    @FXML
    private void onBoldAction() {
        estaActivoNegrita = !estaActivoNegrita;

        if (estaActivoNegrita) {
            areaTexto.setStyle("-fx-font-weight: bold;");
            bold.getStyleClass().add("button-active"); // Feedback visual RA4
        } else {
            areaTexto.setStyle("-fx-font-weight: normal;");
            bold.getStyleClass().remove("button-active");
        }
    }

    @FXML
    public void onItalicAction() {
        estaActivoItalica = !estaActivoItalica;

        if (estaActivoItalica) {
            areaTexto.setStyle("-fx-font-style: italic;");
            italic.getStyleClass().add("button-active"); // Feedback visual RA4
        } else {
            areaTexto.setStyle("-fx-font-style: normal;");
            italic.getStyleClass().remove("button-active");
        }
    }

    @FXML
    private void onColorPickerAction(ActionEvent event) {

        Color color = colorpicker.getValue();

        String cssColor = String.format("-fx-text-fill: #%02x%02x%02x;", (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255), (int) (color.getBlue() * 255));

        Platform.runLater(() -> {
            try {
                areaTexto.setStyle(cssColor);
                System.out.println("Estilo aplicado correctamente a areaTexto.");
            } catch (Exception e) {
                System.err.println("¡ERROR al aplicar el estilo!");
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void onMayusAction(ActionEvent event) {
        String textoSeleccionado = areaTexto.getSelectedText();
        if (textoSeleccionado == null) {
            textoSeleccionado = areaTexto.getText();
        }
        String nuevoTexto = textoSeleccionado.toUpperCase();
        areaTexto.replaceSelection(nuevoTexto);
    }

    @FXML
    private void onMinusAction(ActionEvent event) {
        String textoSeleccionado = areaTexto.getSelectedText();
        if (textoSeleccionado == null) {
            textoSeleccionado = areaTexto.getText();
        }
        String nuevoTexto = textoSeleccionado.toLowerCase();
        areaTexto.replaceSelection(nuevoTexto);
    }

    @FXML
    private void onCapAction(ActionEvent event) {
        String textoSeleccionado = areaTexto.getSelectedText();
        if (textoSeleccionado == null) {
            textoSeleccionado = areaTexto.getText();
        }
        String nuevoTexto = textoSeleccionado.substring(0, 1).toUpperCase() + textoSeleccionado.substring(1);
        areaTexto.replaceSelection(nuevoTexto);
    }

    @FXML
    private void onInvertAction(ActionEvent event) {
        String textoSeleccionado = areaTexto.getSelectedText();
        if (textoSeleccionado == null) {
            textoSeleccionado = areaTexto.getText();
        }
        String nuevoTexto = new StringBuilder(textoSeleccionado).reverse().toString();
        areaTexto.replaceSelection(nuevoTexto);
    }

    @FXML
    private void onClearSpaceAction() {
        String texto = areaTexto.getText();

        if (texto.isEmpty() || texto == null) {
            return;
        }

        String textoEspacios = texto.trim();
        String textoLimpio = textoEspacios.replaceAll("\\s+", " ");
        areaTexto.setText(textoLimpio);

    }

    @FXML
    private void onBuscarAction() {
        ultimaPosicionBusqueda = 0;
        onSiguienteAction();

    }

    @FXML
    private void onSiguienteAction() {

        String textoBusqueda = campoBusqueda.getText();
        String textoCompleto = areaTexto.getText();

        if (textoBusqueda.isEmpty() || textoBusqueda == null) {
            return;
        }

        int indiceEncontrado = textoCompleto.indexOf(textoBusqueda, ultimaPosicionBusqueda);

        if (indiceEncontrado != -1) {

            areaTexto.selectRange(indiceEncontrado, indiceEncontrado + textoBusqueda.length());
            ultimaPosicionBusqueda = indiceEncontrado + 1;

        } else {
            ultimaPosicionBusqueda = 0;
        }

    }

    @FXML
    private void onReemplazarAction() {

        String textoBusqueda = campoBusqueda.getText();
        String textoReemplazo = campoBusqueda2.getText();
        String textoSeleccionado = areaTexto.getSelectedText();

        if (textoBusqueda.isEmpty() || textoBusqueda == null) {
            return;
        }

        if (textoSeleccionado != null && textoSeleccionado.equals(textoBusqueda)) {

            areaTexto.replaceSelection(textoReemplazo);
            ultimaPosicionBusqueda = areaTexto.getSelection().getEnd();

        }

        onSiguienteAction();

    }

    private void actualizarEstadoUndoRedo() {
        undo.setDisable(deshacer.isEmpty());
        redo.setDisable(rehacer.isEmpty());
    }

    // A partir de aqui es donde modifico el codigo

    @FXML
    private void onExportAction(ActionEvent event) {

        if (miProgressLabel == null) {
            System.out.println("⚠️ Recuperando ProgressLabel perdido...");
            miProgressLabel = new ProgressLabel();
            miProgressLabel.setPrefWidth(300);
        }
        // Inicializar el FileChooser si no existe

        if (this.fileChooser == null) {
            this.fileChooser = new FileChooser();
        }

        Node source = (Node) event.getSource();
        Window stage = source.getScene().getWindow();

        // Configurar el diálogo
        fileChooser.setTitle("Guardar documento");
        fileChooser.setInitialFileName("documento.txt");
        fileChooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Archivos de Texto", "*.txt"));

        // ventana de guardar
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            // Guardar directorio para la próxima vez
            fileChooser.setInitialDirectory(file.getParentFile());
            miProgressLabel.setEstado(AppState.WORKING);
            miProgressLabel.actualizarProgreso(-1, "Procesando...");
            mostrarVentanaProgreso("Exportando...");

            // Escribir el contenido del área de texto en el fichero
            saveTextToFile(file, areaTexto.getText());
            miProgressLabel.setEstado(AppState.DONE);
            miProgressLabel.actualizarProgreso(1, "Guardado con exito!");

        }

    }

    // Método auxiliar para escribir el fichero
    private void saveTextToFile(File file, String content) {
        try (BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(file))) {
            writer.write(content);
            System.out.println("Archivo guardado correctamente en: " + file.getAbsolutePath());
        } catch (Exception ex) {
            // Mostrar alerta si falla la escritura
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo guardar el archivo");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onImportAction(ActionEvent event) {

        if (miProgressLabel == null) {
            System.out.println("⚠️ Recuperando ProgressLabel perdido...");
            miProgressLabel = new ProgressLabel();
            miProgressLabel.setPrefWidth(300);
        }
        // Inicializar si no existe
        if (this.fileChooser == null) {
            this.fileChooser = new FileChooser();
        }

        Node source = (Node) event.getSource();
        Window stage = source.getScene().getWindow();

        fileChooser.setTitle("Abrir documento");
        fileChooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Archivos de Texto", "*.txt"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {

            fileChooser.setInitialDirectory(file.getParentFile());

            fileChooser.setInitialDirectory(file.getParentFile());
            miProgressLabel.setEstado(AppState.WORKING);
            miProgressLabel.actualizarProgreso(-1, "Procesando...");
            mostrarVentanaProgreso("Importando...");

            leerFicheroYMostrar(file);

            miProgressLabel.setEstado(AppState.DONE);
            miProgressLabel.actualizarProgreso(1, "Cargado con exito!");
        }
    }

    // Metodo auxiliar
    private void leerFicheroYMostrar(File file) {
        StringBuilder contenido = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;

            while ((linea = br.readLine()) != null) {
                contenido.append(linea).append("\n");
            }

            // Ponemos el texto en el editor
            areaTexto.setText(contenido.toString());

            System.out.println("Archivo importado con éxito: " + file.getName());

        } catch (IOException ex) {
            // Mostramos una alerta si ocurre algun error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Lectura");
            alert.setHeaderText("No se pudo abrir el archivo");
            alert.setContentText("Detalles: " + ex.getMessage());
            alert.showAndWait();
        }

    }

    @FXML
    private void onNuevoAction(ActionEvent event) {
        // Si ya está vacío, no se hace nada
        if (areaTexto.getText().isEmpty()) {
            return;
        }

        // Crear alerta de seguridad
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Nuevo Documento");
        alerta.setHeaderText("¿Borrar todo el contenido?");
        alerta.setContentText("Esta acción no se puede deshacer.");

        // Mostrar y esperar respuesta
        Optional<ButtonType> resultado = alerta.showAndWait();

        // OK borramos
        // Cancelar no hacemos nada.
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            areaTexto.setText("");

        }
    }

    private void mostrarVentanaProgreso(String titulo) {

        if (stage == null) {
            stage = new Stage();

            stage.setTitle(titulo);
            stage.setResizable(false);

            stage.initModality(Modality.APPLICATION_MODAL);
            javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(miProgressLabel);
            root.setStyle("-fx-padding: 10;");

            stage.setScene(new javafx.scene.Scene(root, 300, 70));
        } else {

            stage.setTitle(titulo);

        }

        stage.show();
        stage.toFront();

    }

    // ========== MÉTODOS NUI ==========

    /**
     * Maneja la acción del botón "Ejecutar Comando de Voz"
     */
    @FXML
    private void onEjecutarVozAction(ActionEvent event) {
        if (campoVozSimulada == null) {
            System.err.println("[NUI] Campo de voz no disponible");
            return;
        }

        String input = campoVozSimulada.getText();
        if (input == null || input.trim().isEmpty()) {
            actualizarEstadoNui("⚠️ Escribe un comando primero", false);
            return;
        }

        boolean reconocido = speechAdapter.processInput(input);
        if (!reconocido) {
            actualizarEstadoNui("❌ Comando no reconocido: " + input, false);
        }

        // Limpiar campo después de procesar
        campoVozSimulada.clear();
    }

    /**
     * Maneja la acción del botón de dictado
     */
    @FXML
    private void onDictadoAction(ActionEvent event) {
        // Crear diálogo para dictado
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Dictado por Voz");
        dialog.setHeaderText("Modo Dictado (Simulado)");
        dialog.setContentText("Escribe el texto que quieres dictar:");

        Optional<String> resultado = dialog.showAndWait();
        resultado.ifPresent(texto -> {
            if (!texto.trim().isEmpty()) {
                // Enviar como comando DICTAR_TEXTO
                nuiController.dispatch(NuiCommand.DICTAR_TEXTO, texto);
            }
        });
    }

    /**
     * Implementación de NuiListener.onCommand
     * Procesa los comandos NUI y ejecuta las acciones correspondientes
     */
    @Override
    public void onCommand(NuiCommand cmd, String payload) {
        Platform.runLater(() -> {
            System.out.println("[NUI] Ejecutando comando: " + cmd);

            switch (cmd) {
                case NUEVO_DOCUMENTO:
                    onNuevoAction(null);
                    actualizarEstadoNui("✓ Nuevo documento", true);
                    break;

                case ABRIR_DOCUMENTO:
                    onImportAction(null);
                    actualizarEstadoNui("✓ Abrir documento", true);
                    break;

                case GUARDAR_DOCUMENTO:
                    onExportAction(null);
                    actualizarEstadoNui("✓ Guardar documento", true);
                    break;

                case APLICAR_NEGRITA:
                    onBoldAction();
                    actualizarEstadoNui("✓ Negrita " + (estaActivoNegrita ? "activada" : "desactivada"), true);
                    break;

                case APLICAR_CURSIVA:
                    onItalicAction();
                    actualizarEstadoNui("✓ Cursiva " + (estaActivoItalica ? "activada" : "desactivada"), true);
                    break;

                case COLOR_ROJO:
                    aplicarColorTexto(Color.RED);
                    actualizarEstadoNui("✓ Color rojo aplicado", true);
                    break;

                case COLOR_AZUL:
                    aplicarColorTexto(Color.BLUE);
                    actualizarEstadoNui("✓ Color azul aplicado", true);
                    break;

                case DICTAR_TEXTO:
                    if (payload != null && !payload.isEmpty()) {
                        // Añadir texto al área de texto
                        String textoActual = areaTexto.getText();
                        if (!textoActual.isEmpty() && !textoActual.endsWith(" ") && !textoActual.endsWith("\n")) {
                            areaTexto.appendText(" ");
                        }
                        areaTexto.appendText(payload);
                        actualizarEstadoNui("✓ Dictado: \"" + payload + "\"", true);
                    }
                    break;

                default:
                    actualizarEstadoNui("⚠️ Comando no implementado: " + cmd, false);
            }
        });
    }

    /**
     * Aplica un color específico al texto
     */
    private void aplicarColorTexto(Color color) {
        String cssColor = String.format("-fx-text-fill: #%02x%02x%02x;",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
        areaTexto.setStyle(cssColor);
    }

    /**
     * Actualiza el label de estado NUI
     */
    private void actualizarEstadoNui(String mensaje, boolean exito) {
        if (lblEstadoNui != null) {
            lblEstadoNui.setText(mensaje);
            lblEstadoNui.setStyle(exito ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        }
        System.out.println("[NUI Estado] " + mensaje);
    }
}