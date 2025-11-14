package modelo;

public enum EstadoProceso {
    NUEVO, LISTO, EJECUCION, BLOQUEADO, SUSPENDIDO_LISTO, SUSPENDIDO_BLOQUEADO, TERMINADO;
    
    @Override
    public String toString() {
        switch(this) {
            case NUEVO: return "Nuevo";
            case LISTO: return "Listo";
            case EJECUCION: return "Ejecución";
            case BLOQUEADO: return "Bloqueado";
            case SUSPENDIDO_LISTO: return "Susp-Listo";
            case SUSPENDIDO_BLOQUEADO: return "Susp-Bloq";
            case TERMINADO: return "Terminado";
            default: return "Desconocido";
        }
    }
}
