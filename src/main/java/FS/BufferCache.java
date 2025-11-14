
package FS;

import estructura_datos.Cola;
import estructura_datos.ListaEnlazada;
import modelo.Bloque;
import modelo.SD;
/**
 *
 * @author Diego A. Vivolo
 */
public class BufferCache {

    public class EntradaCache {
        private int indiceBloque;
        private Bloque bloque;
        
        public EntradaCache(int indiceBloque, Bloque bloque) {
            this.indiceBloque = indiceBloque;
            this.bloque = bloque;
        }
        
        public int getIndiceBloque() { return indiceBloque; }
        public Bloque getBloque() { return bloque; }

        @Override
        public String toString() {
            return "[B: " + indiceBloque + "]";
        }
    }

    private int tamanoMaximo;
    private SD discoDuro;
    
    private Cola<EntradaCache> cacheFIFO;

    private ListaEnlazada<EntradaCache> cacheContenido;

    public BufferCache(int tamanoMaximo, SD discoDuro) {
        this.tamanoMaximo = (tamanoMaximo <= 0) ? 5 : tamanoMaximo; 
        this.discoDuro = discoDuro;
        this.cacheFIFO = new Cola<>();
        this.cacheContenido = new ListaEnlazada<>();
    }


    public Bloque leerBloque(int indiceBloque) {
        
        Bloque bloqueEnCache = buscarEnCache(indiceBloque);
        
        if (bloqueEnCache != null) {

            return bloqueEnCache;
            
        } else {

            Bloque bloqueDeDisco = discoDuro.getBloques()[indiceBloque];
            
            agregarAlCache(indiceBloque, bloqueDeDisco);

            return bloqueDeDisco;
        }
    }


    private Bloque buscarEnCache(int indiceBloque) {
        for (EntradaCache entrada : cacheContenido) {
            if (entrada.getIndiceBloque() == indiceBloque) {
                return entrada.getBloque(); 
            }
        }
        return null;
    }
    

    private void agregarAlCache(int indiceBloque, Bloque bloque) {
        
        if (cacheContenido.size() >= tamanoMaximo) {

            EntradaCache entradaAReemplazar = cacheFIFO.desencolar();
            
            cacheContenido.remove(entradaAReemplazar);
            
        }

        EntradaCache nuevaEntrada = new EntradaCache(indiceBloque, bloque);
        cacheFIFO.encolar(nuevaEntrada);
        cacheContenido.add(nuevaEntrada);
        

    }
    

    public ListaEnlazada<EntradaCache> getCacheContenido() {
        return this.cacheContenido;
    }
    
    public int getTamanoMaximo() {
        return this.tamanoMaximo;
    }
}