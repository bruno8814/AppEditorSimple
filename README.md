📝 Editor de Texto Avanzado (JavaFX)
![alt text](https://img.shields.io/badge/Java-21-orange)
![alt text](https://img.shields.io/badge/JavaFX-21-blue)
![alt text](https://img.shields.io/badge/Status-Terminado-green)
Una aplicación de escritorio robusta para la edición de texto enriquecido, desarrollada como parte de la práctica UT2 - Desarrollo de Interfaces. Este proyecto implementa una arquitectura MVC, gestión de eventos complejos y componentes personalizados.
🚀 Características Principales
1. Gestión de Archivos y Persistencia
📂 Importar/Exportar: Lectura y escritura de archivos de texto (.txt) mediante FileChooser.
✨ Persistencia de Formato: El sistema conserva el estilo (negrita, cursiva) mediante un sistema de marcado interno al guardar y cargar.
🛡️ Feedback Visual: Uso de ventanas modales y barras de progreso para informar al usuario del estado de las operaciones de E/S.
2. Edición y Estilo
🎨 Formato de Texto: Aplicación de Negrita, Cursiva y Color de fuente personalizado.
🔠 Transformaciones:
Mayúsculas / Minúsculas.
Capitalización (Primera letra mayúscula).
Invertir texto (Reverse).
Limpieza de espacios duplicados.
3. Herramientas de Productividad
🔍 Búsqueda y Reemplazo: Buscador integrado con función "Siguiente" y reemplazo de texto seleccionado.
↩️ Deshacer / Rehacer: Implementación de pilas (Stack) para un historial de cambios ilimitado (Undo/Redo).
📊 Estadísticas en Tiempo Real: Contador dinámico de caracteres, palabras y espacios.
4. UX/UI Avanzada
⚠️ Modo Seguro: Confirmación mediante diálogos modales antes de acciones destructivas (como "Nuevo Documento").
Componente Personalizado (ProgressLabel): Un componente visual propio diseñado desde cero para gestionar estados de la aplicación.
🛠️ Arquitectura Técnica
Componente Propio: ProgressLabel
El corazón de la retroalimentación visual es la clase ProgressLabel, que extiende de VBox.
Estados: Gestiona un Enum AppState (IDLE, WORKING, DONE, ERROR).
Concurrencia: Integrado con Platform.runLater para actualizaciones seguras desde hilos secundarios.
Visualización: Combina un Label descriptivo y un ProgressBar que cambia de color según el estado (Azul: procesando, Verde: éxito, Rojo: error).
Estructura de Clases
HelloApplication: Punto de entrada (Main).
HelloController: Controlador principal que gestiona la lógica de la vista y eventos.
ProgressLabel: Componente visual reutilizable.
AppState: Enumerado para la máquina de estados del componente.
📸 Capturas de Pantalla
Vista Principal	Ventana de Progreso
[Pon aquí una captura de tu editor]	[Pon aquí una captura de la ventanita flotante]
📖 Manual de Uso Rápido
Escribir: Usa el área central para redactar. Los contadores inferiores se actualizarán solos.
Dar Estilo: Selecciona el texto y usa los botones de la barra superior (Bold, Italic, Color).
Buscar: Escribe en el campo "Buscar..." y pulsa Siguiente para navegar por las coincidencias.
Guardar: Pulsa Exportar. Aparecerá una ventana flotante indicando el progreso de la operación.
⚙️ Requisitos de Ejecución
JDK: Java Development Kit 21 o superior.
JavaFX SDK: Versión 21.
IDE Recomendado: IntelliJ IDEA o Eclipse.
👨‍💻 Autor
[Tu Nombre y Apellidos]
Asignatura: Desarrollo de Interfaces (DAM2)
Curso: 2025-2026
