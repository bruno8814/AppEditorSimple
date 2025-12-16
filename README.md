# AppEditorSimple - Editor de Texto con Reconocimiento de Voz

Editor de texto simple con soporte para reconocimiento de voz usando Vosk.

## Requisitos

- **Java 23** o superior
- **Maven 3.8+**
- Micrófono funcional (para reconocimiento de voz)

---

## Configuración del Modelo de Voz Vosk

El reconocimiento de voz requiere descargar un modelo de idioma. Para español:

### Paso 1: Descargar el modelo

Descarga el modelo español pequeño (~39MB):

👉 **[Descargar vosk-model-small-es-0.42.zip](https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip)**

También disponible en: https://alphacephei.com/vosk/models

### Paso 2: Extraer el modelo

Extrae el archivo ZIP. Tendrás una carpeta llamada `vosk-model-small-es-0.42`.

### Paso 3: Colocar el modelo

Tienes **3 opciones** (el programa busca automáticamente en este orden):

#### Opción A: En la raíz del proyecto (Recomendado)
```
AppEditorSimple/
├── vosk-model-small-es-0.42/    ← Colocar aquí
│   ├── conf/
│   ├── graph/
│   ├── am/
│   └── ...
├── src/
├── pom.xml
└── ...
```

#### Opción B: En tu carpeta de usuario
```
C:\Users\TuUsuario\vosk-model-small-es-0.42\
```
o en Linux/Mac:
```
~/vosk-model-small-es-0.42/
```

#### Opción C: Seleccionar manualmente
Si el modelo no se encuentra automáticamente, aparecerá un diálogo para seleccionar la carpeta del modelo.

---

## Ejecución

### Desde línea de comandos:
```bash
mvn clean javafx:run
```

### Desde un IDE:
Ejecuta la clase `HelloApplication.java`

---

## Uso del Reconocimiento de Voz

### 1. Cargar el modelo
Haz clic en **"📂 Cargar Modelo"** para cargar el modelo de voz.
- Si el modelo está en una ruta por defecto, se cargará automáticamente
- Si no, aparecerá un diálogo para seleccionar la carpeta

### 2. Iniciar grabación
Una vez cargado el modelo, haz clic en **"🎤 Iniciar Grabación"**.

### 3. Hablar
- **Comandos**: Puedes decir comandos como:
  - "nuevo documento"
  - "guardar documento"
  - "aplicar negrita"
  - "aplicar cursiva"
  - "color rojo" / "color azul"
  
- **Dictado**: Cualquier texto que no sea un comando se insertará automáticamente en el editor.

### 4. Detener grabación
Haz clic en **"⏹ Detener Grabación"** para parar.

---

## Comandos de Voz Disponibles

| Comando | Acción |
|---------|--------|
| "nuevo documento" / "nuevo" | Crea documento nuevo |
| "abrir documento" / "abrir" | Abre diálogo de importar |
| "guardar documento" / "guardar" | Abre diálogo de exportar |
| "aplicar negrita" / "negrita" | Activa/desactiva negrita |
| "aplicar cursiva" / "cursiva" | Activa/desactiva cursiva |
| "color rojo" | Aplica color rojo al texto |
| "color azul" | Aplica color azul al texto |
| *(cualquier otro texto)* | Se dicta al editor |

---

## Solución de Problemas

### "Modelo no encontrado"
- Verifica que descargaste el modelo correctamente
- Asegúrate de que la carpeta se llama `vosk-model-small-es-0.42` (sin `.zip`)
- Coloca la carpeta en la raíz del proyecto

### "Micrófono no soportado"
- Verifica que tienes un micrófono conectado
- Comprueba que el micrófono tiene permisos en Windows

### El reconocimiento es impreciso
- Habla claro y a un volumen normal
- Reduce el ruido de fondo
- Para mejor precisión, descarga el modelo grande (~1.5GB) desde la web de Vosk

---

## Estructura del Proyecto

```
AppEditorSimple/
├── src/main/java/
│   └── com/example/appeditorsimple/
│       ├── HelloApplication.java    # Clase principal
│       ├── HelloController.java     # Controlador UI
│       └── nui/                      # Sistema de voz
│           ├── NuiController.java
│           ├── NuiCommand.java
│           ├── SpeechInputAdapter.java
│           └── VoskSpeechAdapter.java
├── src/main/resources/
│   └── com/example/appeditorsimple/
│       └── hello-view.fxml
├── vosk-model-small-es-0.42/        # ← Modelo de voz (descargar)
├── pom.xml
└── README.md
```

---

## Licencias

- **AppEditorSimple**: MIT License
- **Vosk Model**: Apache 2.0 License
- **Vosk API**: Apache 2.0 License
