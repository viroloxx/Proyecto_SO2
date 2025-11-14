package planificacion;

import estructura_datos.Cola;
import modelo.SolicitudIO;


public interface Planificador {


    void reorganizarCola(Cola<SolicitudIO> colaPeticiones, int cabezaActual, int bloquesTotales, boolean direccionSubida);
    

    SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaPeticiones, int cabezaActual, int bloquesTotales, boolean direccionSubida);


    String getNombre();
}