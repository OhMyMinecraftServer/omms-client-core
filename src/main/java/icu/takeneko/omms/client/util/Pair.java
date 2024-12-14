package icu.takeneko.omms.client.util;

import lombok.Getter;

@Getter
public class Pair<K, V> {
    K a;
    V b;

    public Pair(K a, V b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "a=" + a.toString() +
                ", b=" + b.toString() +
                '}';
    }

    public static <K,V> Pair<K,V> of(K a, V b){
        return new Pair<>(a, b);
    }
}
