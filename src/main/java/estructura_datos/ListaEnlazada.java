package estructura_datos;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author Diego A. Vivolo
 */

public class ListaEnlazada<T> implements Iterable<T> {

    private Nodo<T> head;
    private Nodo<T> tail;
    private int size;

    public ListaEnlazada() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    public void add(T data) {
        Nodo<T> newNode = new Nodo<>(data);

        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        } else {
    
            tail.setSiguiente(newNode); 
            tail = newNode;
        }
        size++;
    }

    public boolean remove(T data) {
        if (isEmpty()) {
            return false;
        }

       
        if (head.getDato().equals(data)) {
          
            head = head.getSiguiente(); 

            if (head == null) {
                tail = null;
            }
            size--;
            return true;
        }

        
        Nodo<T> current = head; 

      
        while (current.getSiguiente() != null && !current.getSiguiente().getDato().equals(data)) {

            current = current.getSiguiente(); 
        }

        
        if (current.getSiguiente() == null) {
            return false;
        }

      
        Nodo<T> nodeToRemove = current.getSiguiente();

        if (nodeToRemove == tail) {
            tail = current;
        }

        current.setSiguiente(nodeToRemove.getSiguiente());

        size--;
        return true;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Índice: " + index + ", Tamaño: " + size);
        }
        

        Nodo<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getSiguiente();
        }
        return current.getDato();
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<T> iterator() {

        return new Iterator<T>() {

            private Nodo<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T data = current.getDato();
                current = current.getSiguiente();
                return data;
            }
        };
    }
}