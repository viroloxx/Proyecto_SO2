package FS;

import estructura_datos.ListaEnlazada;
import modelo.SD;
import modelo.Directorio;
import modelo.Archivo;
import modelo.Bloque;
/**
 *
 * @author Diego A. Vivolo
 */
 
public class SistemaArchivos {

    private Directorio directorioRaiz;
    private SD disco;

    public SistemaArchivos(SD disco) {
        this.disco = disco;
        this.directorioRaiz = new Directorio("raiz");
    }

    public Directorio getDirectorioRaiz() {
        return this.directorioRaiz;
    }
    
    public SD getDisco() {
        return this.disco;
    }


    public Archivo crearArchivo(String nombreArchivo, int tamanoEnBloques, Directorio padre) {
        return crearArchivo(nombreArchivo, tamanoEnBloques, padre, "admin");
    }

    public Archivo crearArchivo(String nombreArchivo, int tamanoEnBloques, Directorio padre, String propietario) {
        if (padre.buscarHijo(nombreArchivo) != null) {
            System.err.println("Error: Ya existe un archivo o directorio con ese nombre.");
            return null;
        }
        int primerBloque = disco.alocarBloques(nombreArchivo, tamanoEnBloques);


        if (primerBloque == -1) {

            return null;
        }


        Archivo nuevoArchivo = new Archivo(nombreArchivo, tamanoEnBloques, primerBloque, propietario);


        padre.agregarArchivo(nuevoArchivo);

        System.out.println("Archivo creado: " + nombreArchivo + " (propietario: " + propietario + ") en " + padre.getNombre());
        return nuevoArchivo;
    }
    
    public Directorio crearDirectorio(String nombreDirectorio, Directorio padre) {
        if (padre.buscarHijo(nombreDirectorio) != null) {
            System.err.println("Error: Ya existe un archivo o directorio con ese nombre.");
            return null;
        }

       
        Directorio nuevoDirectorio = new Directorio(nombreDirectorio);
        padre.agregarSubdirectorio(nuevoDirectorio);
        
        System.out.println("Directorio creado: " + nombreDirectorio + " en " + padre.getNombre());
        return nuevoDirectorio;
    }

    public boolean Eliminar(String nombreHijo, Directorio padre) {

        Object hijoAeliminar = padre.buscarHijo(nombreHijo);

        if (hijoAeliminar == null) {
            System.err.println("Error: No se encontró '" + nombreHijo + "'.");
            return false;
        }

        if (hijoAeliminar instanceof Archivo) {
            Archivo archivo = (Archivo) hijoAeliminar;
            
     
            disco.liberarBloques(archivo.getDireccionPrimerBloque());
            
 
            padre.eliminarHijo(archivo);
            System.out.println("Archivo eliminado: " + archivo.getNombre());

        } else if (hijoAeliminar instanceof Directorio) {
            Directorio directorio = (Directorio) hijoAeliminar;
       
            eliminarDirectorioRecursivo(directorio);
            
  
            padre.eliminarHijo(directorio);
            System.out.println("Directorio eliminado: " + directorio.getNombre());
        }
        
        return true;
    }

    private void eliminarDirectorioRecursivo(Directorio directorioAeliminar) {
        ListaEnlazada<Object> hijos = directorioAeliminar.getHijos();
        
        Object[] hijosCopia = new Object[hijos.size()];
        int i = 0;
        for (Object hijo : hijos) {
            hijosCopia[i++] = hijo;
        }
        for (Object hijo : hijosCopia) {
            if (hijo == null) continue;
            if (hijo instanceof Archivo) {
                Archivo archivo = (Archivo) hijo;
             
                disco.liberarBloques(archivo.getDireccionPrimerBloque());
              
                directorioAeliminar.eliminarHijo(archivo);
                
            } else if (hijo instanceof Directorio) {
             
                eliminarDirectorioRecursivo((Directorio) hijo);
            
                directorioAeliminar.eliminarHijo(hijo);
            }
        }
    }
    

    public boolean Renombrar(Directorio padre, String nombreActual, String nombreNuevo) {
        
        if (padre.buscarHijo(nombreNuevo) != null) {
            System.err.println("Error: Ya existe un archivo con el nombre '" + nombreNuevo + "'.");
            return false;
        }

        Object hijoArenombrar = padre.buscarHijo(nombreActual);

        if (hijoArenombrar == null) {
            System.err.println("Error: No se encontró '" + nombreActual + "'.");
            return false;
        }

        if (hijoArenombrar instanceof Archivo) {
            Archivo archivo = (Archivo) hijoArenombrar;
            archivo.setNombre(nombreNuevo);

            int indiceBloque = archivo.getDireccionPrimerBloque();
            Bloque[] bloques = disco.getBloques();
            while (indiceBloque != -1) {
                if(indiceBloque < bloques.length) { 
                   bloques[indiceBloque].setArchivoPropietario(nombreNuevo);
                   indiceBloque = bloques[indiceBloque].getSiguienteBloque();
                } else {
                   break;
                }
            }

        } else if (hijoArenombrar instanceof Directorio) {
            ((Directorio) hijoArenombrar).setNombre(nombreNuevo);
        }
        
        System.out.println("Renombrado: " + nombreActual + " -> " + nombreNuevo);
        return true;
    }
}
