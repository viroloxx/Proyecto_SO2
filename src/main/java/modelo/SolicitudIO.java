package modelo;

/**
 *
 * @author Diego A. Vivolo
 */
public class SolicitudIO {
    private final TipoOperacion tipo;
    private final String nombreArchivo;
    private final int tamanoEnBloques;
    private final String nuevoNombre;
    private final int bloqueObjetivo;
    private final String propietario;

    public SolicitudIO(TipoOperacion tipo, String nombreArchivo, int tamanoEnBloques, int bloqueObjetivo) {
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.tamanoEnBloques = tamanoEnBloques;
        this.bloqueObjetivo = bloqueObjetivo;
        this.nuevoNombre = null;
        this.propietario = "admin";
    }

    public SolicitudIO(TipoOperacion tipo, String nombreArchivo, int tamanoEnBloques, int bloqueObjetivo, String propietario) {
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.tamanoEnBloques = tamanoEnBloques;
        this.bloqueObjetivo = bloqueObjetivo;
        this.nuevoNombre = null;
        this.propietario = propietario;
    }

    public SolicitudIO(TipoOperacion tipo, String nombreArchivo, int bloqueObjetivo) {
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.tamanoEnBloques = 0;
        this.bloqueObjetivo = bloqueObjetivo;
        this.nuevoNombre = null;
        this.propietario = "admin";
    }

    public SolicitudIO(TipoOperacion tipo, String nombreArchivo, String nuevoNombre, int bloqueObjetivo) {
        this.tipo = tipo;
        this.nombreArchivo = nombreArchivo;
        this.nuevoNombre = nuevoNombre;
        this.tamanoEnBloques = 0;
        this.bloqueObjetivo = bloqueObjetivo;
        this.propietario = "admin";
    }

    public TipoOperacion getTipo() { return tipo; }
    public String getNombreArchivo() { return nombreArchivo; }
    public int getTamanoEnBloques() { return tamanoEnBloques; }
    public String getNuevoNombre() { return nuevoNombre; }
    public int getBloqueObjetivo() { return bloqueObjetivo; }
    public String getPropietario() { return propietario; }

    @Override
    public String toString() {
        String base;
        switch(tipo) {
            case CREAR_ARCHIVO:
                base = String.format("CREAR %s (%d bloques)", nombreArchivo, tamanoEnBloques);
                break;
            case LEER_ARCHIVO:
                base = "LEER " + nombreArchivo;
                break;
            case ELIMINAR_ARCHIVO:
                base = "ELIMINAR " + nombreArchivo;
                break;
            case ACTUALIZAR_ARCHIVO:
                base = String.format("RENOMBRAR %s -> %s", nombreArchivo, nuevoNombre);
                break;
            default:
                base = tipo.name() + " " + nombreArchivo;
        }
        return String.format("%s [Bloque: %d]", base, bloqueObjetivo);
    }
}
