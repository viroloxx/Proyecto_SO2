package Persistencia;

import modelo.PCB;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author Diego A. Vivolo
 */
public class PersistenciaCSV {

    public static void guardarResultados(List<PCB> terminados, String ruta) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta))) {
            bw.write("ID,Nombre,TiempoLlegada,TiempoRetorno,TiempoEspera\n");

            for (PCB pcb : terminados) {
                String linea = String.format("%d,%s,%d,%d,%d\n",
                        pcb.getIdProceso(),
                        pcb.getNombre(),
                        pcb.getTiempoLlegada(),
                        pcb.getTiempoRetorno(),
                        pcb.getTiempoEsperaTotal()
                );
                bw.write(linea);
            }
        }
    }
}
