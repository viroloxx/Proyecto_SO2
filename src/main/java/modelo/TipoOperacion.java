
package modelo;
import modelo.PCB;
import modelo.Archivo;

/**
 *
 * @author Diego A. Vivolo
 */


public enum TipoOperacion {
    CREAR_ARCHIVO,
    LEER_ARCHIVO,
    ACTUALIZAR_ARCHIVO,
    ELIMINAR_ARCHIVO,
    CREAR_DIRECTORIO,
    ELIMINAR_DIRECTORIO
}

public class SolicitudIO {
    private final TipoOperacion tipo;
    private final String nombreArchivo;
    private final int tamanoEnBloques; 
    private final String nuevoNombre;

    public SolicitudIO(TipoOperacion tipo, String nombreArchivo, int tamanoEnBloques) {
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.tamanoEnBloques = tamanoEnBloques;
        this.nuevoNombre = null;
    }
    

    public SolicitudIO(TipoOperacion tipo, String nombreArchivo) {
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.tamanoEnBloques = 0;
        this.nuevoNombre = null;
    }

    public SolicitudIO(TipoOperacion tipo, String nombreArchivo, String nuevoNombre) {
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.nuevoNombre = nuevoNombre;
        this.tamanoEnBloques = 0;
    }

    public TipoOperacion getTipo() { return tipo; }
    public String getNombreArchivo() { return nombreArchivo; }
    public int getTamanoEnBloques() { return tamanoEnBloques; }
    public String getNuevoNombre() { return nuevoNombre; }
    
    @Override
    public String toString() {
        switch(tipo) {
            case CREAR_ARCHIVO:
                return String.format("CREAR %s (%d bloques)", nombreArchivo, tamanoEnBloques);
            case LEER_ARCHIVO:
                return "LEER " + nombreArchivo;
            case ELIMINAR_ARCHIVO:
                return "ELIMINAR " + nombreArchivo;
            case ACTUALIZAR_ARCHIVO:
                return String.format("RENOMBRAR %s -> %s", nombreArchivo, nuevoNombre);
            default:
                return tipo.name() + " " + nombreArchivo;
        }
    }
}