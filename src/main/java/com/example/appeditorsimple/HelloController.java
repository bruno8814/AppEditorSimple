package com.example.appeditorsimple;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;


import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Stack;

public class HelloController {
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
    private ColorPicker colorpicker;
    @FXML
    private Button btnExportar;

    @FXML
    private Pane pane;

    private ProgressLabel miProgressLabel;


    private Stack<String> deshacer = new Stack<>();
    private Stack<String> rehacer = new Stack<>();

    private boolean undoRedoEnabled = false;

    private boolean estaActivoNegrita = false;
    private boolean estaActivoItalica = false;
    int ultimaPosicionBusqueda = 0;
    private FileChooser fileChooser;
    private Stage stage;



    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        campoBusqueda.setPromptText("Buscar...");
        campoBusqueda2.setPromptText("Reemplazar...");
        areaTexto.setPromptText(" Escribe tu texto aquí...");

        miProgressLabel = new ProgressLabel();
        miProgressLabel.setPrefWidth(300);

        areaTexto.textProperty().addListener((observable, oldValue, newValue) -> {



            //Si el cambio fue causado por nuestro propio código de undo/redo, lo ignoramos.
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



    }

    public void contadorLabel(String texto) {
        int caracteres = texto.length();

        int numPalabras = 0;
        // Usamos trim() para quitar espacios al principio y al final.
        if (!texto.trim().isEmpty()) {
        // split("\\s+") divide el texto por uno o más espacios, tabuladores o saltos de línea.
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
                espacios
        );

        contador.setText(textoContador);


    }

    @FXML
     public void onUndoAction () {

        if(deshacer.isEmpty()) {
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
    public void onRedoAction () {
        if(rehacer.isEmpty()) {
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

        if(estaActivoNegrita) {
            areaTexto.setStyle("-fx-font-weight: bold;");
        } else {
            areaTexto.setStyle("-fx-font-weight: normal;");
        }


    }

    @FXML

    public void onItalicAction() {

        estaActivoItalica = !estaActivoItalica;

        if(estaActivoItalica) {
            areaTexto.setStyle("-fx-font-style: italic;");
        } else {
            areaTexto.setStyle("-fx-font-style: normal;");
        }

    }


    @FXML
    private void onColorPickerAction (ActionEvent event) {

        Color color = colorpicker.getValue();

        String cssColor = String.format("-fx-text-fill: #%02x%02x%02x;", (int)(color.getRed() * 255), (int)(color.getGreen() * 255), (int)(color.getBlue() * 255));


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
        if(textoSeleccionado == null) {
            textoSeleccionado = areaTexto.getText();
        }
        String nuevoTexto = textoSeleccionado.toUpperCase();
        areaTexto.replaceSelection(nuevoTexto);
    }

    @FXML
    private void onMinusAction(ActionEvent event) {
        String textoSeleccionado = areaTexto.getSelectedText();
        if(textoSeleccionado == null) {
            textoSeleccionado = areaTexto.getText();
        }
        String nuevoTexto = textoSeleccionado.toLowerCase();
        areaTexto.replaceSelection(nuevoTexto);
    }

    @FXML
    private void onCapAction(ActionEvent event) {
        String textoSeleccionado = areaTexto.getSelectedText();
        if(textoSeleccionado == null) {
            textoSeleccionado = areaTexto.getText();
        }
        String nuevoTexto = textoSeleccionado.substring(0, 1).toUpperCase() + textoSeleccionado.substring(1);
        areaTexto.replaceSelection(nuevoTexto);
    }

    @FXML
    private void onInvertAction(ActionEvent event) {
        String textoSeleccionado = areaTexto.getSelectedText();
        if(textoSeleccionado == null) {
            textoSeleccionado = areaTexto.getText();
        }
        String nuevoTexto = new StringBuilder(textoSeleccionado).reverse().toString();
        areaTexto.replaceSelection(nuevoTexto);
    }

    @FXML
    private void onClearSpaceAction(){
        String texto = areaTexto.getText();

        if(texto.isEmpty() || texto == null) {
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

        if(textoBusqueda.isEmpty() || textoBusqueda == null) {
            return;
        }

        int indiceEncontrado = textoCompleto.indexOf(textoBusqueda, ultimaPosicionBusqueda);

        if(indiceEncontrado != -1) {

            areaTexto.selectRange(indiceEncontrado, indiceEncontrado + textoBusqueda.length());
            ultimaPosicionBusqueda = indiceEncontrado + 1;

        }else {
            ultimaPosicionBusqueda = 0;
        }



    }

    @FXML
    private void onReemplazarAction() {

        String textoBusqueda = campoBusqueda.getText();
        String textoReemplazo = campoBusqueda2.getText();
        String textoSeleccionado = areaTexto.getSelectedText();

        if(textoBusqueda.isEmpty() || textoBusqueda == null) {
            return;
        }

        if (textoSeleccionado != null && textoSeleccionado.equals(textoBusqueda) ) {

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
                new FileChooser.ExtensionFilter("Archivos de Texto", "*.txt")
        );

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
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
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
        //Inicializar si no existe
        if (this.fileChooser == null) {
            this.fileChooser = new FileChooser();
        }


        Node source = (Node) event.getSource();
        Window stage = source.getScene().getWindow();


        fileChooser.setTitle("Abrir documento");
        fileChooser.getExtensionFilters().setAll(
                new FileChooser.ExtensionFilter("Archivos de Texto", "*.txt"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );


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
        //Si ya está vacío, no se hace nada
        if (areaTexto.getText().isEmpty()) {
            return;
        }

        //Crear alerta de seguridad
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


    private void mostrarVentanaProgreso(String titulo){

        if (stage == null) {
            stage = new Stage();


            stage.setTitle(titulo);
            stage.setResizable(false);

            stage.initModality(Modality.APPLICATION_MODAL);
            javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane(miProgressLabel);
            root.setStyle("-fx-padding: 10;");

            stage.setScene(new javafx.scene.Scene(root, 300, 70));
        }else{

            stage.setTitle(titulo);

        }


        stage.show();
        stage.toFront();


    }




}