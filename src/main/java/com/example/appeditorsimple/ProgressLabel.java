package com.example.appeditorsimple;


import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ProgressLabel extends VBox {

    private Label lblMensaje;
    private ProgressBar progressBar;

    public ProgressLabel() {
        this.setSpacing(5);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setStyle("-fx-padding: 10px; -fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        lblMensaje = new Label("Procesando...");
        lblMensaje.setFont(Font.font("System", FontWeight.NORMAL, 12));
        lblMensaje.setTextFill(javafx.scene.paint.Color.DARKGRAY);

        progressBar = new ProgressBar(0.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setProgress(0);

        this.getChildren().addAll(lblMensaje, progressBar);

        setEstado(AppState.IDLE);

    }


    public void setEstado(AppState estado) {

        Platform.runLater(() -> {
            switch (estado) {
                case IDLE:
                    lblMensaje.setText("Listo!");
                    lblMensaje.setTextFill(Color.GRAY);
                    progressBar.setVisible(false);
                break;

                case WORKING:
                    lblMensaje.setTextFill(Color.BLACK);
                    progressBar.setVisible(true);
                    progressBar.setStyle("-fx-accent: #007bff;");
                    break;

                case DONE:
                    lblMensaje.setTextFill(Color.GREEN);
                    progressBar.setVisible(true);
                    progressBar.setProgress(1.0);
                    progressBar.setStyle("-fx-accent: #28a745;");
                    break;

                case ERROR:
                    lblMensaje.setTextFill(Color.RED);
                    progressBar.setVisible(true);
                    progressBar.setProgress(1.0);
                    progressBar.setStyle("-fx-accent: #dc3545;");
                    break;

            }
        });

    }

    public void actualizarProgreso(double progreso, String mensaje) {
        Platform.runLater(() -> {
            this.progressBar.setProgress(progreso);
            this.lblMensaje.setText(mensaje);
        });
    }


    public void reiniciar() {
        setEstado(AppState.IDLE);
        actualizarProgreso(0, "Listo!");
    }

}
