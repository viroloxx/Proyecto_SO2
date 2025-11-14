package modelo;

/**
 *
 * @author Diego A. Vivolo
 */
public class Bloque {

    private boolean estaLibre;
    private int siguienteBloque; 
    private String archivoPropietario;


    public Bloque() {
        this.estaLibre = true;
        this.siguienteBloque = -1; 
        this.archivoPropietario = null;
    }


    public void Ocupar(String nombreArchivo, int siguiente) {
        this.estaLibre = false;
        this.archivoPropietario = nombreArchivo;
        this.siguienteBloque = siguiente;
    }

    public void Liberar() {
        this.estaLibre = true;
        this.siguienteBloque = -1;
        this.archivoPropietario = null;
    }

    public boolean isEstaLibre() {
        return estaLibre;
    }

    public int getSiguienteBloque() {
        return siguienteBloque;
    }

    public void setSiguienteBloque(int siguienteBloque) {
        this.siguienteBloque = siguienteBloque;
    }

    public String getArchivoPropietario() {
        return archivoPropietario;
    }

    public void setArchivoPropietario(String archivoPropietario) {
        this.archivoPropietario = archivoPropietario;
    }
}
    
   
