package net.zhuruoling.omms.client.response;

@FunctionalInterface
public interface Callback <T>{
    void accept(T value);
}
