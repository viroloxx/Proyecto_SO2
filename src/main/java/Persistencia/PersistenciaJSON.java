
package Persistencia;

import estructura_datos.cola; 
import modelo.PCB;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Diego A. Vivolo
 */
public class PersistenciaJSON {


    public static void guardarResultados(Lista terminados, String ruta) throws IOException {
        

        PCB[] procesosArray = terminados.toArray(); 
        
        
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        
        
        try (FileWriter writer = new FileWriter(ruta)) {
            
        
            gson.toJson(procesosArray, writer);
        }
        
    }

}
