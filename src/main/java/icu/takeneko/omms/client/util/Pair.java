package icu.takeneko.omms.client.util;

public class Pair <K,V> {
    K a;
    V b;

    public Pair(K a, V b) {
        this.a = a;
        this.b = b;
    }

    public K getA() {
        return a;
    }

    public V getB() {
        return b;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "a=" + a.toString() +
                ", b=" + b.toString() +
                '}';
    }
}
