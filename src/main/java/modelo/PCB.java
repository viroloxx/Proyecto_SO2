package modelo;

import modelo.TipoOperacion;

/**
 *
 * @author Diego A. Vivolo
 */

public class PCB {
    
    private final int idProceso;
    private final String nombre;
    private EstadoProceso estado;
    private final SolicitudIO solicitudIO;

    private final int tiempoLlegada;
    private int tiempoEsperaTotal;
    private int tiempoRespuesta;
    private int tiempoRetorno;


    private boolean primeraEjecucion;
    private int tiempoInicioPrimeraEjecucion;

    // Ciclos que el proceso necesita ejecutar en CPU antes de solicitar I/O
    private int ciclosRestantesCPU;


    public PCB(int id, String nombre, int tiempoLlegada, SolicitudIO solicitud) {
        this.idProceso = id;
        this.nombre = "Proc-" + id + "-" + solicitud.getTipo().name();
        this.estado = EstadoProceso.NUEVO;
        this.tiempoLlegada = tiempoLlegada;
        this.solicitudIO = solicitud;

        this.tiempoEsperaTotal = 0;
        this.primeraEjecucion = true;
        this.tiempoInicioPrimeraEjecucion = -1;
        this.tiempoRespuesta = -1;
        this.tiempoRetorno = -1;

        // Cada proceso necesita 2-4 ciclos de CPU antes de generar I/O
        this.ciclosRestantesCPU = 2 + (int)(Math.random() * 3); // 2 a 4 ciclos
    }

    public void incrementarTiempoEspera() {
        if (estado == EstadoProceso.LISTO) {
            tiempoEsperaTotal++;
        }
    }
    
    public void registrarInicioEjecucion(int cicloActual) {
        if (primeraEjecucion) {
            this.tiempoInicioPrimeraEjecucion = cicloActual;
            this.tiempoRespuesta = cicloActual - tiempoLlegada;
            this.primeraEjecucion = false;
        }
    }

    public void registrarTiempoRetorno(int cicloActual) {
        this.tiempoRetorno = cicloActual - tiempoLlegada;
    }

    public void decrementarCiclosCPU() {
        if (ciclosRestantesCPU > 0) {
            ciclosRestantesCPU--;
        }
    }

    public boolean haCompletadoCPU() {
        return ciclosRestantesCPU <= 0;
    }

    public boolean haTerminado() {
        return this.estado == EstadoProceso.TERMINADO;
    }
    
    public int getIdProceso() { return idProceso; }
    public String getNombre() { return nombre; }
    public EstadoProceso getEstado() { return estado; }
    public SolicitudIO getSolicitudIO() { return solicitudIO; }
    public int getTiempoLlegada() { return tiempoLlegada; }
    public int getTiempoEsperaTotal() { return tiempoEsperaTotal; }
    public int getTiempoRespuesta() { return tiempoRespuesta; }
    public int getTiempoRetorno() { return tiempoRetorno; }
    public int getCiclosRestantesCPU() { return ciclosRestantesCPU; }
    

    public void setEstado(EstadoProceso estado) { this.estado = estado; }

    @Override
    public String toString() {

        return String.format("PCB(ID:%d, Estado:%s, Solicitud:[%s])", 
            idProceso, 
            estado, 
            solicitudIO.toString()
        );
    }
}