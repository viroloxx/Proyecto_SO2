# Funcionamiento Detallado del Sistema de Entrada/Salida (E/S)

## Tabla de Contenidos
1. [Visión General del Sistema E/S](#visión-general)
2. [Flujo Completo de una Operación E/S](#flujo-completo)
3. [Componentes del Sistema E/S](#componentes)
4. [Ciclo de Ejecución Detallado](#ciclo-de-ejecución)
5. [Ejemplo Paso a Paso](#ejemplo-paso-a-paso)
6. [Integración con Planificadores](#integración-planificadores)
7. [Integración con Buffer Cache](#integración-cache)
8. [Diagramas de Secuencia](#diagramas)

---

## Visión General del Sistema E/S

### Arquitectura General

El sistema de E/S implementado sigue el modelo clásico de Sistemas Operativos con los siguientes componentes:

```
┌─────────────────────────────────────────────────────────────┐
│                        USUARIO (GUI)                         │
│  [Doble click en archivo] o [Botón "Leer"]                  │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                   SISTEMA OPERATIVO (SO)                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ Cola LISTOS  │  │     CPU      │  │Cola BLOQUEADOS│      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                               │
│  ┌──────────────────────────────────────────────────┐       │
│  │           Cola de Solicitudes I/O                │       │
│  └──────────────────────────────────────────────────┘       │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                   PLANIFICADOR DE DISCO                      │
│  [FIFO] [SSTF] [SCAN] [C-SCAN]                              │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                      BUFFER CACHE                            │
│  [Cache Hit] → Retornar de memoria                          │
│  [Cache Miss] → Leer de disco                               │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ↓
┌─────────────────────────────────────────────────────────────┐
│                    DISCO DURO (SD)                           │
│  [Bloque 0] [Bloque 1] ... [Bloque 99]                      │
└─────────────────────────────────────────────────────────────┘
```

### Principios Fundamentales

El sistema E/S implementa los siguientes principios de SO:

1. **Asincronía**: Las operaciones E/S no bloquean todo el sistema
2. **Multiprogramación**: Mientras un proceso espera E/S, otro puede usar CPU
3. **Planificación**: Optimización del orden de acceso a disco
4. **Buffering**: Cache para reducir accesos físicos a disco
5. **Estados de Procesos**: Transiciones claras entre estados

---

## Flujo Completo de una Operación E/S

### Descripción del Flujo

Cuando un usuario solicita leer un archivo, el sistema ejecuta los siguientes pasos:

```
1. Solicitud del Usuario (GUI)
   ↓
2. Creación de PCB y Solicitud I/O
   ↓
3. Proceso → Estado NUEVO → Estado LISTO
   ↓
4. Proceso entra en Cola de LISTOS
   ↓
5. CPU disponible → Asignar proceso a CPU
   ↓
6. Proceso → Estado EJECUTANDO
   ↓
7. Ejecutar N ciclos en CPU (2-4 ciclos)
   ↓
8. Proceso completa fase CPU → Genera solicitud I/O
   ↓
9. Proceso → Estado BLOQUEADO
   ↓
10. Solicitud I/O → Cola de Solicitudes I/O
    ↓
11. Planificador selecciona próxima solicitud
    ↓
12. Mover cabezal del disco
    ↓
13. Buffer Cache verifica si bloque está en memoria
    ↓
14a. [Cache Hit] → Retornar bloque de memoria
    ↓
14b. [Cache Miss] → Leer bloque de disco → Agregar a cache
    ↓
15. Marcar solicitud I/O como completada
    ↓
16. Proceso → Estado LISTO (vuelve a cola de listos)
    ↓
17. Proceso → Estado TERMINADO
```

---

## Componentes del Sistema E/S

### 1. Solicitud de I/O (`SolicitudIO`)

**Clase:** `modelo/SolicitudIO.java`

**Propósito:** Encapsula toda la información de una operación de E/S.

**Estructura:**
```java
public class SolicitudIO {
    private TipoOperacion tipo;           // LEER, ESCRIBIR, CREAR, ELIMINAR
    private String nombreArchivo;         // Nombre del archivo objetivo
    private int bloqueObjetivo;          // Número de bloque a acceder
    private boolean completada;          // Estado de la solicitud

    public SolicitudIO(TipoOperacion tipo, String nombreArchivo, int bloqueObjetivo) {
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.bloqueObjetivo = bloqueObjetivo;
        this.completada = false;
    }
}
```

**Tipos de Operaciones:**
```java
public enum TipoOperacion {
    CREAR_ARCHIVO,      // Crear nuevo archivo en disco
    LEER_ARCHIVO,       // Leer bloque de archivo
    ESCRIBIR_ARCHIVO,   // Escribir en bloque
    ELIMINAR_ARCHIVO,   // Eliminar archivo del disco
    LISTAR_DIRECTORIO   // Listar contenido de directorio
}
```

**Ejemplo de creación:**
```java
// Lectura de archivo
SolicitudIO solicitud = new SolicitudIO(
    TipoOperacion.LEER_ARCHIVO,
    "documento.txt",
    15  // Primer bloque del archivo
);
```

---

### 2. Process Control Block (`PCB`)

**Clase:** `modelo/PCB.java`

**Propósito:** Representa un proceso que solicita E/S.

**Estructura completa:**
```java
public class PCB {
    // Identificación
    private int idProceso;
    private String nombre;

    // Estado
    private EstadoProceso estado;  // NUEVO, LISTO, EJECUTANDO, BLOQUEADO, TERMINADO

    // Solicitud I/O asociada
    private SolicitudIO solicitudIO;

    // Métricas temporales
    private int tiempoLlegada;        // Ciclo en que llegó al sistema
    private int tiempoRespuesta;      // Primera ejecución - llegada
    private int tiempoEsperaTotal;    // Total de ciclos esperando
    private int tiempoRetorno;        // Terminación - llegada

    // Control de ejecución
    private int ciclosRestantesCPU;   // Ciclos que necesita en CPU antes de I/O
}
```

**Estados de Procesos:**
```java
public enum EstadoProceso {
    NUEVO,       // Proceso recién creado
    LISTO,       // Esperando CPU
    EJECUTANDO,  // Usando CPU
    BLOQUEADO,   // Esperando I/O
    TERMINADO    // Proceso finalizado
}
```

**Ciclo de vida de un PCB:**
```
[NUEVO]
   ↓ (Admisión)
[LISTO] ←──────────────┐
   ↓ (Despacho)        │
[EJECUTANDO]           │
   ↓ (Solicitud I/O)   │
[BLOQUEADO] ───────────┘
   ↓ (I/O completa)
[LISTO]
   ↓ (Sin más trabajo)
[TERMINADO]
```

**Métodos clave:**
```java
// Decrementar ciclos de CPU
public void decrementarCiclosCPU() {
    if (ciclosRestantesCPU > 0) {
        ciclosRestantesCPU--;
    }
}

// Verificar si completó fase de CPU
public boolean haCompletadoCPU() {
    return ciclosRestantesCPU <= 0;
}

// Registrar primera ejecución (para tiempo de respuesta)
public void registrarInicioEjecucion(int cicloActual) {
    if (this.tiempoRespuesta == -1) {
        this.tiempoRespuesta = cicloActual - this.tiempoLlegada;
    }
}
```

---

### 3. Colas del Sistema

**Ubicación:** `sistema/SO.java`

El sistema mantiene múltiples colas para gestionar procesos:

```java
public class SO {
    // Cola de procesos listos para ejecutar
    private Cola<PCB> colaListos;

    // Cola de procesos bloqueados esperando I/O
    private Cola<PCB> colaBloqueados;

    // Cola de solicitudes I/O pendientes
    private Cola<SolicitudIO> colaSolicitudesIO;

    // Cola de procesos terminados (para estadísticas)
    private Cola<PCB> colaTerminados;
}
```

**Función de cada cola:**

#### Cola de LISTOS
- **Propósito**: Procesos que esperan CPU
- **Entrada**: Procesos nuevos o desbloqueados
- **Salida**: Cuando CPU está disponible
- **Política**: FIFO

#### Cola de BLOQUEADOS
- **Propósito**: Procesos esperando que su I/O se complete
- **Entrada**: Procesos que generaron solicitud I/O
- **Salida**: Cuando I/O se completa
- **Política**: No hay selección (pasan a LISTOS al completar I/O)

#### Cola de Solicitudes I/O
- **Propósito**: Solicitudes de E/S pendientes de procesar
- **Entrada**: Cuando proceso genera solicitud I/O
- **Salida**: Planificador selecciona según algoritmo
- **Política**: FIFO, SSTF, SCAN o C-SCAN (configurable)

#### Cola de TERMINADOS
- **Propósito**: Almacenar procesos finalizados para estadísticas
- **Entrada**: Procesos que completaron todo su trabajo
- **Salida**: Exportación de estadísticas o limpieza

---

### 4. CPU Virtual

**Clase:** `modelo/CPU.java`

**Propósito:** Simula la CPU que ejecuta procesos.

**Estructura:**
```java
public class CPU {
    private PCB procesoActual;  // Proceso actualmente en ejecución

    public boolean estaOcupada() {
        return procesoActual != null;
    }

    public void asignarProceso(PCB proceso) {
        this.procesoActual = proceso;
        proceso.setEstado(EstadoProceso.EJECUTANDO);
    }

    public void liberarProceso() {
        this.procesoActual = null;
    }

    public PCB getProcesoActual() {
        return procesoActual;
    }
}
```

**Características:**
- **Monoprocesador**: Solo un proceso a la vez
- **No preventivo**: Proceso se ejecuta hasta completar ciclos de CPU
- **Contexto simple**: Solo mantiene referencia al proceso actual

---

### 5. Reloj del Sistema

**Clase:** `modelo/Reloj.java`

**Propósito:** Simula el paso del tiempo en el sistema.

**Estructura:**
```java
public class Reloj implements Runnable {
    private int cicloActual = 0;
    private boolean pausado = false;
    private int velocidad = 500;  // milisegundos por ciclo

    @Override
    public void run() {
        while (true) {
            if (!pausado) {
                cicloActual++;
                notificarCiclo();  // Notifica al SO para ejecutar ciclo
            }

            try {
                Thread.sleep(velocidad);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
```

**Funciones:**
- `getCicloActual()`: Retorna ciclo actual del sistema
- `pausar()`: Detiene el avance del tiempo
- `reanudar()`: Continúa el avance del tiempo
- `cambiarVelocidad(int ms)`: Ajusta velocidad de simulación

---

## Ciclo de Ejecución Detallado

### Método Principal: `ejecutarCiclo()`

**Ubicación:** `sistema/SO.java`

Este método se ejecuta cada ciclo del reloj y orquesta todo el sistema E/S.

```java
public void ejecutarCiclo() {
    // Bandera para evitar procesar I/O recién generada en el mismo ciclo
    boolean ioGeneradaEsteCiclo = false;

    // ============================================================
    // FASE 1: ASIGNACIÓN DE CPU
    // ============================================================
    if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
        PCB proceso = colaListos.desencolar();
        cpu.asignarProceso(proceso);
        proceso.setEstado(EstadoProceso.EJECUTANDO);
        proceso.registrarInicioEjecucion(reloj.getCicloActual());

        agregarLog(String.format("CPU asignada a: %s (necesita %d ciclos)",
            proceso.getNombre(),
            proceso.getCiclosRestantesCPU()));
    }

    // ============================================================
    // FASE 2: EJECUCIÓN EN CPU
    // ============================================================
    else if (cpu.estaOcupada()) {
        PCB proceso = cpu.getProcesoActual();

        // Ejecutar un ciclo de CPU
        proceso.decrementarCiclosCPU();

        agregarLog(String.format("Ejecutando: %s (ciclos restantes: %d)",
            proceso.getNombre(),
            proceso.getCiclosRestantesCPU()));

        // Verificar si completó la fase de CPU
        if (proceso.haCompletadoCPU()) {
            // Liberar CPU
            cpu.liberarProceso();

            // Generar solicitud I/O
            SolicitudIO solicitud = proceso.getSolicitudIO();
            colaSolicitudesIO.encolar(solicitud);

            // Bloquear proceso
            proceso.setEstado(EstadoProceso.BLOQUEADO);
            colaBloqueados.encolar(proceso);

            agregarLog(String.format("Solicitud I/O generada: %s - Proceso BLOQUEADO",
                solicitud.toString()));

            // Marcar que se generó I/O este ciclo
            ioGeneradaEsteCiclo = true;
        }
    }

    // ============================================================
    // FASE 3: PROCESAMIENTO DE I/O
    // ============================================================
    // Solo procesar I/O si NO se generó en este ciclo
    if (!colaSolicitudesIO.estaVacia() && !ioGeneradaEsteCiclo) {
        // El planificador selecciona la próxima solicitud
        SolicitudIO solicitud = planificador.seleccionarSiguiente(
            colaSolicitudesIO,
            cabezaDisco
        );

        if (solicitud != null) {
            // Remover de la cola
            colaSolicitudesIO.remove(solicitud);

            // Calcular distancia
            int bloqueObjetivo = solicitud.getBloqueObjetivo();
            int distancia = Math.abs(bloqueObjetivo - cabezaDisco);
            distanciaTotal += distancia;

            agregarLog(String.format("Planificador: Seleccionó bloque %d (distancia: %d)",
                bloqueObjetivo, distancia));

            // Mover cabezal
            cabezaDisco = bloqueObjetivo;

            // Ejecutar operación I/O
            boolean exito = ejecutarIO(solicitud);

            if (exito) {
                // Buscar el proceso asociado a esta solicitud
                PCB proceso = buscarProcesoPorSolicitud(solicitud);

                if (proceso != null) {
                    // Desbloquear proceso
                    proceso.setEstado(EstadoProceso.LISTO);
                    colaBloqueados.remove(proceso);

                    // Volver a cola de listos
                    colaListos.encolar(proceso);

                    agregarLog(String.format("I/O completa para: %s - Proceso LISTO",
                        proceso.getNombre()));
                }
            }
        }
    }

    // ============================================================
    // FASE 4: MOVER PROCESOS A TERMINADOS
    // ============================================================
    // Verificar procesos en LISTOS sin más trabajo
    for (PCB proceso : colaListos) {
        if (proceso.getSolicitudIO().estaCompletada()) {
            proceso.setEstado(EstadoProceso.TERMINADO);
            proceso.setTiempoRetorno(reloj.getCicloActual() - proceso.getTiempoLlegada());

            colaListos.remove(proceso);
            colaTerminados.encolar(proceso);

            agregarLog(String.format("Proceso TERMINADO: %s (Retorno: %d ciclos)",
                proceso.getNombre(),
                proceso.getTiempoRetorno()));
        }
    }

    // ============================================================
    // FASE 5: INCREMENTAR TIEMPOS DE ESPERA
    // ============================================================
    for (PCB proceso : colaListos) {
        proceso.incrementarTiempoEspera();
    }
}
```

### Detalles de Cada Fase

#### **FASE 1: Asignación de CPU**

**Condición:** CPU libre Y hay procesos listos

**Acciones:**
1. Desencolar primer proceso de cola LISTOS
2. Asignar proceso a CPU
3. Cambiar estado a EJECUTANDO
4. Registrar tiempo de respuesta (si es primera ejecución)
5. Log: "CPU asignada a: Proceso-X (necesita N ciclos)"

**Código:**
```java
if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
    PCB proceso = colaListos.desencolar();
    cpu.asignarProceso(proceso);
    proceso.setEstado(EstadoProceso.EJECUTANDO);
    proceso.registrarInicioEjecucion(reloj.getCicloActual());
    agregarLog("CPU asignada a: " + proceso.getNombre() +
               " (necesita " + proceso.getCiclosRestantesCPU() + " ciclos)");
}
```

**Por qué se usa `if` y no `else if`:**
- Para que en el **próximo ciclo** se ejecute la Fase 2
- Evita ejecutar el proceso en el mismo ciclo que se asigna

---

#### **FASE 2: Ejecución en CPU**

**Condición:** CPU ocupada

**Acciones:**
1. Obtener proceso actual de CPU
2. Decrementar ciclos restantes de CPU
3. Log: "Ejecutando: Proceso-X (ciclos restantes: N)"
4. Si completó ciclos de CPU:
   - Liberar CPU
   - Generar solicitud I/O
   - Cambiar estado a BLOQUEADO
   - Encolar en cola de bloqueados
   - Encolar solicitud en cola I/O
   - Marcar flag `ioGeneradaEsteCiclo = true`

**Código:**
```java
else if (cpu.estaOcupada()) {
    PCB proceso = cpu.getProcesoActual();
    proceso.decrementarCiclosCPU();

    agregarLog("Ejecutando: " + proceso.getNombre() +
               " (ciclos restantes: " + proceso.getCiclosRestantesCPU() + ")");

    if (proceso.haCompletadoCPU()) {
        cpu.liberarProceso();

        SolicitudIO solicitud = proceso.getSolicitudIO();
        colaSolicitudesIO.encolar(solicitud);

        proceso.setEstado(EstadoProceso.BLOQUEADO);
        colaBloqueados.encolar(proceso);

        agregarLog("Solicitud I/O generada: " + solicitud.toString() +
                   " - Proceso BLOQUEADO");

        ioGeneradaEsteCiclo = true;
    }
}
```

**Por qué se usa `else if`:**
- Para evitar asignar y ejecutar en el mismo ciclo
- Permite visualizar el estado EJECUTANDO en la GUI

**Por qué `ioGeneradaEsteCiclo = true`:**
- Evita que la Fase 3 procese esta solicitud en el mismo ciclo
- Permite visualizar el estado BLOQUEADO en la GUI

---

#### **FASE 3: Procesamiento de I/O**

**Condición:** Hay solicitudes I/O Y NO se generó I/O este ciclo

**Acciones:**
1. Planificador selecciona próxima solicitud (según algoritmo)
2. Remover solicitud de la cola
3. Calcular distancia = |bloqueObjetivo - cabezaActual|
4. Acumular distancia total (métrica)
5. Log: "Planificador: Seleccionó bloque X (distancia: Y)"
6. Mover cabezal a bloqueObjetivo
7. Ejecutar operación I/O (leer, escribir, etc.)
8. Buscar proceso asociado a la solicitud
9. Desbloquear proceso (BLOQUEADO → LISTO)
10. Remover de cola de bloqueados
11. Encolar en cola de listos
12. Log: "I/O completa para: Proceso-X - Proceso LISTO"

**Código:**
```java
if (!colaSolicitudesIO.estaVacia() && !ioGeneradaEsteCiclo) {
    SolicitudIO solicitud = planificador.seleccionarSiguiente(
        colaSolicitudesIO,
        cabezaDisco
    );

    if (solicitud != null) {
        colaSolicitudesIO.remove(solicitud);

        int bloqueObjetivo = solicitud.getBloqueObjetivo();
        int distancia = Math.abs(bloqueObjetivo - cabezaDisco);
        distanciaTotal += distancia;

        agregarLog("Planificador: Seleccionó bloque " + bloqueObjetivo +
                   " (distancia: " + distancia + ")");

        cabezaDisco = bloqueObjetivo;

        boolean exito = ejecutarIO(solicitud);

        if (exito) {
            PCB proceso = buscarProcesoPorSolicitud(solicitud);

            if (proceso != null) {
                proceso.setEstado(EstadoProceso.LISTO);
                colaBloqueados.remove(proceso);
                colaListos.encolar(proceso);

                agregarLog("I/O completa para: " + proceso.getNombre() +
                           " - Proceso LISTO");
            }
        }
    }
}
```

**Por qué la condición `!ioGeneradaEsteCiclo`:**
- Evita que la solicitud recién generada se procese en el mismo ciclo
- Permite que la GUI capture el estado BLOQUEADO

---

#### **FASE 4: Mover Procesos a TERMINADOS**

**Condición:** Proceso en LISTOS con solicitud I/O completada

**Acciones:**
1. Iterar por cola de LISTOS
2. Si solicitud I/O está completada:
   - Cambiar estado a TERMINADO
   - Calcular tiempo de retorno
   - Remover de cola de LISTOS
   - Encolar en cola de TERMINADOS
   - Log: "Proceso TERMINADO: Proceso-X (Retorno: Y ciclos)"

**Código:**
```java
for (PCB proceso : colaListos) {
    if (proceso.getSolicitudIO().estaCompletada()) {
        proceso.setEstado(EstadoProceso.TERMINADO);
        proceso.setTiempoRetorno(reloj.getCicloActual() - proceso.getTiempoLlegada());

        colaListos.remove(proceso);
        colaTerminados.encolar(proceso);

        agregarLog("Proceso TERMINADO: " + proceso.getNombre() +
                   " (Retorno: " + proceso.getTiempoRetorno() + " ciclos)");
    }
}
```

---

#### **FASE 5: Incrementar Tiempos de Espera**

**Condición:** Cada ciclo

**Acciones:**
1. Iterar por todos los procesos en cola de LISTOS
2. Incrementar su contador de tiempo de espera

**Código:**
```java
for (PCB proceso : colaListos) {
    proceso.incrementarTiempoEspera();
}
```

**Propósito:**
- Contabilizar ciclos que cada proceso espera en LISTO
- Métrica importante para evaluar rendimiento del sistema

---

### Método: `ejecutarIO(SolicitudIO solicitud)`

**Propósito:** Ejecuta una operación de E/S específica.

**Código completo:**
```java
private boolean ejecutarIO(SolicitudIO solicitud) {
    String nombreArchivo = solicitud.getNombreArchivo();
    boolean exito = true;
    String logMsg = "";

    switch (solicitud.getTipo()) {
        case LEER_ARCHIVO:
            // Validar bloque antes de leer
            if (solicitud.getBloqueObjetivo() < 0 ||
                solicitud.getBloqueObjetivo() >= disco.getTamanoTotal()) {

                System.err.println("ERROR: Bloque fuera de rango al leer '" +
                    nombreArchivo + "': " + solicitud.getBloqueObjetivo());
                exito = false;
                logMsg = String.format("Disco: FALLÓ lectura de '%s' - Bloque inválido: %d",
                    nombreArchivo, solicitud.getBloqueObjetivo());
                break;
            }

            // Leer a través del buffer cache
            int hitsAntes = cacheDisco.getCacheHits();
            Bloque bloqueLeido = cacheDisco.leerBloque(solicitud.getBloqueObjetivo());

            if (bloqueLeido == null) {
                System.err.println("ERROR: No se pudo leer bloque " +
                    solicitud.getBloqueObjetivo());
                exito = false;
                logMsg = String.format("Disco: FALLÓ lectura de '%s' - Bloque: %d",
                    nombreArchivo, solicitud.getBloqueObjetivo());
            } else {
                // Determinar si fue cache hit o miss
                boolean cacheHit = (cacheDisco.getCacheHits() > hitsAntes);

                logMsg = String.format("Disco: Leyendo '%s' - Bloque: %d (distancia: %d)%s",
                    nombreArchivo,
                    solicitud.getBloqueObjetivo(),
                    Math.abs(solicitud.getBloqueObjetivo() - cabezaDisco),
                    cacheHit ? " [CACHE HIT]" : " [CACHE MISS]"
                );
            }
            break;

        case CREAR_ARCHIVO:
            logMsg = String.format("Disco: Creando archivo '%s'", nombreArchivo);
            break;

        case ESCRIBIR_ARCHIVO:
            logMsg = String.format("Disco: Escribiendo en '%s' - Bloque: %d",
                nombreArchivo, solicitud.getBloqueObjetivo());
            break;

        case ELIMINAR_ARCHIVO:
            logMsg = String.format("Disco: Eliminando archivo '%s'", nombreArchivo);
            break;

        case LISTAR_DIRECTORIO:
            logMsg = String.format("Disco: Listando directorio '%s'", nombreArchivo);
            break;
    }

    agregarLog(logMsg);
    solicitud.setCompletada(true);
    return exito;
}
```

**Características:**
- **Validación**: Verifica índices de bloques
- **Integración con cache**: Todas las lecturas pasan por buffer cache
- **Detección de cache hits**: Compara contador antes/después
- **Logging detallado**: Registra cada operación con resultado
- **Manejo de errores**: Retorna `false` en caso de fallo

---

## Ejemplo Paso a Paso

### Caso: Usuario lee archivo "documento.txt" (5 bloques)

**Configuración inicial:**
- Archivo "documento.txt" ocupa bloques: 10, 23, 45, 67, 89
- Cabezal del disco en bloque 0
- Planificador: FIFO
- Cache: Vacío
- Velocidad: 500 ms por ciclo

---

### **Ciclo 0: Solicitud del Usuario**

**Acción del usuario:**
```
Usuario hace doble click en "documento.txt" en el JTree
```

**Código ejecutado (GUI):**
```java
arbolArchivos.addMouseListener(new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            // Obtener archivo seleccionado
            Archivo arch = (Archivo) nodo.getUserObject();

            // Solicitar lectura al sistema
            simulador.leerArchivoDirecto(arch);
        }
    }
});
```

**Código ejecutado (SO):**
```java
public void leerArchivoDirecto(Archivo archivo) {
    // Obtener primer bloque del archivo
    int bloqueObjetivo = archivo.getDireccionPrimerBloque();  // 10

    // Crear solicitud I/O
    SolicitudIO solicitud = new SolicitudIO(
        TipoOperacion.LEER_ARCHIVO,
        "documento.txt",
        bloqueObjetivo  // 10
    );

    // Crear PCB para manejar esta solicitud
    PCB proceso = new PCB(
        ++contadorProcesos,           // ID: 1
        "Proceso-1",                  // Nombre
        reloj.getCicloActual(),       // Tiempo llegada: 0
        solicitud                     // Solicitud asociada
    );

    // Procesos necesitan 2-4 ciclos de CPU antes de generar I/O
    proceso.setCiclosRestantesCPU(3);  // Aleatorio: 3 ciclos

    // Recibir solicitud
    recibirSolicitud(proceso);
}
```

**Método `recibirSolicitud()`:**
```java
public void recibirSolicitud(PCB proceso) {
    // Estado inicial: NUEVO
    proceso.setEstado(EstadoProceso.NUEVO);
    agregarLog("Proceso NUEVO: " + proceso.getNombre());

    // Admitir al sistema: NUEVO → LISTO
    proceso.setEstado(EstadoProceso.LISTO);
    colaListos.encolar(proceso);
    agregarLog("Proceso LISTO: " + proceso.getNombre());
}
```

**Estado del sistema al final del ciclo 0:**
```
Cola LISTOS: [Proceso-1 (3 ciclos CPU restantes)]
Cola BLOQUEADOS: []
Cola I/O: []
CPU: Libre
Cabezal: 0
Log:
  [Ciclo 0] Proceso NUEVO: Proceso-1
  [Ciclo 0] Proceso LISTO: Proceso-1
```

---

### **Ciclo 1: Asignación de CPU**

**Ejecución de `ejecutarCiclo()`:**

**FASE 1: Asignación de CPU**
```java
if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
    PCB proceso = colaListos.desencolar();  // Proceso-1
    cpu.asignarProceso(proceso);
    proceso.setEstado(EstadoProceso.EJECUTANDO);
    proceso.registrarInicioEjecucion(1);  // Tiempo respuesta = 1 - 0 = 1

    agregarLog("CPU asignada a: Proceso-1 (necesita 3 ciclos)");
}
```

**Estado del sistema al final del ciclo 1:**
```
Cola LISTOS: []
Cola BLOQUEADOS: []
Cola I/O: []
CPU: Proceso-1 (EJECUTANDO, 3 ciclos restantes)
Cabezal: 0
Log:
  [Ciclo 1] CPU asignada a: Proceso-1 (necesita 3 ciclos)
```

**GUI muestra:**
```
=== LISTOS ===
=== EN CPU ===
  Proceso-1 - EJECUCION
=== BLOQUEADOS ===
=== COLA I/O ===
```

---

### **Ciclo 2: Ejecución en CPU (ciclo 1/3)**

**FASE 2: Ejecución en CPU**
```java
else if (cpu.estaOcupada()) {
    PCB proceso = cpu.getProcesoActual();  // Proceso-1
    proceso.decrementarCiclosCPU();        // 3 → 2

    agregarLog("Ejecutando: Proceso-1 (ciclos restantes: 2)");

    if (proceso.haCompletadoCPU()) {  // false (2 > 0)
        // No entra aquí
    }
}
```

**Estado del sistema al final del ciclo 2:**
```
Cola LISTOS: []
Cola BLOQUEADOS: []
Cola I/O: []
CPU: Proceso-1 (EJECUTANDO, 2 ciclos restantes)
Cabezal: 0
Log:
  [Ciclo 2] Ejecutando: Proceso-1 (ciclos restantes: 2)
```

---

### **Ciclo 3: Ejecución en CPU (ciclo 2/3)**

**FASE 2: Ejecución en CPU**
```java
else if (cpu.estaOcupada()) {
    PCB proceso = cpu.getProcesoActual();  // Proceso-1
    proceso.decrementarCiclosCPU();        // 2 → 1

    agregarLog("Ejecutando: Proceso-1 (ciclos restantes: 1)");

    if (proceso.haCompletadoCPU()) {  // false (1 > 0)
        // No entra aquí
    }
}
```

**Estado del sistema al final del ciclo 3:**
```
Cola LISTOS: []
Cola BLOQUEADOS: []
Cola I/O: []
CPU: Proceso-1 (EJECUTANDO, 1 ciclo restante)
Cabezal: 0
Log:
  [Ciclo 3] Ejecutando: Proceso-1 (ciclos restantes: 1)
```

---

### **Ciclo 4: Ejecución en CPU (ciclo 3/3) - Generación de I/O**

**FASE 2: Ejecución en CPU**
```java
else if (cpu.estaOcupada()) {
    PCB proceso = cpu.getProcesoActual();  // Proceso-1
    proceso.decrementarCiclosCPU();        // 1 → 0

    agregarLog("Ejecutando: Proceso-1 (ciclos restantes: 0)");

    if (proceso.haCompletadoCPU()) {  // true (0 == 0)
        // Liberar CPU
        cpu.liberarProceso();

        // Generar solicitud I/O
        SolicitudIO solicitud = proceso.getSolicitudIO();
        colaSolicitudesIO.encolar(solicitud);

        // Bloquear proceso
        proceso.setEstado(EstadoProceso.BLOQUEADO);
        colaBloqueados.encolar(proceso);

        agregarLog("Solicitud I/O generada: LEER 'documento.txt' (bloque 10) - Proceso BLOQUEADO");

        ioGeneradaEsteCiclo = true;
    }
}
```

**FASE 3: NO se ejecuta** (porque `ioGeneradaEsteCiclo = true`)

**Estado del sistema al final del ciclo 4:**
```
Cola LISTOS: []
Cola BLOQUEADOS: [Proceso-1]
Cola I/O: [LEER documento.txt, bloque 10]
CPU: Libre
Cabezal: 0
ioGeneradaEsteCiclo: true
Log:
  [Ciclo 4] Ejecutando: Proceso-1 (ciclos restantes: 0)
  [Ciclo 4] Solicitud I/O generada: LEER 'documento.txt' (bloque 10) - Proceso BLOQUEADO
```

**GUI muestra:**
```
=== LISTOS ===
=== EN CPU ===
=== BLOQUEADOS ===
  Proceso-1 - BLOQUEADO
=== COLA I/O ===
  LEER documento.txt (bloque 10)
```

---

### **Ciclo 5: Procesamiento de I/O**

**FASE 1 y 2: NO se ejecutan** (CPU libre, cola LISTOS vacía)

**FASE 3: Procesamiento de I/O**
```java
if (!colaSolicitudesIO.estaVacia() && !ioGeneradaEsteCiclo) {  // true && true
    // Planificador FIFO selecciona primera solicitud
    SolicitudIO solicitud = planificador.seleccionarSiguiente(
        colaSolicitudesIO,  // [LEER documento.txt, bloque 10]
        cabezaDisco         // 0
    );
    // solicitud = LEER documento.txt, bloque 10

    colaSolicitudesIO.remove(solicitud);

    // Calcular distancia
    int bloqueObjetivo = solicitud.getBloqueObjetivo();  // 10
    int distancia = Math.abs(bloqueObjetivo - cabezaDisco);  // |10 - 0| = 10
    distanciaTotal += distancia;  // 0 + 10 = 10

    agregarLog("Planificador: Seleccionó bloque 10 (distancia: 10)");

    // Mover cabezal
    cabezaDisco = bloqueObjetivo;  // 0 → 10

    // Ejecutar operación I/O
    boolean exito = ejecutarIO(solicitud);
}
```

**Dentro de `ejecutarIO(solicitud)`:**
```java
private boolean ejecutarIO(SolicitudIO solicitud) {
    switch (solicitud.getTipo()) {
        case LEER_ARCHIVO:
            // Validar bloque
            if (solicitud.getBloqueObjetivo() < 0 ||
                solicitud.getBloqueObjetivo() >= 100) {
                // No entra (10 está en rango)
            }

            // Leer a través del cache
            int hitsAntes = cacheDisco.getCacheHits();  // 0
            Bloque bloqueLeido = cacheDisco.leerBloque(10);

            // Buffer Cache busca bloque 10
            // NO está en cache → CACHE MISS
            // Lee de disco
            // Añade a cache: [10]

            boolean cacheHit = (cacheDisco.getCacheHits() > hitsAntes);  // 0 > 0 = false

            logMsg = "Disco: Leyendo 'documento.txt' - Bloque: 10 (distancia: 10) [CACHE MISS]";
            break;
    }

    agregarLog(logMsg);
    solicitud.setCompletada(true);
    return true;
}
```

**De vuelta en `ejecutarCiclo()` - Desbloquear proceso:**
```java
if (exito) {
    PCB proceso = buscarProcesoPorSolicitud(solicitud);  // Proceso-1

    if (proceso != null) {
        proceso.setEstado(EstadoProceso.LISTO);
        colaBloqueados.remove(proceso);
        colaListos.encolar(proceso);

        agregarLog("I/O completa para: Proceso-1 - Proceso LISTO");
    }
}
```

**Estado del sistema al final del ciclo 5:**
```
Cola LISTOS: [Proceso-1]
Cola BLOQUEADOS: []
Cola I/O: []
CPU: Libre
Cabezal: 10
Cache: [10]
Cache Hits: 0
Cache Misses: 1
Tasa aciertos: 0%
Log:
  [Ciclo 5] Planificador: Seleccionó bloque 10 (distancia: 10)
  [Ciclo 5] Disco: Leyendo 'documento.txt' - Bloque: 10 (distancia: 10) [CACHE MISS]
  [Ciclo 5] I/O completa para: Proceso-1 - Proceso LISTO
```

**GUI muestra:**
```
=== LISTOS ===
  Proceso-1 - LISTO
=== EN CPU ===
=== BLOQUEADOS ===
=== COLA I/O ===

Cache:
  Bloque 10 (documento.txt)
  Hits: 0 | Misses: 1 | Tasa: 0.0%
```

---

### **Ciclo 6: Proceso Termina**

**FASE 1: Asignación de CPU**
```java
if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
    // CPU está libre, pero...
}
```

**FASE 4: Mover a TERMINADOS**
```java
for (PCB proceso : colaListos) {  // [Proceso-1]
    if (proceso.getSolicitudIO().estaCompletada()) {  // true
        proceso.setEstado(EstadoProceso.TERMINADO);
        proceso.setTiempoRetorno(6 - 0);  // 6 ciclos

        colaListos.remove(proceso);
        colaTerminados.encolar(proceso);

        agregarLog("Proceso TERMINADO: Proceso-1 (Retorno: 6 ciclos)");
    }
}
```

**Estado del sistema al final del ciclo 6:**
```
Cola LISTOS: []
Cola BLOQUEADOS: []
Cola I/O: []
Cola TERMINADOS: [Proceso-1]
CPU: Libre
Cabezal: 10
Log:
  [Ciclo 6] Proceso TERMINADO: Proceso-1 (Retorno: 6 ciclos)

Métricas de Proceso-1:
  - Tiempo de llegada: 0
  - Tiempo de respuesta: 1 (ciclo 1 - ciclo 0)
  - Tiempo de espera: 1 (1 ciclo en LISTO)
  - Tiempo de retorno: 6 (ciclo 6 - ciclo 0)
```

---

### Resumen del Flujo Completo

| Ciclo | Estado Proceso | Cola LISTOS | CPU | Cola BLOQUEADOS | Cola I/O | Cabezal | Acción |
|-------|---------------|-------------|-----|-----------------|----------|---------|--------|
| 0 | NUEVO → LISTO | [P-1] | Libre | [] | [] | 0 | Usuario solicita lectura |
| 1 | EJECUTANDO | [] | P-1 | [] | [] | 0 | Asignar a CPU |
| 2 | EJECUTANDO | [] | P-1 | [] | [] | 0 | Ejecutar (2 ciclos restantes) |
| 3 | EJECUTANDO | [] | P-1 | [] | [] | 0 | Ejecutar (1 ciclo restante) |
| 4 | BLOQUEADO | [] | Libre | [P-1] | [Leer bloque 10] | 0 | Generar I/O |
| 5 | LISTO | [P-1] | Libre | [] | [] | 10 | Procesar I/O (CACHE MISS) |
| 6 | TERMINADO | [] | Libre | [] | [] | 10 | Proceso termina |

**Ciclos totales:** 6
**Distancia recorrida:** 10 bloques
**Cache hits:** 0
**Cache misses:** 1

---

## Integración con Planificadores

### Selección de Solicitud I/O

**Interfaz común:**
```java
public interface Planificador {
    SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> cola, int cabezaActual);
    String getNombre();
}
```

**Invocación en `ejecutarCiclo()`:**
```java
SolicitudIO solicitud = planificador.seleccionarSiguiente(
    colaSolicitudesIO,
    cabezaDisco
);
```

### Ejemplo con SSTF

**Situación:**
```
Cabezal en: 50
Cola I/O: [Bloque 10, Bloque 55, Bloque 90, Bloque 52]
```

**Algoritmo SSTF:**
```java
public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> cola, int cabezaActual) {
    SolicitudIO masCercana = null;
    int menorDistancia = Integer.MAX_VALUE;

    for (SolicitudIO solicitud : cola) {
        int distancia = Math.abs(solicitud.getBloqueObjetivo() - cabezaActual);

        // Bloque 10: distancia = |10 - 50| = 40
        // Bloque 55: distancia = |55 - 50| = 5  ← MENOR
        // Bloque 90: distancia = |90 - 50| = 40
        // Bloque 52: distancia = |52 - 50| = 2  ← MENOR

        if (distancia < menorDistancia) {
            menorDistancia = distancia;
            masCercana = solicitud;
        }
    }

    cola.remove(masCercana);
    return masCercana;  // Retorna solicitud de Bloque 52
}
```

**Resultado:**
- Selecciona: Bloque 52 (distancia 2)
- Distancia total: 2 bloques
- Cabezal: 50 → 52

**Comparación con FIFO:**
- FIFO seleccionaría: Bloque 10 (primero en cola)
- Distancia total: 40 bloques
- Cabezal: 50 → 10

**Ahorro con SSTF:** 40 - 2 = 38 bloques (95% menos)

---

## Integración con Buffer Cache

### Flujo de Lectura con Cache

**Cada lectura pasa por el cache:**
```java
// En ejecutarIO()
Bloque bloqueLeido = cacheDisco.leerBloque(indiceBloque);
```

**Dentro de `BufferCache.leerBloque()`:**
```java
public Bloque leerBloque(int indiceBloque) {
    // 1. Buscar en cache
    Bloque bloqueEnCache = buscarEnCache(indiceBloque);

    if (bloqueEnCache != null) {
        // CACHE HIT: Bloque encontrado en memoria
        cacheHits++;
        return bloqueEnCache;
    } else {
        // CACHE MISS: Debe leer del disco
        cacheMisses++;

        // Leer del disco físico
        Bloque bloqueDeDisco = discoDuro.getBloques()[indiceBloque];

        // Agregar al cache
        agregarAlCache(indiceBloque, bloqueDeDisco);

        return bloqueDeDisco;
    }
}
```

### Ejemplo con Múltiples Lecturas

**Archivo "video.mp4" con 3 bloques: 10, 20, 30**

**Primera lectura completa:**
```
Leer bloque 10:
  - Cache: []
  - Buscar en cache: NO encontrado
  - CACHE MISS
  - Leer de disco
  - Agregar a cache: [10]
  - Hits: 0, Misses: 1

Leer bloque 20:
  - Cache: [10]
  - Buscar en cache: NO encontrado
  - CACHE MISS
  - Leer de disco
  - Agregar a cache: [10, 20]
  - Hits: 0, Misses: 2

Leer bloque 30:
  - Cache: [10, 20]
  - Buscar en cache: NO encontrado
  - CACHE MISS
  - Leer de disco
  - Agregar a cache: [10, 20, 30]
  - Hits: 0, Misses: 3

Tasa de aciertos: 0/3 = 0%
```

**Segunda lectura del mismo archivo (localidad temporal):**
```
Leer bloque 10:
  - Cache: [10, 20, 30]
  - Buscar en cache: ✓ ENCONTRADO
  - CACHE HIT
  - Retornar de memoria
  - Hits: 1, Misses: 3

Leer bloque 20:
  - Cache: [10, 20, 30]
  - Buscar en cache: ✓ ENCONTRADO
  - CACHE HIT
  - Retornar de memoria
  - Hits: 2, Misses: 3

Leer bloque 30:
  - Cache: [10, 20, 30]
  - Buscar en cache: ✓ ENCONTRADO
  - CACHE HIT
  - Retornar de memoria
  - Hits: 3, Misses: 3

Tasa de aciertos: 3/6 = 50%
```

**Tercera lectura del mismo archivo:**
```
Leer bloque 10: CACHE HIT (Hits: 4, Misses: 3)
Leer bloque 20: CACHE HIT (Hits: 5, Misses: 3)
Leer bloque 30: CACHE HIT (Hits: 6, Misses: 3)

Tasa de aciertos: 6/9 = 66.7%
```

**Beneficio:**
- **Primera lectura**: 3 accesos a disco
- **Segunda lectura**: 0 accesos a disco (100% desde cache)
- **Tercera lectura**: 0 accesos a disco (100% desde cache)
- **Total**: 3 accesos a disco en vez de 9 (ahorro del 66.7%)

---

## Diagramas de Secuencia

### Diagrama 1: Lectura de Archivo Completa

```
Usuario          GUI              SO              Planificador    Cache         Disco
  |              |                |                    |            |             |
  |--dobleClick->|                |                    |            |             |
  |              |--leerArchivo-->|                    |            |             |
  |              |                |--crearPCB--------->|            |             |
  |              |                |--nuevaEntrada----->|            |             |
  |              |<---confirma----|                    |            |             |
  |              |                |                    |            |             |
  |    [Ciclo 1: Asignar CPU]    |                    |            |             |
  |              |                |--asignarCPU------->|            |             |
  |              |                |                    |            |             |
  |    [Ciclos 2-4: Ejecutar]    |                    |            |             |
  |              |                |--ejecutarCPU------>|            |             |
  |              |                |                    |            |             |
  |    [Ciclo 4: Generar I/O]    |                    |            |             |
  |              |                |--generarIO-------->|            |             |
  |              |                |--bloquearProceso-->|            |             |
  |              |                |                    |            |             |
  |    [Ciclo 5: Procesar I/O]   |                    |            |             |
  |              |                |--seleccionarIO---->|            |             |
  |              |                |<--solicitud--------|            |             |
  |              |                |--moverCabezal----->|            |             |
  |              |                |--leerBloque--------|----------->|             |
  |              |                |                    |--buscarCache            |
  |              |                |                    |  [MISS]    |             |
  |              |                |                    |--leerDisco------------->|
  |              |                |                    |<--bloque----------------|
  |              |                |                    |--agregarCache           |
  |              |                |<--bloque-----------|            |             |
  |              |                |--desbloquear------>|            |             |
  |              |                |                    |            |             |
  |    [Ciclo 6: Terminar]       |                    |            |             |
  |              |                |--terminarProceso-->|            |             |
  |              |<--actualizar---|                    |            |             |
  |<--mostrar----|                |                    |            |             |
```

---

## Conclusión

El sistema de E/S implementado simula fielmente el comportamiento de un sistema operativo real con las siguientes características:

### Componentes Principales
1. **Solicitudes I/O**: Encapsulan operaciones de disco
2. **PCB**: Representan procesos con estados completos
3. **Colas**: Organizan procesos según estado
4. **CPU Virtual**: Ejecuta procesos
5. **Reloj**: Controla el tiempo del sistema

### Flujo de Operaciones
1. Usuario solicita operación
2. Sistema crea PCB y solicitud I/O
3. Proceso pasa por estados: NUEVO → LISTO → EJECUTANDO → BLOQUEADO → LISTO → TERMINADO
4. Cada transición está claramente separada en ciclos distintos
5. Planificador optimiza orden de acceso a disco
6. Buffer cache reduce accesos físicos a disco

### Características Destacadas
- **Visibilidad de estados**: Cada estado es observable en la GUI
- **Separación de fases**: Evita procesamiento en mismo ciclo
- **Métricas completas**: Tiempos de respuesta, espera y retorno
- **Integración completa**: Planificadores + Cache + Procesos
- **Logging detallado**: Cada acción registrada

Este diseño permite comprender visualmente cómo un SO maneja operaciones de E/S, desde la solicitud del usuario hasta la finalización del proceso.
