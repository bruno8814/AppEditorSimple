package com.example.appeditorsimple.nui;

import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador que simula entrada por voz.
 * Convierte texto escrito en comandos NUI usando palabras clave.
 * 
 * En una implementación real, este adaptador recibiría texto
 * de una librería ASR como Vosk y lo convertiría a comandos.
 */
public class SpeechInputAdapter {

    private final NuiController controller;
    private final Map<String, NuiCommand> keywords;

    /**
     * Crea un nuevo adaptador de voz simulada.
     * 
     * @param controller El NuiController al que enviar los comandos
     */
    public SpeechInputAdapter(NuiController controller) {
        this.controller = controller;
        this.keywords = new HashMap<>();
        initKeywords();
    }

    /**
     * Inicializa el mapa de palabras clave a comandos.
     */
    private void initKeywords() {
        // Comandos de documento
        keywords.put("nuevo", NuiCommand.NUEVO_DOCUMENTO);
        keywords.put("abrir", NuiCommand.ABRIR_DOCUMENTO);
        keywords.put("guardar", NuiCommand.GUARDAR_DOCUMENTO);

        // Comandos de formato
        keywords.put("negrita", NuiCommand.APLICAR_NEGRITA);
        keywords.put("cursiva", NuiCommand.APLICAR_CURSIVA);

        // Comandos de color
        keywords.put("rojo", NuiCommand.COLOR_ROJO);
        keywords.put("azul", NuiCommand.COLOR_AZUL);
    }

    /**
     * Procesa una entrada de texto simulando reconocimiento de voz.
     * Busca palabras clave y genera el comando correspondiente.
     * 
     * @param input El texto "reconocido" (simulado)
     * @return true si se reconoció un comando, false en caso contrario
     */
    public boolean processInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        String lowercaseInput = input.toLowerCase().trim();

        // Caso especial: dictado de texto
        // Formato: "dictar <texto a dictar>"
        if (lowercaseInput.startsWith("dictar ")) {
            String textoADictar = input.substring(7).trim(); // Preservar mayúsculas originales
            if (!textoADictar.isEmpty()) {
                controller.dispatch(NuiCommand.DICTAR_TEXTO, textoADictar);
                return true;
            }
        }

        // Buscar palabra clave exacta
        if (keywords.containsKey(lowercaseInput)) {
            controller.dispatch(keywords.get(lowercaseInput));
            return true;
        }

        // Buscar palabra clave dentro del texto
        for (Map.Entry<String, NuiCommand> entry : keywords.entrySet()) {
            if (lowercaseInput.contains(entry.getKey())) {
                controller.dispatch(entry.getValue());
                return true;
            }
        }

        System.out.println("[SpeechInputAdapter] No se reconoció comando en: " + input);
        return false;
    }

    /**
     * Obtiene la lista de palabras clave soportadas.
     * Útil para mostrar ayuda al usuario.
     * 
     * @return Array con las palabras clave disponibles
     */
    public String[] getAvailableKeywords() {
        return keywords.keySet().toArray(new String[0]);
    }
}
