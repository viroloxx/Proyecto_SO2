package planificacion;
import estructura_datos.Cola;
import modelo.SolicitudIO;

public class SSTF implements Planificador {

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
                
                int dist1 = Math.abs(solicitudes[j].getBloqueObjetivo() - cabezaActual);
                int dist2 = Math.abs(solicitudes[j + 1].getBloqueObjetivo() - cabezaActual);

                if (dist1 > dist2) {
                    SolicitudIO temp = solicitudes[j];
                    solicitudes[j] = solicitudes[j + 1];
                    solicitudes[j + 1] = temp;
                }
            }
        }
        

        colaPeticiones.fromArray(solicitudes);
    }

    @Override
    public SolicitudIO seleccionarSiguiente(Cola<SolicitudIO> colaPeticiones, int cabezaActual, int bloquesTotales, boolean direccionSubida) {

        reorganizarCola(colaPeticiones, cabezaActual, bloquesTotales, direccionSubida);

        return colaPeticiones.desencolar();
    }

    @Override
    public String getNombre() {
        return "SSTF";
    }
}