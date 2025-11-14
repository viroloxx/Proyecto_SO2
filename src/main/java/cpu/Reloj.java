
package cpu;

/**
 *
 * @author Diego A. Vivolo
 */
public class Reloj {
    private int cicloActual;
    private int duracionCicloMs;
    private boolean pausado;
    
    public Reloj(int duracionCicloMs) {
        this.cicloActual = 0;
        this.duracionCicloMs = duracionCicloMs;
        this.pausado = false;
    }
    
    public void incrementarCiclo() {
        cicloActual++;
    }
    
    public void reiniciar() {
        cicloActual = 0;
    }
    
    public int getCicloActual() {
        return cicloActual;
    }
    
    public void pausar() {
        this.pausado = true;
    }
    
    public void reanudar() {
        this.pausado = false;
    }
    
    public boolean estaPausado() {
        return pausado;
    }
    
    public void esperarCiclo() {
        try {
            Thread.sleep(duracionCicloMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}