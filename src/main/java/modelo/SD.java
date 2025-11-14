package modelo;

/**
 *
 * @author Diego A. Vivolo
 */
public class SD {
   

    private Bloque[] bloques;
    private int tamanoTotal;
    private int bloquesLibres;


    public SD(int tamanoTotal) {
        this.tamanoTotal = tamanoTotal;
        this.bloquesLibres = tamanoTotal;
        this.bloques = new Bloque[tamanoTotal];

        for (int i = 0; i < tamanoTotal; i++) {
            this.bloques[i] = new Bloque();
        }
    }

    public int alocarBloques(String nombreArchivo, int numBloques) {

        if (numBloques > this.bloquesLibres) {
            System.err.println("Error de disco: Espacio insuficiente.");
            return -1; 
        }

        int indicePrimerBloque = -1;
        int indiceBloqueAnterior = -1;

     
        for (int i = 0; i < numBloques; i++) {
           
            int indiceNuevoBloque = encontrarSiguienteBloqueLibre();
            
          
            if (indiceNuevoBloque == -1) { 
  
                 System.err.println("Error crítico: No se encontraron bloques libres.");
                 return -1;
            }

            Bloque bloqueActual = this.bloques[indiceNuevoBloque];

            if (i == 0) {
            
                indicePrimerBloque = indiceNuevoBloque;
            } else {
            
                this.bloques[indiceBloqueAnterior].setSiguienteBloque(indiceNuevoBloque);
            }
            
         
            bloqueActual.Ocupar(nombreArchivo, -1); 
            
            
            indiceBloqueAnterior = indiceNuevoBloque;
            this.bloquesLibres--;
        }
        return indicePrimerBloque;
    }

    public void liberarBloques(int indicePrimerBloque) {
        int indiceActual = indicePrimerBloque;

        while (indiceActual != -1) {
            Bloque bloque = this.bloques[indiceActual];
            

            int indiceSiguiente = bloque.getSiguienteBloque();
            
            bloque.liberar();
            this.bloquesLibres++;

            indiceActual = indiceSiguiente;
        }
    }

    private int encontrarSiguienteBloqueLibre() {
        for (int i = 0; i < this.tamanoTotal; i++) {
            if (this.bloques[i].isEstaLibre()) {
                return i;
            }
        }
        return -1; 
    }

    public Bloque[] getBloques() {
        return this.bloques;
    }

    public int getTamanoTotal() {
        return tamanoTotal;
    }

    public int getBloquesLibres() {
        return bloquesLibres;
    }
}
    
    
