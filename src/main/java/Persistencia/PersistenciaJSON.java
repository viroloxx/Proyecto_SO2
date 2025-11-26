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

        try (BufferedReader reader = new BufferedReader(new FileReader(rutaArchivo))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }

            // Limpiar el sistema de archivos antes de cargar
            System.out.println("Limpiando sistema de archivos existente...");
            limpiarDirectorio(sistemaArchivos.getDirectorioRaiz(), sistemaArchivos, disco);

            // Parsear JSON manualmente (sin librerías externas)
            String json = jsonContent.toString();

            // Buscar el objeto "raiz"
            int raizIndex = json.indexOf("\"raiz\"");
            if (raizIndex == -1) {
                System.err.println("No se encontró el elemento 'raiz' en el JSON");
                return;
            }

            // Extraer la sección raiz
            int raizStart = json.indexOf("{", raizIndex);
            int raizEnd = encontrarCierreObjeto(json, raizStart);
            String raizJson = json.substring(raizStart, raizEnd + 1);

            // Procesar el directorio raiz
            procesarDirectorio(raizJson, sistemaArchivos.getDirectorioRaiz(), sistemaArchivos);

            System.out.println("Estado cargado exitosamente desde: " + rutaArchivo);
            System.out.println("Bloques ocupados: " + (disco.getTamanoTotal() - disco.getBloquesLibres()) + " de " + disco.getTamanoTotal());

        } catch (IOException e) {
            System.err.println("Error al cargar estado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void limpiarDirectorio(Directorio dir, SistemaArchivos sistemaArchivos, SD disco) {
        ListaEnlazada<Object> hijos = dir.getHijos();

        // Crear copia del contenido para evitar problemas de concurrencia
        Object[] hijosCopia = new Object[hijos.size()];
        int i = 0;
        for (Object hijo : hijos) {
            hijosCopia[i++] = hijo;
        }

        // Eliminar cada hijo
        for (Object hijo : hijosCopia) {
            if (hijo == null) continue;

            if (hijo instanceof Archivo) {
                Archivo arch = (Archivo) hijo;
                // Liberar bloques del disco
                disco.liberarBloques(arch.getDireccionPrimerBloque());
                // Eliminar del directorio
                dir.eliminarHijo(arch);
                System.out.println("  Eliminado archivo: " + arch.getNombre());

            } else if (hijo instanceof Directorio) {
                Directorio subDir = (Directorio) hijo;
                // Limpiar recursivamente
                limpiarDirectorio(subDir, sistemaArchivos, disco);
                // Eliminar el subdirectorio
                dir.eliminarHijo(subDir);
                System.out.println("  Eliminado directorio: " + subDir.getNombre());
            }
        }
    }

    private static void procesarDirectorio(String dirJson, Directorio directorioActual, SistemaArchivos sistemaArchivos) {
        // Buscar el array "hijos"
        int hijosIndex = dirJson.indexOf("\"hijos\"");
        if (hijosIndex == -1) return;

        int arrayStart = dirJson.indexOf("[", hijosIndex);
        int arrayEnd = encontrarCierreArray(dirJson, arrayStart);
        String hijosJson = dirJson.substring(arrayStart + 1, arrayEnd);

        // Procesar cada hijo
        int pos = 0;
        while (pos < hijosJson.length()) {
            // Buscar siguiente objeto
            int objStart = hijosJson.indexOf("{", pos);
            if (objStart == -1) break;

            int objEnd = encontrarCierreObjeto(hijosJson, objStart);
            String objetoJson = hijosJson.substring(objStart, objEnd + 1);

            // Extraer tipo
            String tipo = extraerValor(objetoJson, "tipo");

            if ("archivo".equals(tipo)) {
                // Crear archivo
                String nombre = extraerValor(objetoJson, "nombre");
                int tamano = Integer.parseInt(extraerValor(objetoJson, "tamano"));
                String propietario = extraerValor(objetoJson, "propietario");

                // NUEVO: Extraer primerBloque si está especificado
                String primerBloqueStr = extraerValor(objetoJson, "primerBloque");
                Integer bloqueInicio = null;
                if (primerBloqueStr != null && !primerBloqueStr.isEmpty()) {
                    try {
                        bloqueInicio = Integer.parseInt(primerBloqueStr);
                    } catch (NumberFormatException e) {
                        bloqueInicio = null;
                    }
                }

                if (propietario == null || propietario.isEmpty()) {
                    propietario = "admin";
                }

                // Si se especificó un bloque inicial, usar método de asignación estratégica
                if (bloqueInicio != null && bloqueInicio > 0) {
                    System.out.println("  Creando archivo: " + nombre + " (" + tamano + " bloques, propietario: " + propietario + ", bloque inicial: ~" + bloqueInicio + ")");
                    sistemaArchivos.crearArchivoEnBloque(nombre, tamano, directorioActual, propietario, bloqueInicio);
                } else {
                    System.out.println("  Creando archivo: " + nombre + " (" + tamano + " bloques, propietario: " + propietario + ")");
                    sistemaArchivos.crearArchivo(nombre, tamano, directorioActual, propietario);
                }

            } else if ("directorio".equals(tipo)) {
                // Crear subdirectorio
                String nombre = extraerValor(objetoJson, "nombre");

                if (!"raiz".equals(nombre)) {
                    System.out.println("  Creando directorio: " + nombre);
                    Directorio nuevoDir = sistemaArchivos.crearDirectorio(nombre, directorioActual);

                    // Procesar hijos del subdirectorio recursivamente
                    if (nuevoDir != null) {
                        procesarDirectorio(objetoJson, nuevoDir, sistemaArchivos);
                    }
                }
            }

            pos = objEnd + 1;
        }
    }

    private static String extraerValor(String json, String campo) {
        String patron = "\"" + campo + "\"";
        int index = json.indexOf(patron);
        if (index == -1) return null;

        int colonIndex = json.indexOf(":", index);
        if (colonIndex == -1) return null;

        // Saltar espacios
        colonIndex++;
        while (colonIndex < json.length() && Character.isWhitespace(json.charAt(colonIndex))) {
            colonIndex++;
        }

        // Si es string (comienza con ")
        if (json.charAt(colonIndex) == '"') {
            int startQuote = colonIndex + 1;
            int endQuote = json.indexOf("\"", startQuote);
            return json.substring(startQuote, endQuote);
        } else {
            // Es número
            int end = colonIndex;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
                end++;
            }
            return json.substring(colonIndex, end);
        }
    }

    private static int encontrarCierreObjeto(String json, int start) {
        int count = 0;
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '{') count++;
            else if (json.charAt(i) == '}') {
                count--;
                if (count == 0) return i;
            }
        }
        return json.length() - 1;
    }

    private static int encontrarCierreArray(String json, int start) {
        int count = 0;
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '[') count++;
            else if (json.charAt(i) == ']') {
                count--;
                if (count == 0) return i;
            }
        }
        return json.length() - 1;
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
