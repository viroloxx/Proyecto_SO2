package cpu;


import modelo.PCB;
import modelo.EstadoProceso;
/**
 *
 * @author Diego A. Vivolo
 */

public class CPU {
    private PCB procesoActual;
    
    public CPU() {
        this.procesoActual = null;
    }
    
    public void asignarProceso(PCB proceso) {
        this.procesoActual = proceso;
        if (proceso != null) {
            proceso.setEstado(EstadoProceso.EJECUCION);
        }
    }
    
    public PCB liberarProceso() {
        PCB temp = procesoActual;
        procesoActual = null;
        return temp;
    }
    
    public PCB getProcesoActual() {
        return procesoActual;
    }
    
    public boolean estaOcupada() {
        return procesoActual != null;
    }
}