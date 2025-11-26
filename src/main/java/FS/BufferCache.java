
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

    // Estadísticas de rendimiento del cache
    private int cacheHits;      // Accesos encontrados en cache
    private int cacheMisses;    // Accesos que requirieron disco

    public BufferCache(int tamanoMaximo, SD discoDuro) {
        this.tamanoMaximo = (tamanoMaximo <= 0) ? 5 : tamanoMaximo;
        this.discoDuro = discoDuro;
        this.cacheFIFO = new Cola<>();
        this.cacheContenido = new ListaEnlazada<>();
        this.cacheHits = 0;
        this.cacheMisses = 0;
    }


    public Bloque leerBloque(int indiceBloque) {
        // Validar índice de bloque
        if (indiceBloque < 0 || indiceBloque >= discoDuro.getTamanoTotal()) {
            System.err.println("ERROR BufferCache: Índice de bloque fuera de rango: " + indiceBloque);
            return null;
        }

        Bloque bloqueEnCache = buscarEnCache(indiceBloque);

        if (bloqueEnCache != null) {
            // Cache HIT: bloque encontrado en cache (no acceso a disco)
            cacheHits++;
            return bloqueEnCache;

        } else {
            // Cache MISS: se debe leer del disco
            cacheMisses++;
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

    public int getCacheHits() {
        return this.cacheHits;
    }

    public int getCacheMisses() {
        return this.cacheMisses;
    }

    public int getTotalAccesos() {
        return this.cacheHits + this.cacheMisses;
    }

    public double getTasaAciertos() {
        int total = getTotalAccesos();
        if (total == 0) return 0.0;
        return (double) cacheHits / total * 100.0;
    }

    public String getEstadisticas() {
        return String.format("Hits: %d | Misses: %d | Total: %d | Tasa: %.1f%%",
                cacheHits, cacheMisses, getTotalAccesos(), getTasaAciertos());
    }
}