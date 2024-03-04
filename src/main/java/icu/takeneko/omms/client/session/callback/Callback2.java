package icu.takeneko.omms.client.session.callback;

@FunctionalInterface
public interface Callback2<T, U> {
    void accept(T value, U value2);
}
