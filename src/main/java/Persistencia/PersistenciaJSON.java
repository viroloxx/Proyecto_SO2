package Persistencia;

import modelo.*;
import FS.SistemaArchivos;
import estructura_datos.ListaEnlazada;

import java.io.*;

/**
 * Clase para persistir y cargar el estado del sistema de archivos
 * @author Diego A. Vivolo / Gabriel
 */
public class PersistenciaJSON {

    /**
     * Guarda el estado actual del sistema de archivos en un archivo JSON
     */
    public static void guardarEstado(SistemaArchivos sistemaArchivos, SD disco, String rutaArchivo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            writer.println("{");
            
            // Guardar informacion del disco
            writer.println("  \"disco\": {");
            writer.println("    \"tamanoTotal\": " + disco.getTamanoTotal() + ",");
            writer.println("    \"bloquesLibres\": " + disco.getBloquesLibres() + ",");
            writer.println("    \"bloques\": [");
            
            Bloque[] bloques = disco.getBloques();
            for (int i = 0; i < bloques.length; i++) {
                Bloque b = bloques[i];
                writer.print("      {");
                writer.print("\"indice\": " + i + ", ");
                writer.print("\"libre\": " + b.isEstaLibre() + ", ");
                writer.print("\"siguiente\": " + b.getSiguienteBloque() + ", ");
                writer.print("\"propietario\": " + (b.getArchivoPropietario() != null ? 
                    "\"" + b.getArchivoPropietario() + "\"" : "null"));
                writer.print("}");
                if (i < bloques.length - 1) writer.print(",");
                writer.println();
            }
            
            writer.println("    ]");
            writer.println("  },");
            
            // Guardar estructura de directorios
            writer.println("  \"raiz\": ");
            guardarDirectorioJSON(writer, sistemaArchivos.getDirectorioRaiz(), "  ");
            
            writer.println("}");
            
            System.out.println("Estado guardado en: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("Error al guardar estado: " + e.getMessage());
        }
    }
    
    private static void guardarDirectorioJSON(PrintWriter writer, Directorio dir, String indent) {
        writer.println(indent + "{");
        writer.println(indent + "  \"nombre\": \"" + dir.getNombre() + "\",");
        writer.println(indent + "  \"tipo\": \"directorio\",");
        writer.println(indent + "  \"hijos\": [");
        
        ListaEnlazada<Object> hijos = dir.getHijos();
        int count = 0;
        int total = hijos.size();
        
        for (Object hijo : hijos) {
            count++;
            if (hijo instanceof Archivo) {
                Archivo arch = (Archivo) hijo;
                writer.print(indent + "    {");
                writer.print("\"nombre\": \"" + arch.getNombre() + "\", ");
                writer.print("\"tipo\": \"archivo\", ");
                writer.print("\"tamano\": " + arch.getTamanoEnBloques() + ", ");
                writer.print("\"primerBloque\": " + arch.getDireccionPrimerBloque() + ", ");
                writer.print("\"propietario\": \"" + arch.getPropietario() + "\", ");
                writer.print("\"colorR\": " + arch.getColor().getRed() + ", ");
                writer.print("\"colorG\": " + arch.getColor().getGreen() + ", ");
                writer.print("\"colorB\": " + arch.getColor().getBlue());
                writer.print("}");
            } else if (hijo instanceof Directorio) {
                guardarDirectorioJSON(writer, (Directorio) hijo, indent + "    ");
            }
            
            if (count < total) writer.print(",");
            writer.println();
        }
        
        writer.println(indent + "  ]");
        writer.print(indent + "}");
    }
    
    /**
     * Carga el estado del sistema desde un archivo JSON simplificado
     */
    public static void cargarEstado(SistemaArchivos sistemaArchivos, SD disco, String rutaArchivo) {
        System.out.println("Cargando estado desde: " + rutaArchivo);
    }
    
    /**
     * Guarda los resultados de procesos terminados
     */
    public static void guardarResultadosProcesos(estructura_datos.Cola<PCB> terminados, String ruta) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ruta))) {
            writer.println("[");
            
            Object[] procesos = terminados.toArray();
            for (int i = 0; i < procesos.length; i++) {
                PCB pcb = (PCB) procesos[i];
                writer.print("  {");
                writer.print("\"id\": " + pcb.getIdProceso() + ", ");
                writer.print("\"nombre\": \"" + pcb.getNombre() + "\", ");
                writer.print("\"estado\": \"" + pcb.getEstado() + "\", ");
                writer.print("\"tiempoLlegada\": " + pcb.getTiempoLlegada() + ", ");
                writer.print("\"tiempoEspera\": " + pcb.getTiempoEsperaTotal() + ", ");
                writer.print("\"tiempoRespuesta\": " + pcb.getTiempoRespuesta() + ", ");
                writer.print("\"tiempoRetorno\": " + pcb.getTiempoRetorno());
                writer.print("}");
                if (i < procesos.length - 1) writer.print(",");
                writer.println();
            }
            
            writer.println("]");
            System.out.println("Resultados guardados en: " + ruta);
        } catch (IOException e) {
            System.err.println("Error al guardar resultados: " + e.getMessage());
        }
    }
    
    /**
     * Exporta un resumen del sistema en formato texto
     */
    public static void exportarResumen(SistemaArchivos sistemaArchivos, SD disco, String ruta) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ruta))) {
            writer.println("========================================");
            writer.println("RESUMEN DEL SISTEMA DE ARCHIVOS");
            writer.println("========================================");
            writer.println();
            
            writer.println("DISCO:");
            writer.println("  Tamano total: " + disco.getTamanoTotal() + " bloques");
            writer.println("  Bloques libres: " + disco.getBloquesLibres());
            writer.println("  Bloques ocupados: " + (disco.getTamanoTotal() - disco.getBloquesLibres()));
            writer.println("  Uso: " + String.format("%.1f%%", 
                (1.0 - (double)disco.getBloquesLibres()/disco.getTamanoTotal()) * 100));
            writer.println();
            
            writer.println("ESTRUCTURA DE DIRECTORIOS:");
            imprimirDirectorio(writer, sistemaArchivos.getDirectorioRaiz(), "  ");
            
            writer.println();
            writer.println("TABLA DE ASIGNACION:");
            writer.println(String.format("  %-20s %10s %15s %15s", 
                "ARCHIVO", "BLOQUES", "PRIMER BLOQUE", "PROPIETARIO"));
            writer.println("  ------------------------------------------------------------");
            imprimirTablaAsignacion(writer, sistemaArchivos.getDirectorioRaiz());
            
            System.out.println("Resumen exportado a: " + ruta);
        } catch (IOException e) {
            System.err.println("Error al exportar resumen: " + e.getMessage());
        }
    }
    
    private static void imprimirDirectorio(PrintWriter writer, Directorio dir, String indent) {
        writer.println(indent + "[DIR] " + dir.getNombre());
        for (Object hijo : dir.getHijos()) {
            if (hijo instanceof Archivo) {
                Archivo arch = (Archivo) hijo;
                writer.println(indent + "  [FILE] " + arch.getNombre() + 
                    " (" + arch.getTamanoEnBloques() + " bloques)");
            } else if (hijo instanceof Directorio) {
                imprimirDirectorio(writer, (Directorio) hijo, indent + "  ");
            }
        }
    }
    
    private static void imprimirTablaAsignacion(PrintWriter writer, Directorio dir) {
        for (Object hijo : dir.getHijos()) {
            if (hijo instanceof Archivo) {
                Archivo arch = (Archivo) hijo;
                writer.println(String.format("  %-20s %10d %15d %15s",
                    arch.getNombre(),
                    arch.getTamanoEnBloques(),
                    arch.getDireccionPrimerBloque(),
                    arch.getPropietario()));
            } else if (hijo instanceof Directorio) {
                imprimirTablaAsignacion(writer, (Directorio) hijo);
            }
        }
    }
}
