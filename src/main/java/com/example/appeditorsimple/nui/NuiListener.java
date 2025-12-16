package com.example.appeditorsimple.nui;

/**
 * Interface que debe implementar cualquier componente que quiera
 * recibir comandos NUI (voz o gestos).
 */
public interface NuiListener {

    /**
     * Método invocado cuando se recibe un comando NUI.
     * 
     * @param cmd     El comando NUI a ejecutar
     * @param payload Datos adicionales del comando (ej: texto para DICTAR_TEXTO).
     *                Puede ser null si el comando no requiere payload.
     */
    void onCommand(NuiCommand cmd, String payload);
}
