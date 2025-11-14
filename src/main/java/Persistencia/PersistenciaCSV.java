/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Persistencia;

/**
 *
 * @author Diego A. Vivolo
 */
import estructura_datos.Lista; 
import modelo.PCB;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Diego A. Vivolo
 */
public class PersistenciaCSV {


    public static void guardarResultados(Lista terminados, String ruta) throws IOException {
        
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta))) {

            bw.write("ID,Nombre,Tipo,Prioridad,TiempoLlegada,TiempoEjecucion,TiempoRetorno,TiempoEspera,CiclosParaExcepcion,CiclosParaSatisfacerExcepcion\n");


            PCB[] procesos = terminados.toArray();

            for (PCB pcb : procesos) {

                String linea = String.format("%d,%s,%s,%d,%d,%d,%d,%d,%d,%d\n",
                        pcb.getIdProceso(),
                        pcb.getNombre(),
                        pcb.getTipo().toString(),
                        pcb.getPrioridad(),
                        pcb.getTiempoLlegada(),
                        pcb.getTiempoEjecucion(),
                        pcb.getTiempoRetorno(),
                        pcb.getTiempoEsperaTotal(),
                        pcb.getCiclosParaExcepcion(),
                        pcb.getCiclosParaSatisfacerExcepcion()
                );
                bw.write(linea);
            }
        } 
    
    }
}
