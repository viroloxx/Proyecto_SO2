package modelo;

import modelo.TipoProceso;

/**
 *
 * @author Diego A. Vivolo
 */


public class PlantillaProceso {

    private String nombre;
    private String tipo;
    private int numeroInstrucciones;
    private int prioridad;
    private int tiempoLlegada; 


    private int ciclosParaExcepcion; 
    private int ciclosParaSatisfacerExcepcion; 

 
    public PlantillaProceso() {
    }


    public PlantillaProceso(String nombre, String tipo, int numeroInstrucciones,
                           int prioridad, int tiempoLlegada,
                           int ciclosParaExcepcion, int ciclosParaSatisfacerExcepcion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.numeroInstrucciones = numeroInstrucciones;
        this.prioridad = prioridad;
        this.tiempoLlegada = tiempoLlegada;
        this.ciclosParaExcepcion = ciclosParaExcepcion;
        this.ciclosParaSatisfacerExcepcion = ciclosParaSatisfacerExcepcion;
    }

    public TipoProceso getTipoProceso() {
        if ("IO_BOUND".equalsIgnoreCase(tipo) || "I/O_BOUND".equalsIgnoreCase(tipo)) {
            return TipoProceso.IO_BOUND;
        }
        return TipoProceso.CPU_BOUND;
    }

    public boolean esValido() {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        if (numeroInstrucciones < 1 || numeroInstrucciones > 1000) {
            return false;
        }
        if (prioridad < 0 || prioridad > 10) {
            return false;
        }
        if (tiempoLlegada < 0) {
            return false;
        }
        if (getTipoProceso() == TipoProceso.IO_BOUND) {
            if (ciclosParaExcepcion < 1 || ciclosParaSatisfacerExcepcion < 1) {
                return false;
            }
        }
        return true;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getNumeroInstrucciones() {
        return numeroInstrucciones;
    }

    public void setNumeroInstrucciones(int numeroInstrucciones) {
        this.numeroInstrucciones = numeroInstrucciones;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public int getTiempoLlegada() {
        return tiempoLlegada;
    }

    public void setTiempoLlegada(int tiempoLlegada) {
        this.tiempoLlegada = tiempoLlegada;
    }

    public int getCiclosParaExcepcion() {
        return ciclosParaExcepcion;
    }

    public void setCiclosParaExcepcion(int ciclosParaExcepcion) {
        this.ciclosParaExcepcion = ciclosParaExcepcion;
    }

    public int getCiclosParaSatisfacerExcepcion() {
        return ciclosParaSatisfacerExcepcion;
    }

    public void setCiclosParaSatisfacerExcepcion(int ciclosParaSatisfacerExcepcion) {
        this.ciclosParaSatisfacerExcepcion = ciclosParaSatisfacerExcepcion;
    }

    @Override
    public String toString() {
        return String.format("%s (%s, %d inst, prior=%d, llegada=%d)",
                           nombre, tipo, numeroInstrucciones, prioridad, tiempoLlegada);
    }
}
