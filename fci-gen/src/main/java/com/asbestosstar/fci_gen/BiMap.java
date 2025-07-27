package com.asbestosstar.fci_gen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class ParValores<V1, V2> {
    private V1 valor1;
    private V2 valor2;

    public ParValores(V1 valor1, V2 valor2) {
        this.valor1 = valor1;
        this.valor2 = valor2;
    }

    public V1 getValor1() {
        return valor1;
    }

    public V2 getValor2() {
        return valor2;
    }

    @Override
    public String toString() {
        return "(" + valor1 + ", " + valor2 + ")";
    }
}

public class BiMap<K, V1, V2> implements Iterable<BiMap.Entry<K, V1, V2>> {
    private Map<K, ParValores<V1, V2>> mapa;

    public BiMap() {
        mapa = new HashMap<>();
    }

    public void poner(K clave, V1 valor1, V2 valor2) {
        mapa.put(clave, new ParValores<>(valor1, valor2));
    }

    public ParValores<V1, V2> obtenerPorClave(K clave) {
        return mapa.get(clave);
    }

    public void eliminarPorClave(K clave) {
        mapa.remove(clave);
    }

    @Override
    public String toString() {
        return mapa.toString();
    }

    // Clase interna Entry
    public static class Entry<K, V1, V2> {
        private K clave;
        private ParValores<V1, V2> valores;

        public Entry(K clave, ParValores<V1, V2> valores) {
            this.clave = clave;
            this.valores = valores;
        }

        public K getClave() {
            return clave;
        }

        public ParValores<V1, V2> getValores() {
            return valores;
        }

        @Override
        public String toString() {
            return "Entry{" + "clave=" + clave + ", valores=" + valores + '}';
        }
    }

    // Implementación del iterador
    @Override
    public Iterator<Entry<K, V1, V2>> iterator() {
        return new Iterator<Entry<K, V1, V2>>() {
            private final Iterator<Map.Entry<K, ParValores<V1, V2>>> iterador = mapa.entrySet().iterator();

            @Override
            public boolean hasNext() {
                return iterador.hasNext();
            }

            @Override
            public Entry<K, V1, V2> next() {
                Map.Entry<K, ParValores<V1, V2>> entry = iterador.next();
                return new Entry<>(entry.getKey(), entry.getValue());
            }
        };
    }

}
