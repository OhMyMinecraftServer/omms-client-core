package icu.takeneko.omms.client.session.callback;

@FunctionalInterface
public interface Callback<T> {
    void accept(T value);
}
