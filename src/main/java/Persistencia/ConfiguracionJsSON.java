package Persistencia;

import modelo.PlantillaProceso;

/**
 * @author Diego A. Vivolo
 */
class ConfiguracionJSON {
    // TODO: Implementar ConfiguracionSistema si es necesario
    private PlantillaProceso[] procesos;

    public ConfiguracionJSON() {
    }

    public PlantillaProceso[] getProcesos() {
        return procesos;
    }

    public void setProcesos(PlantillaProceso[] procesos) {
        this.procesos = procesos;
    }
}
