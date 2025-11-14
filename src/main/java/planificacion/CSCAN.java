package planificacion;

import estructura_datos.Cola;
import modelo.SolicitudIO;

public class CSCAN implements Planificador {

    @Override
    public void reorganizarCola(Cola<SolicitudIO> colaPeticiones, int cabezaActual, int bloquesTotales, boolean direccionSubida) {
        if (colaPeticiones.obtenerTamanio() <= 1) return;

        // C-SCAN solo se mueve en una dirección (asumamos "subida")
        // Así que ignoraremos `direccionSubida` y siempre subiremos.

        Object[] arreglo = colaPeticiones.toArray();
        SolicitudIO[] solicitudes = new SolicitudIO[arreglo.length];
        for(int i=0; i < arreglo.length; i++) {
            solicitudes[i] = (SolicitudIO) arreglo[i];
        }

        // Bubble Sort para ordenar por posición de bloque
        for (int i = 0; i < solicitudes.length - 1; i++) {
            for (int j = 0; j < solicitudes.length - i - 1; j++) {
                if (solicitudes[j].getBloqueObjetivo() > solicitudes[j + 1].getBloqueObjetivo()) {
                    SolicitudIO temp = solicitudes[j];
                    solicitudes[j] = solicitudes[j + 1];
                    solicitudes[j + 1] = temp;
                }
            }
        }
        
        SolicitudIO[] colaFinal = new SolicitudIO[solicitudes.length];
        int idxFinal = 0;

        // 1. Atender todos los que están por DELANTE (mayores o iguales)
        for (int i = 0; i < solicitudes.length; i++) {
            if (solicitudes[i].getBloqueObjetivo() >= cabezaActual) {
                colaFinal[idxFinal++] = solicitudes[i];
            }
        }
        // 2. Saltar al inicio y atender los del principio (menores)
        for (int i = 0; i < solicitudes.length; i++) {
            if (solicitudes[i].getBloqueObjetivo() < cabezaActual) {
                colaFinal[idxFinal++] = solicitudes[i];
            }
        }
        
        colaPeticiones.fromArray(colaFinal);
    }

    @Override
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaPeticiones, int cabezaActual, int bloquesTotales, boolean direccionSubida) {
        // Asumimos que C-SCAN siempre sube, así que `direccionSubida` es true
        reorganizarCola(colaPeticiones, cabezaActual, bloquesTotales, true);
        return colaPeticiones.desencolar();
    }

    @Override
    public String getNombre() {
        return "C-SCAN";
    }
}