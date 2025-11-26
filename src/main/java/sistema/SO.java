package sistema;

import estructura_datos.Cola;
import modelo.PCB;
import modelo.EstadoProceso;
import modelo.SolicitudIO;
import modelo.SD;
import modelo.TipoOperacion;
import FS.SistemaArchivos; 
import planificacion.Planificador; 
import planificacion.FIFO; 
import cpu.CPU;
import cpu.Reloj;
import modelo.Directorio;
import modelo.Archivo;
import FS.BufferCache;
import modelo.Bloque;


/**
 *
 * @author Diego A. Vivolo
 */

public class SO implements Runnable {
 
    private Reloj reloj;
    private CPU cpu;
    private SistemaArchivos sistemaArchivos;
    private SD disco;
    private Directorio directorioActual;
    private Directorio directorioTemporal;  // Para operaciones que requieren directorio específico
    private BufferCache cacheDisco;

    private Cola<PCB> colaListos;
    private Cola<PCB> colaBloqueados;
    private Cola<PCB> colaTerminados;
    private Cola<SolicitudIO> colaSolicitudesIO;

    private Planificador planificadorDisco;
    private int cabezaDiscoActual;
    private boolean direccionSubida;

    private boolean ejecutando;
    private Thread hiloSimulacion;
    private StringBuilder logEventos;
    private int contadorProcesos;

    // Control de tiempo para operaciones I/O en ejecución
    private SolicitudIO solicitudIOEnEjecucion;  // Solicitud actualmente ejecutándose
    private int ciclosRestantesIO;               // Ciclos que faltan para completar I/O

    public SO(int tamanoDisco, int duracionCicloMs) {
        this.reloj = new Reloj(duracionCicloMs);
        this.cpu = new CPU();
        
        this.disco = new SD(tamanoDisco);
        this.cacheDisco = new BufferCache(10, this.disco);
        
        this.sistemaArchivos = new SistemaArchivos(disco);
        this.directorioActual = this.sistemaArchivos.getDirectorioRaiz();
        
        
        this.colaListos = new Cola<>();
        this.colaBloqueados = new Cola<>();
        this.colaTerminados = new Cola<>();
        this.colaSolicitudesIO = new Cola<>();
        
        this.planificadorDisco = new FIFO();
        this.cabezaDiscoActual = 0;
        this.direccionSubida = true;

        this.logEventos = new StringBuilder();
        this.ejecutando = false;
        this.contadorProcesos = 0;

        // Inicializar control de I/O
        this.solicitudIOEnEjecucion = null;
        this.ciclosRestantesIO = 0;

        // Precarga de datos de ejemplo
        precargarDatos();
    }

    private void precargarDatos() {
        agregarLog("Precargando datos de ejemplo...");

        // Crear directorios
        sistemaArchivos.crearDirectorio("documentos", directorioActual);
        sistemaArchivos.crearDirectorio("imagenes", directorioActual);
        sistemaArchivos.crearDirectorio("proyectos", directorioActual);

        // Crear archivos en raíz
        sistemaArchivos.crearArchivo("readme.txt", 2, directorioActual);
        sistemaArchivos.crearArchivo("config.ini", 1, directorioActual);

        // Archivos en subdirectorios
        Directorio docs = (Directorio) directorioActual.buscarHijo("documentos");
        if (docs != null) {
            sistemaArchivos.crearArchivo("informe.docx", 5, docs);
            sistemaArchivos.crearArchivo("notas.txt", 2, docs);
        }

        Directorio imgs = (Directorio) directorioActual.buscarHijo("imagenes");
        if (imgs != null) {
            sistemaArchivos.crearArchivo("foto1.jpg", 4, imgs);
            sistemaArchivos.crearArchivo("logo.png", 3, imgs);
        }

        agregarLog("Precarga completada. Sistema listo.");
    }
    
    // Método para crear archivo desde la GUI
    public void crearArchivoDesdeGUI(String nombre, int tamanoEnBloques) {
        crearArchivoDesdeGUI(nombre, tamanoEnBloques, "admin");
    }

    // Método para crear archivo desde la GUI con propietario
    public void crearArchivoDesdeGUI(String nombre, int tamanoEnBloques, String propietario) {
        int bloqueObjetivo = (int)(Math.random() * disco.getTamanoTotal());
        SolicitudIO solicitud = new SolicitudIO(TipoOperacion.CREAR_ARCHIVO, nombre, tamanoEnBloques, bloqueObjetivo, propietario);
        PCB proceso = new PCB(++contadorProcesos, "Proceso-" + contadorProcesos, reloj.getCicloActual(), solicitud);
        recibirSolicitud(proceso);
    }
    
    // Método para crear directorio desde la GUI
    public void crearDirectorioDesdeGUI(String nombre) {
        int bloqueObjetivo = 0;
        SolicitudIO solicitud = new SolicitudIO(TipoOperacion.CREAR_DIRECTORIO, nombre, bloqueObjetivo);
        PCB proceso = new PCB(++contadorProcesos, "Proceso-" + contadorProcesos, reloj.getCicloActual(), solicitud);
        recibirSolicitud(proceso);
    }
    
    // Método para eliminar desde la GUI
    public void eliminarDesdeGUI(String nombre, boolean esArchivo) {
        Object hijo = directorioActual.buscarHijo(nombre);
        int bloqueObjetivo = 0;
        if (esArchivo && hijo instanceof Archivo) {
            bloqueObjetivo = ((Archivo) hijo).getDireccionPrimerBloque();
        }
        TipoOperacion tipo = esArchivo ? TipoOperacion.ELIMINAR_ARCHIVO : TipoOperacion.ELIMINAR_DIRECTORIO;
        SolicitudIO solicitud = new SolicitudIO(tipo, nombre, bloqueObjetivo);
        PCB proceso = new PCB(++contadorProcesos, "Proceso-" + contadorProcesos, reloj.getCicloActual(), solicitud);
        recibirSolicitud(proceso);
    }
    
    // Método para renombrar desde la GUI
    public void renombrarDesdeGUI(String nombreActual, String nuevoNombre) {
        Object hijo = directorioActual.buscarHijo(nombreActual);
        int bloqueObjetivo = 0;
        if (hijo instanceof Archivo) {
            bloqueObjetivo = ((Archivo) hijo).getDireccionPrimerBloque();
        }
        SolicitudIO solicitud = new SolicitudIO(TipoOperacion.ACTUALIZAR_ARCHIVO, nombreActual, nuevoNombre, bloqueObjetivo);
        PCB proceso = new PCB(++contadorProcesos, "Proceso-" + contadorProcesos, reloj.getCicloActual(), solicitud);
        recibirSolicitud(proceso);
    }

    // Método para renombrar directamente (recibe el objeto y el directorio padre)
    public void renombrarDirecto(Object elemento, Directorio padre, String nuevoNombre) {
        if (elemento == null) {
            System.err.println("ERROR: Elemento nulo al renombrar");
            agregarLog("ERROR: Elemento nulo al renombrar");
            return;
        }

        if (padre == null) {
            System.err.println("ERROR: Directorio padre nulo al renombrar");
            agregarLog("ERROR: Directorio padre nulo al renombrar");
            return;
        }

        String nombreActual = "";
        int bloqueObjetivo = 0;

        if (elemento instanceof Archivo) {
            Archivo archivo = (Archivo) elemento;
            nombreActual = archivo.getNombre();
            bloqueObjetivo = archivo.getDireccionPrimerBloque();

            System.out.println("DEBUG: Renombrando archivo '" + nombreActual + "' a '" + nuevoNombre + "' en directorio '" + padre.getNombre() + "'");
        } else if (elemento instanceof Directorio) {
            Directorio directorio = (Directorio) elemento;
            nombreActual = directorio.getNombre();
            bloqueObjetivo = 0; // Directorios no tienen bloques

            System.out.println("DEBUG: Renombrando directorio '" + nombreActual + "' a '" + nuevoNombre + "' en directorio '" + padre.getNombre() + "'");
        } else {
            System.err.println("ERROR: Tipo de elemento desconocido al renombrar");
            agregarLog("ERROR: Tipo de elemento desconocido al renombrar");
            return;
        }

        // Crear solicitud I/O con el nombre actual y nuevo nombre
        SolicitudIO solicitud = new SolicitudIO(TipoOperacion.ACTUALIZAR_ARCHIVO, nombreActual, nuevoNombre, bloqueObjetivo);

        // Guardar el directorio padre en una variable temporal para que ejecutarIO lo use
        directorioTemporal = padre;

        PCB proceso = new PCB(++contadorProcesos, "Proceso-" + contadorProcesos, reloj.getCicloActual(), solicitud);
        recibirSolicitud(proceso);
    }
    
    // Método para leer archivo directamente (recibe el objeto Archivo)
    public void leerArchivoDirecto(Archivo archivo) {
        System.out.println("DEBUG: Leyendo archivo directo: " + archivo.getNombre());

        int bloqueObjetivo = archivo.getDireccionPrimerBloque();

        System.out.println("DEBUG: Primer bloque: " + bloqueObjetivo);
        System.out.println("DEBUG: Tamaño del archivo: " + archivo.getTamanoEnBloques() + " bloques");

        if (bloqueObjetivo < 0) {
            System.err.println("ERROR: Bloque inválido para archivo '" + archivo.getNombre() + "': " + bloqueObjetivo);
            agregarLog("ERROR: Archivo '" + archivo.getNombre() + "' tiene bloque inválido: " + bloqueObjetivo);
            return;
        }

        SolicitudIO solicitud = new SolicitudIO(TipoOperacion.LEER_ARCHIVO, archivo.getNombre(), bloqueObjetivo);
        PCB proceso = new PCB(++contadorProcesos, "Proceso-" + contadorProcesos, reloj.getCicloActual(), solicitud);
        recibirSolicitud(proceso);

        System.out.println("DEBUG: Proceso creado: " + proceso.getNombre());
    }

    // Método para leer archivo desde la GUI (por nombre en directorio actual)
    public void leerArchivoDesdeGUI(String nombre) {
        System.out.println("DEBUG: Intentando leer archivo: " + nombre);
        System.out.println("DEBUG: Directorio actual: " + directorioActual.getNombre());

        Object hijo = directorioActual.buscarHijo(nombre);

        if (hijo == null) {
            System.err.println("ERROR: Archivo '" + nombre + "' no encontrado en directorio '" + directorioActual.getNombre() + "'");
            agregarLog("ERROR: Archivo '" + nombre + "' no encontrado");
            return;
        }

        if (hijo instanceof Archivo) {
            Archivo arch = (Archivo) hijo;
            leerArchivoDirecto(arch);
        } else {
            System.err.println("ERROR: '" + nombre + "' no es un archivo");
            agregarLog("ERROR: '" + nombre + "' no es un archivo");
        }
    }

    public void recibirSolicitud(PCB nuevoProceso) {
        nuevoProceso.setEstado(EstadoProceso.LISTO);
        colaListos.encolar(nuevoProceso);
        agregarLog("Proceso recibido: " + nuevoProceso.getNombre());
        System.out.println("DEBUG: Proceso encolado en LISTOS - Total en cola: " + colaListos.obtenerTamanio());
    }

    public void cambiarPlanificador(Planificador nuevoPlanificador) {
        this.planificadorDisco = nuevoPlanificador;
        agregarLog("Planificador cambiado a: " + nuevoPlanificador.getNombre());
    }

    @Override
    public void run() {
        agregarLog("Simulación iniciada.");
        while (ejecutando) {
            try {

                while (reloj.estaPausado() && ejecutando) {
                    Thread.sleep(100);
                }
                if (!ejecutando) break;


                reloj.esperarCiclo();
                reloj.incrementarCiclo();

                // Bandera para evitar procesar I/O recién generada en el mismo ciclo
                boolean ioGeneradaEsteCiclo = false;


                if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                    PCB proceso = colaListos.desencolar();
                    cpu.asignarProceso(proceso);
                    proceso.registrarInicioEjecucion(reloj.getCicloActual());
                    agregarLog("CPU asignada a: " + proceso.getNombre() + " (necesita " + proceso.getCiclosRestantesCPU() + " ciclos)");
                    // NO ejecutar en el mismo ciclo que se asigna - esperar al siguiente ciclo
                }
                else if (cpu.estaOcupada()) {
                    // Solo ejecutar si ya estaba en CPU (no recién asignado)
                    PCB proceso = cpu.getProcesoActual();

                    // Ejecutar un ciclo
                    proceso.decrementarCiclosCPU();
                    agregarLog("Ejecutando: " + proceso.getNombre() + " (ciclos restantes: " + proceso.getCiclosRestantesCPU() + ")");

                    // Si completó su tiempo de CPU, generar solicitud I/O
                    if (proceso.haCompletadoCPU()) {
                        cpu.liberarProceso();
                        SolicitudIO solicitud = proceso.getSolicitudIO();


                        colaSolicitudesIO.encolar(solicitud);

                        proceso.setEstado(EstadoProceso.BLOQUEADO);
                        colaBloqueados.encolar(proceso);

                        agregarLog("Solicitud I/O generada: " + solicitud.toString() + " - Proceso BLOQUEADO");
                        ioGeneradaEsteCiclo = true; // Marcar que se generó I/O este ciclo
                    }
                }

                // ============================================================
                // FASE 3: PROCESAMIENTO DE I/O (toma varios ciclos)
                // ============================================================

                // Si hay una operación I/O en ejecución, continuar ejecutándola
                if (solicitudIOEnEjecucion != null) {
                    ciclosRestantesIO--;
                    agregarLog("Ejecutando I/O: " + solicitudIOEnEjecucion.toString() +
                               " (ciclos restantes: " + ciclosRestantesIO + ")");

                    // Si completó los ciclos de I/O
                    if (ciclosRestantesIO <= 0) {
                        agregarLog("I/O completada: " + solicitudIOEnEjecucion.toString());

                        // Ejecutar la operación real
                        ejecutarOperacionReal(solicitudIOEnEjecucion);

                        // Desbloquear proceso
                        desbloquearProceso(solicitudIOEnEjecucion);

                        // Limpiar operación en ejecución
                        solicitudIOEnEjecucion = null;
                        ciclosRestantesIO = 0;
                    }
                }
                // Si no hay operación I/O en ejecución Y no se generó I/O este ciclo,
                // seleccionar la siguiente (pero NO ejecutarla aún, esperar al próximo ciclo)
                else if (!colaSolicitudesIO.estaVacia() && !ioGeneradaEsteCiclo) {

                    SolicitudIO siguienteSolicitud = planificadorDisco.seleccionarSiguiente(
                        colaSolicitudesIO,
                        cabezaDiscoActual,
                        disco.getTamanoTotal(),
                        direccionSubida
                    );

                    if (siguienteSolicitud != null) {
                        // Calcular distancia del cabezal
                        int distancia = Math.abs(siguienteSolicitud.getBloqueObjetivo() - cabezaDiscoActual);

                        // Mover cabezal
                        cabezaDiscoActual = siguienteSolicitud.getBloqueObjetivo();

                        if (cabezaDiscoActual == disco.getTamanoTotal() - 1) direccionSubida = false;
                        if (cabezaDiscoActual == 0) direccionSubida = true;

                        // Iniciar operación I/O (toma 5-8 ciclos para más visibilidad y acumulación en cola)
                        solicitudIOEnEjecucion = siguienteSolicitud;
                        ciclosRestantesIO = 5 + (int)(Math.random() * 4);  // 5 a 8 ciclos

                        agregarLog(String.format("Planificador: Seleccionó %s (distancia: %d) - Iniciará I/O en próximo ciclo (%d ciclos totales)",
                                   siguienteSolicitud.toString(), distancia, ciclosRestantesIO));

                        // IMPORTANTE: NO decrementar ni ejecutar en este ciclo
                        // Esperar al próximo ciclo para comenzar la ejecución
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void ejecutarOperacionReal(SolicitudIO solicitud) {

        // Usar directorioTemporal si está definido, sino directorioActual
        Directorio padre = (this.directorioTemporal != null) ? this.directorioTemporal : this.directorioActual;

        TipoOperacion tipo = solicitud.getTipo();
        String nombreArchivo = solicitud.getNombreArchivo();
        boolean exito = false;
        String logMsg = "";

        switch (tipo) {
            case CREAR_ARCHIVO:
                int tamano = solicitud.getTamanoEnBloques();
                String propietario = solicitud.getPropietario();
                Archivo archivo = sistemaArchivos.crearArchivo(nombreArchivo, tamano, padre, propietario);
                exito = (archivo != null);
                logMsg = String.format("Disco: Creó archivo '%s' (%d bloques) propietario: %s en %s",
                                       nombreArchivo, tamano, propietario, padre.getNombre());
                break;

            case CREAR_DIRECTORIO:
                Directorio dir = sistemaArchivos.crearDirectorio(nombreArchivo, padre); 
                exito = (dir != null);
                logMsg = String.format("Disco: Creó directorio '%s' en %s",
                                       nombreArchivo, padre.getNombre());
                break;

            case ELIMINAR_ARCHIVO:
            case ELIMINAR_DIRECTORIO:
                exito = sistemaArchivos.Eliminar(nombreArchivo, padre); 
                logMsg = String.format("Disco: Eliminó '%s' de %s",
                                       nombreArchivo, padre.getNombre());
                break;

            case ACTUALIZAR_ARCHIVO:

                String nuevoNombre = solicitud.getNuevoNombre();
                exito = sistemaArchivos.Renombrar(padre, nombreArchivo, nuevoNombre); 
                logMsg = String.format("Disco: Renombró '%s' a '%s' en %s",
                                       nombreArchivo, nuevoNombre, padre.getNombre());
                break;

            case LEER_ARCHIVO:
                System.out.println("DEBUG: Ejecutando lectura - Archivo: " + nombreArchivo + ", Bloque: " + solicitud.getBloqueObjetivo());

                // Validar bloque antes de leer
                if (solicitud.getBloqueObjetivo() < 0 || solicitud.getBloqueObjetivo() >= disco.getTamanoTotal()) {
                    System.err.println("ERROR: Bloque fuera de rango al leer '" + nombreArchivo + "': " + solicitud.getBloqueObjetivo());
                    exito = false;
                    logMsg = String.format("Disco: FALLÓ lectura de '%s' - Bloque inválido: %d", nombreArchivo, solicitud.getBloqueObjetivo());
                    break;
                }

                int hitsAntes = cacheDisco.getCacheHits();
                Bloque bloqueLeido = cacheDisco.leerBloque(solicitud.getBloqueObjetivo());

                if (bloqueLeido == null) {
                    System.err.println("ERROR: No se pudo leer bloque " + solicitud.getBloqueObjetivo());
                    exito = false;
                    logMsg = String.format("Disco: FALLÓ lectura de '%s' - Bloque: %d", nombreArchivo, solicitud.getBloqueObjetivo());
                    break;
                }

                int hitsDespues = cacheDisco.getCacheHits();
                boolean fueHit = (hitsDespues > hitsAntes);
                exito = true;
                logMsg = String.format("Disco: Leyó bloque %d (Archivo: %s) - %s [Tasa: %.1f%%]",
                                       solicitud.getBloqueObjetivo(), nombreArchivo,
                                       fueHit ? "CACHE HIT" : "CACHE MISS",
                                       cacheDisco.getTasaAciertos());
                break;

            default:
                logMsg = "Disco: Operación desconocida " + tipo;
                exito = false;
                break;
        }

        if (exito) {
            agregarLog(logMsg);
        } else {
            agregarLog("Disco: FALLÓ " + logMsg);
        }

        // Limpiar directorio temporal después de usar
        this.directorioTemporal = null;
    }

    private void desbloquearProceso(SolicitudIO solicitudCompletada) {

        int tamano = colaBloqueados.obtenerTamanio();
        for(int i=0; i<tamano; i++) {
            PCB p = colaBloqueados.desencolar();
            if(p.getSolicitudIO() == solicitudCompletada) { 
                p.setEstado(EstadoProceso.TERMINADO);
                colaTerminados.encolar(p);
                agregarLog("Proceso terminado: " + p.getNombre());
                return; 
            } else {
                colaBloqueados.encolar(p); 
            }
        }
    }

    public void iniciar() {
        if (!ejecutando) {
            ejecutando = true;
            hiloSimulacion = new Thread(this);
            hiloSimulacion.start();
        }
    }
    
    public String obtenerEstadisticasGlobales() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ESTADÍSTICAS DEL SISTEMA ===\n\n");


        int totalProcesos = colaListos.obtenerTamanio() + colaBloqueados.obtenerTamanio() + 
                            colaTerminados.obtenerTamanio() + (cpu.estaOcupada() ? 1 : 0);

        long tiempoEsperaTotal = 0;
        int terminadosCount = 0;


        Object[] terminadosArr = colaTerminados.toArray();
        for (Object obj : terminadosArr) {
            if (obj instanceof PCB) {
                tiempoEsperaTotal += ((PCB) obj).getTiempoEsperaTotal();
                terminadosCount++;
            }
        }

        double promedioEspera = (terminadosCount > 0) ? (double) tiempoEsperaTotal / terminadosCount : 0.0;

        sb.append("Procesos Activos (Listos/Ejec/Bloq): ").append(totalProcesos - terminadosCount).append("\n");
        sb.append("Procesos Terminados: ").append(terminadosCount).append("\n");
        sb.append("Tiempo Promedio de Espera: ").append(String.format("%.2f ms", promedioEspera)).append("\n\n");


        int bloquesLibres = disco.getBloquesLibres();
        int totalBloques = disco.getTamanoTotal();
        double porcentajeUso = 100.0 * (totalBloques - bloquesLibres) / totalBloques;


        int fragmentosLibres = 0;
        boolean enHueco = false;
        Bloque[] bloques = disco.getBloques();

        for (Bloque b : bloques) {
            if (b.isEstaLibre()) {
                if (!enHueco) {
                    fragmentosLibres++;
                    enHueco = true;
                }
            } else {
                enHueco = false;
            }
        }

        sb.append("Uso del Disco: ").append(String.format("%.1f%%", porcentajeUso)).append("\n");
        sb.append("Bloques Libres: ").append(bloquesLibres).append("\n");
        sb.append("Índice de Fragmentación (Zonas libres aisladas): ").append(fragmentosLibres).append("\n");

        return sb.toString();
    }
    
    public void detener() {
        ejecutando = false;
    }
    
    private void agregarLog(String mensaje) {
        logEventos.append("[Ciclo ").append(reloj.getCicloActual()).append("] ").append(mensaje).append("\n");
    }
    
    public SD getDisco() { return disco; }
    public SistemaArchivos getSistemaArchivos() { return sistemaArchivos; }
    public Cola<SolicitudIO> getColaSolicitudes() { return colaSolicitudesIO; }
    public SolicitudIO getSolicitudIOEnEjecucion() { return solicitudIOEnEjecucion; }
    public int getCiclosRestantesIO() { return ciclosRestantesIO; }
    public String getLog() { return logEventos.toString(); }
    public int getCabezaDisco() { return cabezaDiscoActual; }
    public Reloj getReloj() { return reloj; }
    public CPU getCpu() { return cpu; }
    public Cola<PCB> getColaListos() { return colaListos; }
    public Cola<PCB> getColaBloqueados() { return colaBloqueados; }
    public Cola<PCB> getColaTerminados() { return colaTerminados; }
    public Planificador getPlanificadorDisco() { return planificadorDisco; }
    public Directorio getDirectorioActual() { return directorioActual; }
    public BufferCache getCacheDisco() { return cacheDisco; }
    
    public void setDirectorioActual(Directorio dir) { this.directorioActual = dir; }
    
    public void limpiarLog() { logEventos = new StringBuilder(); }
    
    public boolean isEjecutando() { return ejecutando; }
    
    public void pausar() { reloj.pausar(); }

    public void reanudar() { reloj.reanudar(); }

    public void cambiarVelocidad(int duracionCicloMs) {
        reloj.setDuracionCicloMs(duracionCicloMs);
        agregarLog("Velocidad de simulación cambiada: " + duracionCicloMs + " ms/ciclo");
    }

    public int getVelocidadActual() {
        return reloj.getDuracionCicloMs();
    }
}
