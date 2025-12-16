package com.example.appeditorsimple.nui;

/**
 * Enum que define todos los comandos NUI soportados por el editor.
 * Cada comando representa una acción que puede ser disparada por voz o gestos.
 */
public enum NuiCommand {

    // Comandos de documento
    NUEVO_DOCUMENTO,
    ABRIR_DOCUMENTO,
    GUARDAR_DOCUMENTO,

    // Comandos de formato
    APLICAR_NEGRITA,
    APLICAR_CURSIVA,

    // Comandos de color
    COLOR_ROJO,
    COLOR_AZUL,

    // Comando de dictado (payload contiene el texto a dictar)
    DICTAR_TEXTO
}
