# Informe Completo de Funciones del Sistema de Archivos y Planificación de Disco

## Información del Proyecto

**Nombre:** Simulador de Sistema de Archivos con Planificación de Disco
**Curso:** Sistemas Operativos 2425-2
**Lenguaje:** Java (JDK 11+)
**Framework GUI:** Java Swing
**Paradigma:** Programación Orientada a Objetos con patrones de diseño

---

## 1. Gestión del Sistema de Archivos

### 1.1 Creación de Archivos

**Función:** `crearArchivo(String nombre, int tamano, Directorio directorio, String propietario)`
**Ubicación:** `FS/SistemaArchivos.java`

**Descripción:**
Crea un nuevo archivo en el sistema con asignación encadenada de bloques.

**Algoritmo:**
1. Valida que el nombre no esté duplicado en el directorio
2. Verifica disponibilidad de bloques libres en el disco
3. Busca bloques libres no contiguos usando `SD.buscarBloquesLibres()`
4. Enlaza los bloques mediante punteros `siguienteBloque`
5. Asigna color aleatorio RGB para visualización
6. Crea objeto `Archivo` con metadatos
7. Añade el archivo al directorio padre

**Parámetros:**
- `nombre`: Nombre del archivo (String)
- `tamano`: Cantidad de bloques requeridos (int, 1-20)
- `directorio`: Directorio padre donde se creará
- `propietario`: Usuario propietario (String, ej: "admin")

**Retorno:** `Archivo` creado o `null` si falla

**Ejemplo de uso:**
```java
Archivo nuevoArchivo = sistemaArchivos.crearArchivo(
    "documento.txt",
    5,                      // 5 bloques
    directorioRaiz,
    "admin"
);
```

**Características implementadas:**
- **Asignación encadenada**: Bloques enlazados mediante punteros
- **Validación de espacio**: Verifica bloques libres antes de crear
- **Metadatos completos**: Nombre, tamaño, propietario, color, primer bloque
- **Color único**: RGB aleatorio para visualización en disco

**Código relevante:**
```java
public Archivo crearArchivo(String nombre, int tamano, Directorio directorio, String propietario) {
    // Validar duplicados
    for (Object hijo : directorio.getHijos()) {
        if (hijo instanceof Archivo && ((Archivo)hijo).getNombre().equals(nombre)) {
            return null;
        }
    }

    // Buscar bloques libres
    ListaEnlazada<Integer> bloquesLibres = disco.buscarBloquesLibres(tamano);
    if (bloquesLibres == null) {
        return null;
    }

    // Asignar bloques en cadena
    Integer primerBloque = null;
    Integer bloqueAnterior = null;

    for (Integer numBloque : bloquesLibres) {
        Bloque bloque = bloques[numBloque];
        bloque.setEstaLibre(false);
        bloque.setArchivoPropietario(nombre);

        if (primerBloque == null) {
            primerBloque = numBloque;
        }

        if (bloqueAnterior != null) {
            bloques[bloqueAnterior].setSiguienteBloque(numBloque);
        }

        bloqueAnterior = numBloque;
    }

    // Crear archivo
    Archivo archivo = new Archivo(nombre, tamano, primerBloque, propietario, colorAleatorio());
    directorio.agregarHijo(archivo);
    return archivo;
}
```

---

### 1.2 Creación de Directorios

**Función:** `crearDirectorio(String nombre, Directorio padre)`
**Ubicación:** `FS/SistemaArchivos.java`

**Descripción:**
Crea un nuevo directorio en la jerarquía del sistema de archivos.

**Algoritmo:**
1. Valida que el nombre no esté duplicado
2. Crea objeto `Directorio` con referencia al padre
3. Añade el directorio al padre como hijo

**Parámetros:**
- `nombre`: Nombre del directorio (String)
- `padre`: Directorio padre (Directorio)

**Retorno:** `Directorio` creado o `null` si falla

**Características:**
- **Jerarquía de árbol**: Estructura padre-hijo
- **Navegación bidireccional**: Cada directorio conoce su padre
- **Validación de duplicados**: No permite nombres repetidos

**Ejemplo de uso:**
```java
Directorio documentos = sistemaArchivos.crearDirectorio("documentos", directorioRaiz);
Directorio imagenes = sistemaArchivos.crearDirectorio("imagenes", directorioRaiz);
```

---

### 1.3 Eliminación de Archivos

**Función:** `eliminarArchivo(String nombre, Directorio directorio)`
**Ubicación:** `FS/SistemaArchivos.java`

**Descripción:**
Elimina un archivo del sistema, liberando sus bloques en el disco.

**Algoritmo:**
1. Busca el archivo por nombre en el directorio
2. Obtiene la dirección del primer bloque
3. Libera todos los bloques de la cadena usando `SD.liberarBloques()`
4. Remueve el archivo del directorio padre

**Proceso de liberación de bloques:**
```java
public void liberarBloques(int primerBloque) {
    int bloqueActual = primerBloque;

    while (bloqueActual != -1) {
        Bloque bloque = bloques[bloqueActual];
        int siguiente = bloque.getSiguienteBloque();

        // Limpiar bloque
        bloque.setEstaLibre(true);
        bloque.setArchivoPropietario(null);
        bloque.setSiguienteBloque(-1);

        bloquesLibres++;
        bloqueActual = siguiente;
    }
}
```

**Características:**
- **Liberación completa**: Sigue la cadena hasta el final (-1)
- **Actualización de contador**: Incrementa bloques libres
- **Limpieza de metadatos**: Resetea propietario y punteros

---

### 1.4 Eliminación de Directorios

**Función:** `eliminarDirectorio(String nombre, Directorio padre)`
**Ubicación:** `FS/SistemaArchivos.java`

**Descripción:**
Elimina recursivamente un directorio y todo su contenido.

**Algoritmo:**
1. Busca el directorio por nombre
2. Elimina recursivamente todos los hijos (archivos y subdirectorios)
3. Remueve el directorio del padre

**Características:**
- **Eliminación recursiva**: Borra todo el subárbol
- **Liberación de bloques**: Libera espacio de todos los archivos internos
- **Protección de raíz**: No permite eliminar el directorio raíz

---

### 1.5 Renombrado de Archivos/Directorios

**Función:** `renombrar(String nombreActual, String nuevoNombre, Directorio directorio)`
**Ubicación:** `FS/SistemaArchivos.java`

**Descripción:**
Cambia el nombre de un archivo o directorio.

**Algoritmo:**
1. Busca el elemento por nombre actual
2. Valida que el nuevo nombre no esté duplicado
3. Actualiza el nombre en el objeto
4. Si es archivo, actualiza `archivoPropietario` en todos sus bloques

**Características:**
- **Actualización de bloques**: Mantiene consistencia en metadatos de disco
- **Validación de duplicados**: Evita conflictos de nombres
- **Soporte para archivos y directorios**

---

### 1.6 Búsqueda de Archivos

**Función:** `buscarArchivo(String nombre, Directorio directorio)`
**Ubicación:** `FS/SistemaArchivos.java`

**Descripción:**
Busca un archivo por nombre en un directorio específico o recursivamente en toda la jerarquía.

**Algoritmo (recursivo):**
```java
private Archivo buscarArchivoRecursivo(Directorio dir, String nombre) {
    for (Object hijo : dir.getHijos()) {
        if (hijo instanceof Archivo) {
            Archivo arch = (Archivo) hijo;
            if (arch.getNombre().equals(nombre)) {
                return arch;
            }
        } else if (hijo instanceof Directorio) {
            Archivo encontrado = buscarArchivoRecursivo((Directorio) hijo, nombre);
            if (encontrado != null) {
                return encontrado;
            }
        }
    }
    return null;
}
```

**Características:**
- **Búsqueda recursiva**: Explora toda la jerarquía
- **Búsqueda local**: Solo en directorio especificado
- **Retorno directo**: Devuelve objeto `Archivo` completo

---

## 2. Gestión del Disco Duro (SD)

### 2.1 Inicialización del Disco

**Función:** `SD(int tamanoTotal)`
**Ubicación:** `modelo/SD.java`

**Descripción:**
Crea un disco virtual con un número especificado de bloques.

**Algoritmo:**
1. Crea array de objetos `Bloque`
2. Inicializa cada bloque como libre
3. Establece contador de bloques libres = tamaño total

**Parámetros:**
- `tamanoTotal`: Número de bloques del disco (típicamente 100)

**Estructura de datos:**
```java
public class SD {
    private Bloque[] bloques;
    private int tamanoTotal;
    private int bloquesLibres;
}
```

---

### 2.2 Asignación de Bloques

**Función:** `buscarBloquesLibres(int cantidad)`
**Ubicación:** `modelo/SD.java`

**Descripción:**
Busca bloques libres no contiguos para asignación encadenada.

**Algoritmo:**
1. Itera por el array de bloques
2. Añade índices de bloques libres a una lista
3. Retorna lista cuando alcanza la cantidad requerida

**Características:**
- **No requiere contigüidad**: Bloques pueden estar dispersos
- **Primera disponibilidad**: Toma los primeros bloques libres encontrados
- **Retorno null**: Si no hay suficientes bloques libres

**Código:**
```java
public ListaEnlazada<Integer> buscarBloquesLibres(int cantidad) {
    if (bloquesLibres < cantidad) {
        return null;
    }

    ListaEnlazada<Integer> bloquesEncontrados = new ListaEnlazada<>();

    for (int i = 0; i < bloques.length && bloquesEncontrados.size() < cantidad; i++) {
        if (bloques[i].isEstaLibre()) {
            bloquesEncontrados.add(i);
        }
    }

    bloquesLibres -= cantidad;
    return bloquesEncontrados;
}
```

---

### 2.3 Liberación de Bloques

**Función:** `liberarBloques(int primerBloque)`
**Ubicación:** `modelo/SD.java`

**Descripción:**
Libera una cadena completa de bloques asignados a un archivo.

**Algoritmo:**
1. Comienza en el primer bloque
2. Sigue la cadena mediante `siguienteBloque`
3. Marca cada bloque como libre
4. Limpia metadatos (propietario, puntero)
5. Incrementa contador de bloques libres
6. Continúa hasta encontrar -1 (fin de cadena)

**Características:**
- **Liberación completa**: No deja bloques huérfanos
- **Actualización de contador**: Mantiene estadísticas precisas
- **Prevención de ciclos**: Termina en -1

---

## 3. Gestión de Procesos

### 3.1 Creación de Procesos

**Función:** `recibirSolicitud(PCB proceso)`
**Ubicación:** `sistema/SO.java`

**Descripción:**
Recibe una solicitud de I/O y crea un proceso para manejarla.

**Algoritmo:**
1. Establece estado inicial NUEVO
2. Cambia a estado LISTO
3. Encola en `colaListos`
4. Registra tiempo de llegada

**Estructura de PCB:**
```java
public class PCB {
    private int idProceso;
    private String nombre;
    private EstadoProceso estado;
    private SolicitudIO solicitudIO;

    // Métricas temporales
    private int tiempoLlegada;
    private int tiempoRespuesta;
    private int tiempoEsperaTotal;
    private int tiempoRetorno;

    // Control de ejecución
    private int ciclosRestantesCPU;  // 2-4 ciclos antes de I/O
}
```

---

### 3.2 Ciclo de Ejecución de Procesos

**Función:** `ejecutarCiclo()`
**Ubicación:** `sistema/SO.java`

**Descripción:**
Ciclo principal del simulador que maneja transiciones de estados de procesos.

**Algoritmo detallado:**

#### Fase 1: Asignación de CPU
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

#### Fase 2: Ejecución en CPU
```java
else if (cpu.estaOcupada()) {
    PCB proceso = cpu.getProcesoActual();
    proceso.decrementarCiclosCPU();

    agregarLog("Ejecutando: " + proceso.getNombre() +
               " (ciclos restantes: " + proceso.getCiclosRestantesCPU() + ")");

    // Verificar si completó fase de CPU
    if (proceso.haCompletadoCPU()) {
        cpu.liberarProceso();

        // Generar solicitud I/O
        SolicitudIO solicitud = proceso.getSolicitudIO();
        colaSolicitudesIO.encolar(solicitud);

        // Pasar a estado BLOQUEADO
        proceso.setEstado(EstadoProceso.BLOQUEADO);
        colaBloqueados.encolar(proceso);

        agregarLog("Solicitud I/O generada: " + solicitud.toString() +
                   " - Proceso BLOQUEADO");

        ioGeneradaEsteCiclo = true;  // Evitar procesar en mismo ciclo
    }
}
```

#### Fase 3: Procesamiento de I/O
```java
if (!colaSolicitudesIO.estaVacia() && !ioGeneradaEsteCiclo) {
    SolicitudIO solicitud = planificador.seleccionarSiguiente(
        colaSolicitudesIO,
        cabezaDisco
    );

    if (solicitud != null) {
        colaSolicitudesIO.remove(solicitud);

        // Calcular distancia
        int bloqueObjetivo = solicitud.getBloqueObjetivo();
        int distancia = Math.abs(bloqueObjetivo - cabezaDisco);
        distanciaTotal += distancia;

        // Mover cabezal
        cabezaDisco = bloqueObjetivo;

        // Ejecutar operación I/O
        boolean exito = ejecutarIO(solicitud);

        // Desbloquear proceso
        PCB proceso = buscarProcesoPorSolicitud(solicitud);
        if (proceso != null) {
            proceso.setEstado(EstadoProceso.LISTO);
            colaBloqueados.remove(proceso);
            colaListos.encolar(proceso);
        }
    }
}
```

**Características clave:**
- **Separación de fases**: Evita procesamiento en mismo ciclo
- **Flag `ioGeneradaEsteCiclo`**: Previene condiciones de carrera
- **Métricas automáticas**: Calcula distancia, tiempos, etc.

---

### 3.3 Estados de Procesos

**Transiciones implementadas:**

```
NUEVO → LISTO → EJECUTANDO → BLOQUEADO → LISTO → ... → TERMINADO
```

#### 1. NUEVO → LISTO
**Evento:** Solicitud recibida
**Acción:** `proceso.setEstado(EstadoProceso.LISTO)`

#### 2. LISTO → EJECUTANDO
**Evento:** CPU disponible
**Acción:**
- Desencolar de `colaListos`
- Asignar a CPU
- Registrar tiempo de respuesta (si es primera ejecución)

#### 3. EJECUTANDO → BLOQUEADO
**Evento:** Proceso completó ciclos de CPU
**Acción:**
- Liberar CPU
- Generar solicitud I/O
- Encolar en `colaBloqueados` y `colaSolicitudesIO`

#### 4. BLOQUEADO → LISTO
**Evento:** I/O completada
**Acción:**
- Remover de `colaBloqueados`
- Encolar en `colaListos`
- Cambiar estado a LISTO

#### 5. LISTO → TERMINADO
**Evento:** Proceso sin más trabajo
**Acción:**
- Marcar como TERMINADO
- Mover a `colaTerminados`
- Calcular tiempo de retorno

---

### 3.4 Métricas de Procesos

#### Tiempo de Respuesta
```java
public void registrarInicioEjecucion(int cicloActual) {
    if (this.tiempoRespuesta == -1) {
        this.tiempoRespuesta = cicloActual - this.tiempoLlegada;
    }
}
```

#### Tiempo de Espera
```java
public void incrementarTiempoEspera() {
    if (estado == EstadoProceso.LISTO) {
        tiempoEsperaTotal++;
    }
}
```

#### Tiempo de Retorno
```java
public void setEstadoTerminado(int cicloActual) {
    this.estado = EstadoProceso.TERMINADO;
    this.tiempoRetorno = cicloActual - this.tiempoLlegada;
}
```

**Fórmulas:**
- **Tiempo de Respuesta** = Primera ejecución - Llegada
- **Tiempo de Espera** = Suma de ciclos en estado LISTO
- **Tiempo de Retorno** = Terminación - Llegada

---

## 4. Planificadores de Disco

### 4.1 FIFO (First In, First Out)

**Interfaz:** `Planificador`
**Ubicación:** `planificacion/FIFO.java`

**Descripción:**
Atiende solicitudes en orden estricto de llegada.

**Algoritmo:**
```java
public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> cola, int cabezaActual) {
    if (cola.estaVacia()) return null;
    return cola.desencolar();  // Primera en la cola
}
```

**Complejidad:** O(1)

**Características:**
- **Simple**: Un solo desencolar
- **Justo**: No hay inanición
- **Ineficiente**: No optimiza movimientos

---

### 4.2 SSTF (Shortest Seek Time First)

**Ubicación:** `planificacion/SSTF.java`

**Descripción:**
Selecciona la solicitud con menor distancia al cabezal actual.

**Algoritmo:**
```java
public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> cola, int cabezaActual) {
    if (cola.estaVacia()) return null;

    SolicitudIO masCercana = null;
    int menorDistancia = Integer.MAX_VALUE;

    for (SolicitudIO solicitud : cola) {
        int distancia = Math.abs(solicitud.getBloqueObjetivo() - cabezaActual);
        if (distancia < menorDistancia) {
            menorDistancia = distancia;
            masCercana = solicitud;
        }
    }

    cola.remove(masCercana);
    return masCercana;
}
```

**Complejidad:** O(n)

**Características:**
- **Eficiente**: Minimiza distancia total (greedy)
- **Inanición posible**: Solicitudes lejanas pueden esperar indefinidamente

---

### 4.3 SCAN (Algoritmo del Ascensor)

**Ubicación:** `planificacion/SCAN.java`

**Descripción:**
Mueve el cabezal en una dirección, atendiendo solicitudes en el camino. Invierte dirección al llegar al extremo.

**Algoritmo:**
```java
public class SCAN implements Planificador {
    private boolean direccionAscendente = true;

    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> cola, int cabezaActual) {
        if (cola.estaVacia()) return null;

        SolicitudIO seleccionada = null;
        int menorDistancia = Integer.MAX_VALUE;

        if (direccionAscendente) {
            // Buscar la más cercana hacia ARRIBA (bloque >= cabezaActual)
            for (SolicitudIO s : cola) {
                if (s.getBloqueObjetivo() >= cabezaActual) {
                    int dist = s.getBloqueObjetivo() - cabezaActual;
                    if (dist < menorDistancia) {
                        menorDistancia = dist;
                        seleccionada = s;
                    }
                }
            }

            // Si no hay hacia arriba, cambiar dirección
            if (seleccionada == null) {
                direccionAscendente = false;
                return seleccionarSiguiente(cola, cabezaActual);
            }
        } else {
            // Similar para dirección DESCENDENTE
            // Buscar la más cercana hacia ABAJO (bloque <= cabezaActual)
            for (SolicitudIO s : cola) {
                if (s.getBloqueObjetivo() <= cabezaActual) {
                    int dist = cabezaActual - s.getBloqueObjetivo();
                    if (dist < menorDistancia) {
                        menorDistancia = dist;
                        seleccionada = s;
                    }
                }
            }

            // Si no hay hacia abajo, cambiar dirección
            if (seleccionada == null) {
                direccionAscendente = true;
                return seleccionarSiguiente(cola, cabezaActual);
            }
        }

        cola.remove(seleccionada);
        return seleccionada;
    }
}
```

**Complejidad:** O(n)

**Características:**
- **Bidireccional**: Sube y baja
- **No hay inanición**: Todas las solicitudes se atienden
- **Tiempo acotado**: Máximo = 2 × tamaño del disco

---

### 4.4 C-SCAN (Circular SCAN)

**Ubicación:** `planificacion/CSCAN.java`

**Descripción:**
Solo atiende en dirección ascendente. Al llegar al final, regresa al inicio sin atender solicitudes.

**Algoritmo:**
```java
public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> cola, int cabezaActual) {
    if (cola.estaVacia()) return null;

    SolicitudIO seleccionada = null;
    int menorDistancia = Integer.MAX_VALUE;

    // Buscar la más cercana hacia ARRIBA
    for (SolicitudIO s : cola) {
        if (s.getBloqueObjetivo() >= cabezaActual) {
            int dist = s.getBloqueObjetivo() - cabezaActual;
            if (dist < menorDistancia) {
                menorDistancia = dist;
                seleccionada = s;
            }
        }
    }

    // Si no hay hacia arriba, buscar desde el inicio (wrap-around)
    if (seleccionada == null) {
        for (SolicitudIO s : cola) {
            int dist = s.getBloqueObjetivo();
            if (dist < menorDistancia) {
                menorDistancia = dist;
                seleccionada = s;
            }
        }
    }

    cola.remove(seleccionada);
    return seleccionada;
}
```

**Complejidad:** O(n)

**Características:**
- **Unidireccional**: Solo ascendente
- **Tiempo uniforme**: No favorece ninguna zona
- **Circular**: Comportamiento de "cinta transportadora"

---

## 5. Buffer Cache (FIFO)

### 5.1 Lectura con Cache

**Función:** `leerBloque(int indiceBloque)`
**Ubicación:** `FS/BufferCache.java`

**Descripción:**
Lee un bloque desde cache (si está) o desde disco (si no está).

**Algoritmo:**
```java
public Bloque leerBloque(int indiceBloque) {
    // Validar índice
    if (indiceBloque < 0 || indiceBloque >= discoDuro.getTamanoTotal()) {
        System.err.println("ERROR BufferCache: Índice fuera de rango: " + indiceBloque);
        return null;
    }

    // Buscar en cache
    Bloque bloqueEnCache = buscarEnCache(indiceBloque);

    if (bloqueEnCache != null) {
        // CACHE HIT: Bloque encontrado en memoria
        cacheHits++;
        return bloqueEnCache;
    } else {
        // CACHE MISS: Leer del disco
        cacheMisses++;
        Bloque bloqueDeDisco = discoDuro.getBloques()[indiceBloque];

        // Agregar al cache
        agregarAlCache(indiceBloque, bloqueDeDisco);

        return bloqueDeDisco;
    }
}
```

**Características:**
- **Transparente**: Usuario no sabe si lee de cache o disco
- **Métricas automáticas**: Contabiliza hits y misses
- **Validación**: Verifica índices antes de acceder

---

### 5.2 Gestión del Cache

**Función:** `agregarAlCache(int indiceBloque, Bloque bloque)`
**Ubicación:** `FS/BufferCache.java`

**Descripción:**
Añade un bloque al cache, aplicando política FIFO si está lleno.

**Algoritmo:**
```java
private void agregarAlCache(int indiceBloque, Bloque bloque) {
    // Si el cache está lleno, reemplazar el más antiguo
    if (cacheContenido.size() >= tamanoMaximo) {
        EntradaCache entradaAReemplazar = cacheFIFO.desencolar();
        cacheContenido.remove(entradaAReemplazar);
    }

    // Añadir nueva entrada
    EntradaCache nuevaEntrada = new EntradaCache(indiceBloque, bloque);
    cacheFIFO.encolar(nuevaEntrada);
    cacheContenido.add(nuevaEntrada);
}
```

**Estructura de datos:**
```java
public class BufferCache {
    private Cola<EntradaCache> cacheFIFO;               // Orden de llegada
    private ListaEnlazada<EntradaCache> cacheContenido; // Para búsqueda
    private int tamanoMaximo = 10;                      // Capacidad
}
```

**Características:**
- **Política FIFO**: Reemplaza el más antiguo
- **Doble estructura**: Cola (orden) + Lista (búsqueda)
- **Capacidad fija**: 10 bloques

---

### 5.3 Métricas de Rendimiento

**Función:** `getEstadisticas()`
**Ubicación:** `FS/BufferCache.java`

**Descripción:**
Calcula y retorna estadísticas de rendimiento del cache.

**Implementación:**
```java
public double getTasaAciertos() {
    int total = getTotalAccesos();
    if (total == 0) return 0.0;
    return (double) cacheHits / total * 100.0;
}

public String getEstadisticas() {
    return String.format("Hits: %d | Misses: %d | Total: %d | Tasa: %.1f%%",
            cacheHits, cacheMisses, getTotalAccesos(), getTasaAciertos());
}
```

**Métricas disponibles:**
- **Cache Hits**: Accesos encontrados en cache
- **Cache Misses**: Accesos que requirieron disco
- **Total Accesos**: Hits + Misses
- **Tasa de Aciertos**: (Hits / Total) × 100%

---

## 6. Operaciones de E/S

### 6.1 Tipos de Operaciones

**Enumeración:** `TipoOperacion`
**Ubicación:** `modelo/TipoOperacion.java`

```java
public enum TipoOperacion {
    CREAR_ARCHIVO,
    LEER_ARCHIVO,
    ESCRIBIR_ARCHIVO,
    ELIMINAR_ARCHIVO,
    LISTAR_DIRECTORIO
}
```

---

### 6.2 Solicitud de I/O

**Clase:** `SolicitudIO`
**Ubicación:** `modelo/SolicitudIO.java`

**Estructura:**
```java
public class SolicitudIO {
    private TipoOperacion tipo;
    private String nombreArchivo;
    private int bloqueObjetivo;
    private boolean completada;
}
```

**Uso:**
```java
SolicitudIO solicitud = new SolicitudIO(
    TipoOperacion.LEER_ARCHIVO,
    "documento.txt",
    primerBloque
);
```

---

### 6.3 Ejecución de I/O

**Función:** `ejecutarIO(SolicitudIO solicitud)`
**Ubicación:** `sistema/SO.java`

**Descripción:**
Ejecuta una operación de I/O según su tipo.

**Algoritmo:**
```java
private boolean ejecutarIO(SolicitudIO solicitud) {
    String nombreArchivo = solicitud.getNombreArchivo();
    boolean exito = true;
    String logMsg = "";

    switch (solicitud.getTipo()) {
        case LEER_ARCHIVO:
            // Validar bloque
            if (solicitud.getBloqueObjetivo() < 0 ||
                solicitud.getBloqueObjetivo() >= disco.getTamanoTotal()) {
                exito = false;
                logMsg = "ERROR: Bloque inválido";
                break;
            }

            // Leer a través del cache
            int hitsAntes = cacheDisco.getCacheHits();
            Bloque bloqueLeido = cacheDisco.leerBloque(solicitud.getBloqueObjetivo());

            if (bloqueLeido == null) {
                exito = false;
                logMsg = "ERROR: No se pudo leer bloque";
            } else {
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
            // Lógica de creación...
            break;

        // Otros casos...
    }

    agregarLog(logMsg);
    solicitud.setCompletada(true);
    return exito;
}
```

**Características:**
- **Validación**: Verifica integridad antes de ejecutar
- **Integración con cache**: Todas las lecturas pasan por buffer cache
- **Logging detallado**: Registra cada operación y su resultado

---

## 7. Persistencia de Datos

### 7.1 Guardar Estado del Sistema

**Función:** `guardarEstado(SistemaArchivos, SD, String ruta)`
**Ubicación:** `Persistencia/PersistenciaJSON.java`

**Descripción:**
Exporta el estado completo del sistema a un archivo JSON.

**Formato JSON:**
```json
{
  "disco": {
    "tamanoTotal": 100,
    "bloquesLibres": 70,
    "bloques": [
      {"indice": 0, "libre": false, "siguiente": 5, "propietario": "archivo.txt"},
      {"indice": 1, "libre": true, "siguiente": -1, "propietario": null},
      ...
    ]
  },
  "raiz": {
    "nombre": "raiz",
    "tipo": "directorio",
    "hijos": [
      {
        "nombre": "documento.txt",
        "tipo": "archivo",
        "tamano": 3,
        "primerBloque": 0,
        "propietario": "admin",
        "colorR": 100,
        "colorG": 149,
        "colorB": 237
      },
      {
        "nombre": "imagenes",
        "tipo": "directorio",
        "hijos": [...]
      }
    ]
  }
}
```

**Algoritmo:**
1. Guardar metadatos del disco (tamaño, bloques libres)
2. Guardar estado de cada bloque (libre, puntero, propietario)
3. Guardar estructura de directorios recursivamente
4. Guardar metadatos de cada archivo (nombre, tamaño, color, propietario)

---

### 7.2 Cargar Estado del Sistema

**Función:** `cargarEstado(SistemaArchivos, SD, String ruta)`
**Ubicación:** `Persistencia/PersistenciaJSON.java`

**Descripción:**
Restaura el estado completo desde un archivo JSON.

**Algoritmo:**
1. **Limpiar estado actual**:
   - Eliminar todos los archivos y subdirectorios
   - Liberar todos los bloques ocupados

2. **Parsear JSON manualmente**:
   - Buscar sección "raiz"
   - Extraer estructura de directorios

3. **Recrear jerarquía**:
   - Procesar recursivamente cada directorio
   - Crear archivos con metadatos originales
   - Asignar bloques dinámicamente

**Código de limpieza:**
```java
private static void limpiarDirectorio(Directorio dir, SistemaArchivos sa, SD disco) {
    ListaEnlazada<Object> hijos = dir.getHijos();

    // Copiar para evitar modificación durante iteración
    Object[] hijosCopia = new Object[hijos.size()];
    int i = 0;
    for (Object hijo : hijos) {
        hijosCopia[i++] = hijo;
    }

    // Eliminar cada hijo
    for (Object hijo : hijosCopia) {
        if (hijo instanceof Archivo) {
            Archivo arch = (Archivo) hijo;
            disco.liberarBloques(arch.getDireccionPrimerBloque());
            dir.eliminarHijo(arch);
        } else if (hijo instanceof Directorio) {
            limpiarDirectorio((Directorio) hijo, sa, disco);
            dir.eliminarHijo(hijo);
        }
    }
}
```

---

### 7.3 Exportar Estadísticas

**Función:** `guardarResultadosProcesos(Cola<PCB>, String ruta)`
**Ubicación:** `Persistencia/PersistenciaJSON.java`

**Descripción:**
Exporta métricas de procesos terminados a JSON.

**Formato:**
```json
[
  {
    "id": 1,
    "nombre": "Proceso-1",
    "estado": "TERMINADO",
    "tiempoLlegada": 0,
    "tiempoEspera": 5,
    "tiempoRespuesta": 2,
    "tiempoRetorno": 12
  },
  ...
]
```

---

### 7.4 Exportar Resumen del Sistema

**Función:** `exportarResumen(SistemaArchivos, SD, String ruta)`
**Ubicación:** `Persistencia/PersistenciaJSON.java`

**Descripción:**
Genera un reporte de texto con el estado del sistema.

**Formato:**
```
========================================
RESUMEN DEL SISTEMA DE ARCHIVOS
========================================

DISCO:
  Tamaño total: 100 bloques
  Bloques libres: 70
  Bloques ocupados: 30
  Uso: 30.0%

ESTRUCTURA DE DIRECTORIOS:
  [DIR] raiz
    [FILE] readme.txt (3 bloques)
    [DIR] documentos
      [FILE] informe.docx (8 bloques)
      [FILE] notas.txt (3 bloques)
    [DIR] imagenes
      [FILE] foto.jpg (6 bloques)

TABLA DE ASIGNACIÓN:
  ARCHIVO              BLOQUES   PRIMER BLOQUE   PROPIETARIO
  ------------------------------------------------------------------
  readme.txt                 3              0     admin
  informe.docx               8             10     admin
  notas.txt                  3             25     admin
  foto.jpg                   6             40     admin
```

---

## 8. Interfaz Gráfica (GUI)

### 8.1 Componentes Principales

**Clase:** `Ventana_Principal`
**Ubicación:** `GUI/Ventana_Principal.java`

**Componentes Swing:**

#### Panel de Controles
```java
- JRadioButton rbAdministrador, rbUsuario  // Modo de usuario
- JComboBox<String> comboPlanificador      // Selección de planificador
- JButton btnCrearArchivo                  // Crear archivo
- JButton btnCrearDirectorio               // Crear directorio
- JButton btnLeer                          // Leer archivo
- JButton btnRenombrar                     // Renombrar elemento
- JButton btnEliminar                      // Eliminar elemento
- JButton btnPausar                        // Pausar/reanudar simulación
- JSlider sliderVelocidad                  // Control de velocidad (50-2000 ms)
```

#### Árbol de Archivos
```java
- JTree arbolArchivos                      // Jerarquía de directorios
- DefaultTreeModel modeloArbol             // Modelo del árbol
```

#### Pestañas Principales
```java
- "Simulación de Disco": Visualización gráfica del disco
- "Tabla de Asignación": Tabla con todos los archivos
- "Cache": Estado del buffer cache
```

#### Panel de Log
```java
- JTextArea areaLog                        // Eventos del sistema
```

#### Panel de Procesos
```java
- JList<String> listaProcesos              // Estados de procesos
  * === LISTOS ===
  * === EN CPU ===
  * === BLOQUEADOS ===
  * === COLA I/O ===
```

---

### 8.2 Visualización del Disco

**Función:** `dibujarDisco(Graphics g)`
**Ubicación:** `GUI/Ventana_Principal.java`

**Descripción:**
Dibuja representación gráfica del disco con bloques coloreados.

**Algoritmo:**
```java
protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    SD disco = simulador.getDisco();
    Bloque[] bloques = disco.getBloques();

    int cols = 10;
    int tamBloque = 40;
    int espaciado = 5;

    for (int i = 0; i < bloques.length; i++) {
        int fila = i / cols;
        int col = i % cols;
        int x = col * (tamBloque + espaciado);
        int y = fila * (tamBloque + espaciado);

        Bloque bloque = bloques[i];

        // Color según estado
        Color colorBloque;
        if (bloque.isEstaLibre()) {
            colorBloque = Color.GRAY;
        } else {
            colorBloque = obtenerColorArchivo(bloque.getArchivoPropietario());
        }

        // Dibujar bloque
        g.setColor(colorBloque);
        g.fillRoundRect(x, y, tamBloque, tamBloque, 8, 8);

        // Borde
        g.setColor(Color.DARK_GRAY);
        g.drawRoundRect(x, y, tamBloque, tamBloque, 8, 8);

        // Número de bloque
        g.setColor(bloque.isEstaLibre() ? Color.DARK_GRAY : Color.WHITE);
        g.drawString(String.valueOf(i), x + 15, y + 25);

        // Indicador de siguiente bloque
        if (!bloque.isEstaLibre() && bloque.getSiguienteBloque() != -1) {
            g.drawString("→" + bloque.getSiguienteBloque(), x + 2, y + tamBloque - 3);
        }

        // Indicador de cabezal (rectángulo rojo)
        if (i == simulador.getCabezaDisco()) {
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(x - 2, y - 2, tamBloque + 4, tamBloque + 4, 10, 10);
        }
    }
}
```

**Características visuales:**
- **Bloques libres**: Gris claro
- **Bloques ocupados**: Color del archivo propietario
- **Cabezal del disco**: Rectángulo rojo grueso
- **Punteros**: Flecha "→N" indicando siguiente bloque
- **Grid**: 10 columnas × N filas

---

### 8.3 Actualización de GUI

**Función:** `actualizarGUI()`
**Ubicación:** `GUI/Ventana_Principal.java`

**Descripción:**
Sincroniza la interfaz con el estado del sistema (ejecutado por Timer cada 200 ms).

**Algoritmo:**
```java
private void actualizarGUI() {
    SwingUtilities.invokeLater(() -> {
        // 1. Actualizar árbol de archivos
        actualizarArbol();

        // 2. Redibujar disco
        panelDisco.repaint();

        // 3. Actualizar tabla de asignación
        actualizarTablaAsignacion();

        // 4. Actualizar log
        String log = simulador.getLog();
        if (!log.equals(areaLog.getText())) {
            areaLog.setText(log);
            areaLog.setCaretPosition(areaLog.getDocument().getLength());
        }

        // 5. Actualizar cola de procesos
        actualizarColaProcesos();

        // 6. Actualizar cache
        actualizarCache();

        // 7. Actualizar labels
        labelCiclo.setText("Ciclo: " + simulador.getReloj().getCicloActual());
        labelCabeza.setText("Cabeza: " + simulador.getCabezaDisco());
        labelEstadoDisco.setText("Bloques libres: " +
            simulador.getDisco().getBloquesLibres() + "/" +
            simulador.getDisco().getTamanoTotal());
    });
}
```

**Frecuencia:** Timer de 200 ms (5 actualizaciones por segundo)

---

### 8.4 Eventos de Usuario

#### Doble Click en Árbol
```java
arbolArchivos.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            TreePath path = arbolArchivos.getPathForLocation(e.getX(), e.getY());
            if (path != null) {
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) path.getLastPathComponent();
                Object obj = nodo.getUserObject();

                if (obj instanceof Archivo) {
                    Archivo arch = (Archivo) obj;
                    simulador.leerArchivoDirecto(arch);
                } else if (obj instanceof Directorio) {
                    // Expandir/colapsar directorio
                    if (arbolArchivos.isExpanded(path)) {
                        arbolArchivos.collapsePath(path);
                    } else {
                        arbolArchivos.expandPath(path);
                    }
                }
            }
        }
    }
});
```

#### Cambio de Planificador
```java
comboPlanificador.addActionListener(e -> {
    String seleccion = (String) comboPlanificador.getSelectedItem();
    Planificador nuevo;
    switch (seleccion) {
        case "SSTF":  nuevo = new SSTF(); break;
        case "SCAN":  nuevo = new SCAN(); break;
        case "C-SCAN": nuevo = new CSCAN(); break;
        default:      nuevo = new FIFO();
    }
    simulador.cambiarPlanificador(nuevo);
});
```

#### Control de Velocidad
```java
sliderVelocidad.addChangeListener(e -> {
    int velocidad = sliderVelocidad.getValue();
    simulador.cambiarVelocidad(velocidad);
    labelVelocidad.setText(velocidad + " ms");
});
```

---

## 9. Modo de Usuario

### 9.1 Cambio de Modo

**Función:** `cambiarModo(boolean esAdmin)`
**Ubicación:** `GUI/Ventana_Principal.java`

**Descripción:**
Activa/desactiva funciones según el modo (Administrador o Usuario).

**Implementación:**
```java
private void cambiarModo(boolean esAdmin) {
    modoAdministrador = esAdmin;

    // Modo Usuario: SOLO LECTURA
    // Modo Administrador: TODAS LAS OPERACIONES
    btnCrearArchivo.setEnabled(esAdmin);
    btnCrearDirectorio.setEnabled(esAdmin);
    btnEliminar.setEnabled(esAdmin);
    btnRenombrar.setEnabled(esAdmin);
    btnEstadisticas.setEnabled(esAdmin);

    // Lectura permitida en ambos modos (no hay btnLeer en GUI, se usa doble click)
}
```

**Restricciones:**

| Operación          | Administrador | Usuario |
|--------------------|---------------|---------|
| Crear archivo      | ✓             | ✗       |
| Crear directorio   | ✓             | ✗       |
| Leer archivo       | ✓             | ✓       |
| Renombrar          | ✓             | ✗       |
| Eliminar           | ✓             | ✗       |
| Ver estadísticas   | ✓             | ✗       |
| Cargar/Guardar     | ✓             | ✓       |

---

### 9.2 Validación de Permisos

**Ejemplo de validación en operaciones:**
```java
private void crearArchivo() {
    if (!modoAdministrador) {
        JOptionPane.showMessageDialog(this,
            "Operación no permitida en modo Usuario.\nModo Usuario: SOLO LECTURA",
            "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Continuar con lógica de creación...
}
```

**Características:**
- **Validación en GUI**: Botones deshabilitados
- **Validación en lógica**: Verificación adicional en métodos
- **Mensajes claros**: Indicación de permisos insuficientes

---

## 10. Reloj del Sistema

### 10.1 Ciclo de Simulación

**Clase:** `Reloj`
**Ubicación:** `modelo/Reloj.java`

**Estructura:**
```java
public class Reloj implements Runnable {
    private int cicloActual = 0;
    private boolean pausado = false;
    private int velocidad = 500;  // ms por ciclo

    @Override
    public void run() {
        while (true) {
            if (!pausado) {
                cicloActual++;
                notificarCiclo();
            }
            Thread.sleep(velocidad);
        }
    }
}
```

**Funciones:**
- `getCicloActual()`: Retorna ciclo actual
- `pausar()`: Detiene avance del reloj
- `reanudar()`: Continúa avance
- `cambiarVelocidad(int ms)`: Ajusta velocidad de simulación

---

## 11. Estructuras de Datos Personalizadas

### 11.1 Cola (Queue)

**Ubicación:** `estructura_datos/Cola.java`

**Implementación:**
```java
public class Cola<T> implements Iterable<T> {
    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamano = 0;

    public void encolar(T elemento) {
        Nodo<T> nuevoNodo = new Nodo<>(elemento);
        if (estaVacia()) {
            frente = fin = nuevoNodo;
        } else {
            fin.siguiente = nuevoNodo;
            fin = nuevoNodo;
        }
        tamano++;
    }

    public T desencolar() {
        if (estaVacia()) return null;
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) fin = null;
        tamano--;
        return dato;
    }

    public boolean estaVacia() {
        return frente == null;
    }
}
```

**Complejidad:**
- `encolar()`: O(1)
- `desencolar()`: O(1)
- `estaVacia()`: O(1)

---

### 11.2 Lista Enlazada

**Ubicación:** `estructura_datos/ListaEnlazada.java`

**Implementación:**
```java
public class ListaEnlazada<T> implements Iterable<T> {
    private Nodo<T> cabeza;
    private int tamano = 0;

    public void add(T elemento) {
        Nodo<T> nuevoNodo = new Nodo<>(elemento);
        if (cabeza == null) {
            cabeza = nuevoNodo;
        } else {
            Nodo<T> actual = cabeza;
            while (actual.siguiente != null) {
                actual = actual.siguiente;
            }
            actual.siguiente = nuevoNodo;
        }
        tamano++;
    }

    public void remove(T elemento) {
        if (cabeza == null) return;

        if (cabeza.dato.equals(elemento)) {
            cabeza = cabeza.siguiente;
            tamano--;
            return;
        }

        Nodo<T> actual = cabeza;
        while (actual.siguiente != null) {
            if (actual.siguiente.dato.equals(elemento)) {
                actual.siguiente = actual.siguiente.siguiente;
                tamano--;
                return;
            }
            actual = actual.siguiente;
        }
    }
}
```

**Complejidad:**
- `add()`: O(n)
- `remove()`: O(n)
- `size()`: O(1)

---

## 12. Resumen de Características Implementadas

### Funciones Principales

✓ **Sistema de Archivos**
- Creación de archivos con asignación encadenada
- Creación de directorios jerárquicos
- Eliminación de archivos y directorios (recursiva)
- Renombrado de elementos
- Búsqueda recursiva de archivos

✓ **Gestión de Disco**
- Asignación dinámica de bloques no contiguos
- Liberación de bloques siguiendo cadenas
- Metadatos por bloque (libre, propietario, puntero)
- Contador de bloques libres

✓ **Gestión de Procesos**
- Estados completos: NUEVO, LISTO, EJECUTANDO, BLOQUEADO, TERMINADO
- Métricas: Tiempo de respuesta, espera, retorno
- PCB completo con control de ejecución

✓ **Planificadores de Disco**
- FIFO: Orden de llegada
- SSTF: Menor distancia (greedy)
- SCAN: Ascensor bidireccional
- C-SCAN: Ascensor circular

✓ **Buffer Cache**
- Política FIFO de reemplazo
- Capacidad: 10 bloques
- Métricas: Hits, Misses, Tasa de aciertos
- Transparente al usuario

✓ **Modos de Usuario**
- Administrador: Todas las operaciones
- Usuario: Solo lectura
- Validación en GUI y lógica

✓ **Persistencia**
- Guardar/Cargar estado completo (JSON)
- Exportar estadísticas de procesos (JSON)
- Exportar resumen del sistema (TXT)

✓ **Interfaz Gráfica**
- Visualización gráfica del disco con colores
- Árbol jerárquico de directorios
- Tabla de asignación de bloques
- Panel de estados de procesos
- Log de eventos en tiempo real
- Control de velocidad de simulación (50-2000 ms)
- Pausar/reanudar simulación

---

## 13. Tecnologías y Patrones Utilizados

### Patrones de Diseño
- **MVC (Model-View-Controller)**: Separación de capas
- **Strategy**: Planificadores intercambiables
- **Observer**: Actualización de GUI con Timer
- **Singleton**: Instancia única del simulador

### Estructuras de Datos
- Cola enlazada (FIFO)
- Lista enlazada simple
- Árbol N-ario (directorios)
- Array de objetos (bloques del disco)

### Concurrencia
- Thread principal del reloj
- Timer de actualización de GUI
- Sincronización con `SwingUtilities.invokeLater()`

### Validación y Manejo de Errores
- Validación de índices de bloques
- Verificación de espacio disponible
- Prevención de nombres duplicados
- Manejo de casos borde (null, vacío, fuera de rango)

---

## Conclusión

Este proyecto implementa un **simulador completo de sistema de archivos** con las siguientes características principales:

1. **Asignación encadenada de bloques** sin fragmentación externa
2. **4 planificadores de disco** comparables en rendimiento
3. **Gestión completa de procesos** con 5 estados y métricas temporales
4. **Buffer cache FIFO** con métricas de rendimiento
5. **Modos de usuario** con restricciones de permisos
6. **Persistencia JSON** para guardar/cargar configuraciones
7. **Interfaz gráfica completa** con visualización en tiempo real

El sistema simula fielmente los conceptos teóricos de sistemas operativos descritos en el libro "Sistemas Operativos Modernos" de Tanenbaum, proporcionando una herramienta educativa visual e interactiva para comprender:
- Gestión de I/O
- Planificación de disco
- Sistemas de archivos
- Estados de procesos
- Técnicas de caching
