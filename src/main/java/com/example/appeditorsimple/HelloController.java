package com.example.appeditorsimple;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

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
    private Button bold;

    @FXML
    private Button italic;

    @FXML
    private ColorPicker colorpicker;


    private Stack<String> deshacer = new Stack<>();
    private Stack<String> rehacer = new Stack<>();

    private boolean undoRedoEnabled = false;

    private boolean estaActivoNegrita = false;
    private boolean estaActivoItalica = false;
    int ultimaPosicionBusqueda = 0;



    @FXML
    public void initialize() {

        campoBusqueda.setPromptText("Buscar...");
        campoBusqueda2.setPromptText("Reemplazar...");
        areaTexto.setPromptText(" Escribe tu texto aquí...");

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

    //crear para un boton para cuando este encima que salga un pequeño texto flotante







}
