package icu.takeneko.omms.client.response;

@FunctionalInterface
public interface Callback <T>{
    void accept(T value);
}
