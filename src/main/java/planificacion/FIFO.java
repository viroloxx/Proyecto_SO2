package planificacion;
import estructura_datos.Cola;
import modelo.SolicitudIO;

public class FIFO implements Planificador {

    @Override
    public void reorganizarCola(Cola<SolicitudIO> colaPeticiones, int cabezaActual, int bloquesTotales, boolean direccionSubida) {
        // FIFO (First-In, First-Out) no necesita reorganizar la cola.
        // Se atiende en el orden de llegada.
    }

    @Override
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaPeticiones, int cabezaActual, int bloquesTotales, boolean direccionSubida) {

        return colaPeticiones.desencolar();
    }

    @Override
    public String getNombre() {
        return "FIFO";
    }
}