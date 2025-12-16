package com.example.appeditorsimple.nui;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador central de la capa NUI.
 * Gestiona los listeners y distribuye los comandos recibidos desde
 * los adaptadores de entrada (voz, gestos) hacia la UI.
 * 
 * Esta clase NO depende de JavaFX/Swing, solo maneja la lógica de comandos.
 */
public class NuiController {

    private final List<NuiListener> listeners = new ArrayList<>();

    /**
     * Registra un nuevo listener para recibir comandos NUI.
     * 
     * @param listener El listener a registrar
     */
    public void addListener(NuiListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Elimina un listener registrado.
     * 
     * @param listener El listener a eliminar
     */
    public void removeListener(NuiListener listener) {
        listeners.remove(listener);
    }

    /**
     * Distribuye un comando a todos los listeners registrados.
     * Este método es llamado por los adaptadores de entrada (SpeechInputAdapter,
     * etc.)
     * 
     * @param cmd     El comando NUI a ejecutar
     * @param payload Datos adicionales (puede ser null)
     */
    public void dispatch(NuiCommand cmd, String payload) {
        if (cmd == null) {
            System.err.println("[NuiController] Comando nulo ignorado");
            return;
        }

        System.out.println("[NuiController] Dispatching: " + cmd +
                (payload != null ? " (payload: " + payload + ")" : ""));

        for (NuiListener listener : listeners) {
            try {
                listener.onCommand(cmd, payload);
            } catch (Exception e) {
                System.err.println("[NuiController] Error en listener: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Distribuye un comando sin payload.
     * 
     * @param cmd El comando NUI a ejecutar
     */
    public void dispatch(NuiCommand cmd) {
        dispatch(cmd, null);
    }
}
