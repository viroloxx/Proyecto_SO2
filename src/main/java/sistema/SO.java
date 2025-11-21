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
    }
    
    // Método para crear archivo desde la GUI
    public void crearArchivoDesdeGUI(String nombre, int tamanoEnBloques) {
        int bloqueObjetivo = (int)(Math.random() * disco.getTamanoTotal());
        SolicitudIO solicitud = new SolicitudIO(TipoOperacion.CREAR_ARCHIVO, nombre, tamanoEnBloques, bloqueObjetivo);
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
    
    // Método para leer archivo desde la GUI
    public void leerArchivoDesdeGUI(String nombre) {
        Object hijo = directorioActual.buscarHijo(nombre);
        if (hijo instanceof Archivo) {
            int bloqueObjetivo = ((Archivo) hijo).getDireccionPrimerBloque();
            SolicitudIO solicitud = new SolicitudIO(TipoOperacion.LEER_ARCHIVO, nombre, bloqueObjetivo);
            PCB proceso = new PCB(++contadorProcesos, "Proceso-" + contadorProcesos, reloj.getCicloActual(), solicitud);
            recibirSolicitud(proceso);
        }
    }

    public void recibirSolicitud(PCB nuevoProceso) {
        nuevoProceso.setEstado(EstadoProceso.LISTO);
        colaListos.encolar(nuevoProceso);
        agregarLog("Proceso recibido: " + nuevoProceso.getNombre());
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
                

                if (!cpu.estaOcupada() && !colaListos.estaVacia()) {
                    PCB proceso = colaListos.desencolar();
                    cpu.asignarProceso(proceso);
                    agregarLog("CPU asignada a: " + proceso.getNombre());
                }
                

                if (cpu.estaOcupada()) {
                    PCB proceso = cpu.liberarProceso(); 
                    SolicitudIO solicitud = proceso.getSolicitudIO();
                    

                    colaSolicitudesIO.encolar(solicitud);

                    proceso.setEstado(EstadoProceso.BLOQUEADO);
                    colaBloqueados.encolar(proceso);
                    
                    agregarLog("Solicitud generada: " + solicitud.toString());
                }
                
                if (!colaSolicitudesIO.estaVacia()) {

                    SolicitudIO siguienteSolicitud = planificadorDisco.seleccionarSiguiente(
                        colaSolicitudesIO, 
                        cabezaDiscoActual, 
                        disco.getTamanoTotal(), 
                        direccionSubida
                    );
                    
                    if (siguienteSolicitud != null) {

                        cabezaDiscoActual = siguienteSolicitud.getBloqueObjetivo();

                        if (cabezaDiscoActual == disco.getTamanoTotal() - 1) direccionSubida = false;
                        if (cabezaDiscoActual == 0) direccionSubida = true;

                        ejecutarOperacionReal(siguienteSolicitud);
                        

                        desbloquearProceso(siguienteSolicitud);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void ejecutarOperacionReal(SolicitudIO solicitud) {


        Directorio padre = this.directorioActual; 

        TipoOperacion tipo = solicitud.getTipo();
        String nombreArchivo = solicitud.getNombreArchivo();
        boolean exito = false;
        String logMsg = "";

        switch (tipo) {
            case CREAR_ARCHIVO:
                int tamano = solicitud.getTamanoEnBloques();
                Archivo archivo = sistemaArchivos.crearArchivo(nombreArchivo, tamano, padre); 
                exito = (archivo != null);
                logMsg = String.format("Disco: Creó archivo '%s' (%d bloques) en %s",
                                       nombreArchivo, tamano, padre.getNombre());
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
                
                Bloque bloqueLeido = cacheDisco.leerBloque(solicitud.getBloqueObjetivo());

                exito = true; 
                logMsg = String.format("Disco: Leyó bloque %d (Archivo: %s)",
                                       solicitud.getBloqueObjetivo(), nombreArchivo);
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
    
    public void detener() {
        ejecutando = false;
    }
    
    private void agregarLog(String mensaje) {
        logEventos.append("[Ciclo ").append(reloj.getCicloActual()).append("] ").append(mensaje).append("\n");
    }
    
    public SD getDisco() { return disco; }
    public SistemaArchivos getSistemaArchivos() { return sistemaArchivos; }
    public Cola<SolicitudIO> getColaSolicitudes() { return colaSolicitudesIO; }
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
}