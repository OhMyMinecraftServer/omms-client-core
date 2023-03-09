package net.zhuruoling.omms.client.response.callback;

@FunctionalInterface
public interface Callback <T>{
    void accept(T value);
}
