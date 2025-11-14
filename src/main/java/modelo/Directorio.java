package modelo;

/**
 *
 * @author Diego A. Vivolo
 */
import estructura_datos.ListaEnlazada;

public class Directorio {

    private String nombre;
    
    private ListaEnlazada<Object> hijos;


    public Directorio(String nombre) {
        this.nombre = nombre;
        
        this.hijos = new ListaEnlazada<>();
    }



    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public ListaEnlazada<Object> getHijos() {
        return this.hijos;
    }


    public void agregarArchivo(Archivo archivo) {
        this.hijos.add(archivo);
    }


    public void agregarSubdirectorio(Directorio subDirectorio) {
        this.hijos.add(subDirectorio);
    }

    public boolean eliminarHijo(Object hijo) {
        return this.hijos.remove(hijo);
    }
    
    public Object buscarHijo(String nombre) {

        for (Object hijo : hijos) {
            String nombreHijo;
            if (hijo instanceof Archivo) {
                nombreHijo = ((Archivo) hijo).getNombre();
            } else if (hijo instanceof Directorio) {
                nombreHijo = ((Directorio) hijo).getNombre();
            } else {
                continue;
            }
            
            if (nombreHijo.equals(nombre)) {
                return hijo; 
            }
        }
        return null; 
    }
    @Override
    public String toString() {
        return this.nombre;
    }
}