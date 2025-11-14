package Persistencia;

import modelo.PlantillaProceso;


class ConfiguracionJSON {
    private ConfiguracionSistema configuracionSistema;
    private PlantillaProceso[] procesos;

    public ConfiguracionJSON() {
    }

    public ConfiguracionSistema getConfiguracionSistema() {
        return configuracionSistema;
    }

    public void setConfiguracionSistema(ConfiguracionSistema configuracionSistema) {
        this.configuracionSistema = configuracionSistema;
    }

    public PlantillaProceso[] getProcesos() {
        return procesos;
    }

    public void setProcesos(PlantillaProceso[] procesos) {
        this.procesos = procesos;
    }
}
