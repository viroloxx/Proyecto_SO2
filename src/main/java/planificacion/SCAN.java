package planificacion;

import estructura_datos.Cola;
import modelo.SolicitudIO;

public class SCAN implements Planificador {

    @Override
    public void reorganizarCola(Cola<SolicitudIO> colaPeticiones, int cabezaActual, int bloquesTotales, boolean direccionSubida) {
        if (colaPeticiones.obtenerTamanio() <= 1) return;

        Object[] arreglo = colaPeticiones.toArray();
        SolicitudIO[] solicitudes = new SolicitudIO[arreglo.length];
        for(int i=0; i < arreglo.length; i++) {
            solicitudes[i] = (SolicitudIO) arreglo[i];
        }


        for (int i = 0; i < solicitudes.length - 1; i++) {
            for (int j = 0; j < solicitudes.length - i - 1; j++) {
                if (solicitudes[j].getBloqueObjetivo() > solicitudes[j + 1].getBloqueObjetivo()) {
                    SolicitudIO temp = solicitudes[j];
                    solicitudes[j] = solicitudes[j + 1];
                    solicitudes[j + 1] = temp;
                }
            }
        }
        
        // Ahora que está ordenado numéricamente, lo re-ordenamos para SCAN
        // Creamos dos listas (puedes usar tu ListaEnlazada aquí, pero
        // para mantenerlo simple, usaré arrays)
        
        SolicitudIO[] colaFinal = new SolicitudIO[solicitudes.length];
        int idxFinal = 0;

        if (direccionSubida) {
            // 1. Atender todos los que están por DELANTE (mayores o iguales)
            for (int i = 0; i < solicitudes.length; i++) {
                if (solicitudes[i].getBloqueObjetivo() >= cabezaActual) {
                    colaFinal[idxFinal++] = solicitudes[i];
                }
            }
            // 2. Atender todos los que están por DETRÁS (menores) en orden REVERSO
            for (int i = solicitudes.length - 1; i >= 0; i--) {
                if (solicitudes[i].getBloqueObjetivo() < cabezaActual) {
                    colaFinal[idxFinal++] = solicitudes[i];
                }
            }
        } else { // Direccion bajando
            // 1. Atender todos los que están por DETRÁS (menores o iguales) en orden REVERSO
            for (int i = solicitudes.length - 1; i >= 0; i--) {
                if (solicitudes[i].getBloqueObjetivo() <= cabezaActual) {
                    colaFinal[idxFinal++] = solicitudes[i];
                }
            }
            // 2. Atender todos los que están por DELANTE (mayores)
            for (int i = 0; i < solicitudes.length; i++) {
                if (solicitudes[i].getBloqueObjetivo() > cabezaActual) {
                    colaFinal[idxFinal++] = solicitudes[i];
                }
            }
        }
        
        colaPeticiones.fromArray(colaFinal);
    }

    @Override
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaPeticiones, int cabezaActual, int bloquesTotales, boolean direccionSubida) {
        reorganizarCola(colaPeticiones, cabezaActual, bloquesTotales, direccionSubida);
        return colaPeticiones.desencolar();
    }

    @Override
    public String getNombre() {
        return "SCAN";
    }
}