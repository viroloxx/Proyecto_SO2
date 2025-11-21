package modelo;

/**
 *
 * @author Diego A. Vivolo
 */
public class Archivo {

    private String nombre;
    private int tamanoEnBloques;
    private int direccionPrimerBloque;
    private java.awt.Color color;
    private String propietario; // Usuario que creó el archivo
    
    // Colores predefinidos para archivos
    private static final java.awt.Color[] COLORES = {
        new java.awt.Color(255, 99, 71),   // Tomate
        new java.awt.Color(60, 179, 113),  // Verde mar
        new java.awt.Color(65, 105, 225),  // Azul real
        new java.awt.Color(255, 165, 0),   // Naranja
        new java.awt.Color(138, 43, 226),  // Violeta
        new java.awt.Color(0, 206, 209),   // Turquesa
        new java.awt.Color(255, 20, 147),  // Rosa
        new java.awt.Color(154, 205, 50),  // Verde amarillo
        new java.awt.Color(255, 215, 0),   // Oro
        new java.awt.Color(70, 130, 180)   // Acero azul
    };
    private static int contadorColor = 0;

    public Archivo(String nombre, int tamanoEnBloques, int direccionPrimerBloque) {
        this.nombre = nombre;
        this.tamanoEnBloques = tamanoEnBloques;
        this.direccionPrimerBloque = direccionPrimerBloque;
        this.color = COLORES[contadorColor % COLORES.length];
        contadorColor++;
        this.propietario = "admin";
    }
    
    public Archivo(String nombre, int tamanoEnBloques, int direccionPrimerBloque, String propietario) {
        this.nombre = nombre;
        this.tamanoEnBloques = tamanoEnBloques;
        this.direccionPrimerBloque = direccionPrimerBloque;
        this.color = COLORES[contadorColor % COLORES.length];
        contadorColor++;
        this.propietario = propietario;
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
    
    public java.awt.Color getColor() {
        return color;
    }
    
    public void setColor(java.awt.Color color) {
        this.color = color;
    }
    
    public String getPropietario() {
        return propietario;
    }
    
    public void setPropietario(String propietario) {
        this.propietario = propietario;
    }

    @Override
    public String toString() {
        return nombre; 
    }
}
