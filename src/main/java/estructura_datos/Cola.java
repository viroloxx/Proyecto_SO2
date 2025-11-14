package estructura_datos;


import java.util.Iterator;
import java.util.NoSuchElementException;


public class Cola<T> implements Iterable<T> { 
    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamanio;
    
    public Cola() {
        this.frente = null;
        this.fin = null;
        this.tamanio = 0;
    }
    

    public synchronized void encolar(T dato) {
        Nodo<T> nuevoNodo = new Nodo<>(dato);
        if (estaVacia()) {
            frente = nuevoNodo;
            fin = nuevoNodo;
        } else {
            fin.setSiguiente(nuevoNodo);
            fin = nuevoNodo;
        }
        tamanio++;
    }
    
    public synchronized T desencolar() {
        if (estaVacia()) return null;
        T dato = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null) fin = null;
        tamanio--;
        return dato;
    }

    public T verFrente() {
        return estaVacia() ? null : frente.getDato();
    }
    
    public boolean estaVacia() {
        return frente == null;
    }
    
    public int obtenerTamanio() {
        return tamanio;
    }
    

    public synchronized Object[] toArray() {
        if (estaVacia()) return new Object[0];
        
        Object[] arreglo = new Object[tamanio];
        Nodo<T> actual = frente;
        int indice = 0;
        while (actual != null) {
            arreglo[indice++] = actual.getDato();
            actual = actual.getSiguiente();
        }
        return arreglo;
    }
    
    public synchronized void fromArray(Object[] arreglo) {
        limpiar();
        for (Object obj : arreglo) {
            if (obj != null) {
                encolar((T) obj); 
            }
        }
    }
    
    public synchronized void limpiar() {
        frente = null;
        fin = null;
        tamanio = 0;
    }


    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Nodo<T> actual = frente;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (!hasNext()) throw new NoSuchElementException();
                T dato = actual.getDato();
                actual = actual.getSiguiente();
                return dato;
            }
        };
    }
    
    @Override
    public String toString() {
        if (estaVacia()) return "[]";
        
        StringBuilder sb = new StringBuilder("[");

        for (T dato : this) { 
            sb.append(dato.toString());
            sb.append(", ");
        }
       
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("]");
        return sb.toString();
    }


}