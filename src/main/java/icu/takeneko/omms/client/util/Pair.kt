package icu.takeneko.omms.client.util

class Pair<K, V>(var a: K, var b: V) {
    override fun toString(): String {
        return "Pair{" +
                "a=" + a.toString() +
                ", b=" + b.toString() +
                '}'
    }
}
