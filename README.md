# Proyecto 2: Simulador Virtual de Sistema de Archivos

## Descripción
Simulador de sistema de archivos en Java que implementa gestión de archivos y directorios con asignación encadenada de bloques, sistema de procesos, planificación de disco (FIFO, SSTF, SCAN, C-SCAN), y buffer cache. Incluye interfaz gráfica con visualización de estructura jerárquica, estado del disco, y cola de procesos.

## Integrantes del Equipo
- Gabriel Orozco - [TU CARNET]
- [Nombre del otro integrante si lo hay] - [CARNET]

## Repositorio GitHub
[URL de tu repositorio en GitHub]

## Características Implementadas
- ✅ Sistema de archivos jerárquico con directorios
- ✅ Asignación encadenada de bloques en disco simulado
- ✅ 4 algoritmos de planificación de disco (FIFO, SSTF, SCAN, C-SCAN)
- ✅ Sistema de procesos con estados
- ✅ Buffer cache con política FIFO
- ✅ Operaciones CRUD sobre archivos y directorios
- ✅ Interfaz gráfica completa
- ✅ Modo Administrador y Modo Usuario
- ✅ Persistencia de datos (JSON/CSV)
- ✅ Visualización en tiempo real del disco y bloques
- ✅ Log de eventos del sistema

## Tecnologías Utilizadas
- Java 21+
- NetBeans IDE
- Swing (GUI)
- Maven (Gestión de dependencias)

## Estructura del Proyecto
```
src/main/java/
├── GUI/
│   └── Ventana_Principal.java     # Interfaz gráfica
├── sistema/
│   └── SO.java                     # Sistema operativo
├── FS/
│   ├── SistemaArchivos.java        # Gestión de archivos
│   └── BufferCache.java            # Caché de bloques
├── modelo/
│   ├── SD.java                     # Simulación del disco
│   ├── Bloque.java                 # Bloque de disco
│   ├── Archivo.java                # Modelo de archivo
│   ├── Directorio.java             # Modelo de directorio
│   ├── PCB.java                    # Process Control Block
│   └── SolicitudIO.java            # Solicitud de operación I/O
├── planificacion/
│   ├── Planificador.java           # Interfaz planificador
│   ├── FIFO.java                   # First In First Out
│   ├── SSTF.java                   # Shortest Seek Time First
│   ├── SCAN.java                   # Algoritmo del elevador
│   └── CSCAN.java                  # Circular SCAN
├── cpu/
│   ├── CPU.java                    # Simulación de CPU
│   └── Reloj.java                  # Reloj del sistema
├── estructura_datos/
│   ├── Cola.java
│   ├── ListaEnlazada.java
│   └── Nodo.java
└── Persistencia/
    ├── PersistenciaJSON.java
    └── PersistenciaCSV.java
```

## Cómo Ejecutar
1. Abrir el proyecto en NetBeans
2. Compilar (Clean and Build)
3. Ejecutar `Ventana_Principal.java`

Alternativamente, desde línea de comandos:
```bash
mvn clean package
mvn exec:java -Dexec.mainClass="GUI.Ventana_Principal"
```

## Funcionalidades de la GUI

### Panel de Controles
- **Modo Usuario/Administrador**: Cambia entre permisos restringidos y completos
- **Planificador de Disco**: Selector entre FIFO, SSTF, SCAN y C-SCAN
- **Botones de Operaciones**:
  - Crear Archivo
  - Crear Directorio
  - Leer
  - Renombrar
  - Eliminar
- **Control de Simulación**: Pausar/Reanudar
- **Control de Velocidad** (Panel mejorado con código de colores):
  - 🔴 **x4** (100ms) - Rojo claro: Muy rápido, ideal para pruebas
  - 🟠 **x2** (300ms) - Naranja claro: Normal-rápido, para desarrollo
  - 🟢 **x1** (500ms) - Verde claro: Lento (default), para visualización
  - 🔵 **x0.5** (1000ms) - Azul claro: Muy lento, para análisis detallado
  - Slider continuo: 50ms - 2000ms con marcas cada 100ms
  - Label dinámico que muestra velocidad actual y categoría
  - Panel organizado verticalmente con borde y título
- **Estadísticas**: Ver reporte del sistema

### Paneles de Visualización
1. **Sistema de Archivos**: Árbol jerárquico con archivos y directorios
2. **Simulación de Disco**: Visualización gráfica de bloques (libres/ocupados)
3. **Tabla de Asignación**: Detalles de archivos con bloques asignados
4. **Buffer Cache**: Estado actual del caché FIFO
5. **Log de Eventos**: Registro en tiempo real de operaciones
6. **Cola de Procesos**: Estados de procesos (Listos, Bloqueados, Terminados)

### Menú de Persistencia
- **Guardar Estado del Sistema** (.json)
- **Cargar Estado del Sistema** (.json)
- **Exportar Resumen del Sistema** (.txt)
- **Exportar Estadísticas de Procesos** (.csv)

## Algoritmos de Planificación de Disco

### FIFO (First In First Out)
Atiende las solicitudes en orden de llegada.

### SSTF (Shortest Seek Time First)
Atiende primero la solicitud más cercana a la posición actual de la cabeza.

### SCAN (Algoritmo del Elevador)
La cabeza se mueve en una dirección hasta el final, luego invierte dirección.

### C-SCAN (Circular SCAN)
La cabeza se mueve en una dirección, al llegar al final regresa al inicio.

## Sistema de Permisos

### Modo Administrador
- Crear archivos y directorios
- Eliminar archivos y directorios
- Renombrar elementos
- Leer cualquier archivo
- Ver estadísticas del sistema

### Modo Usuario
- Solo lectura de archivos propios o públicos
- No puede crear, modificar o eliminar

## Datos Precargados
Al iniciar el simulador, se crean automáticamente:
- Directorios: `documentos`, `imagenes`, `proyectos`
- Archivos en raíz: `readme.txt`, `config.ini`
- Archivos en `documentos`: `informe.docx`, `notas.txt`
- Archivos en `imagenes`: `foto1.jpg`, `logo.png`

## Autores
Proyecto académico - Sistemas Operativos
Universidad Metropolitana - Trimestre 2425-2

## Licencia
Este proyecto es de código abierto bajo licencia MIT.
