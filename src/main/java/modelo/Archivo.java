package modelo;

/**
 *
 * @author Diego A. Vivolo
 */
public class Archivo {

    private String nombre;
    private int tamanoEnBloques;
    private int direccionPrimerBloque;

    public Archivo(String nombre, int tamanoEnBloques, int direccionPrimerBloque) {
        this.nombre = nombre;
        this.tamanoEnBloques = tamanoEnBloques;
        this.direccionPrimerBloque = direccionPrimerBloque;
    }


    public String getNombre() {
        return nombre;
    }

    public int getTamanoEnBloques() {
        return tamanoEnBloques;
    }

    public int getDireccionPrimerBloque() {
        return direccionPrimerBloque;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre; 
    }
}
