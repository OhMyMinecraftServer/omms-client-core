package icu.takeneko.omms.client.session.handler;

public interface CallbackHandle<C> {
    void invoke(C context);
}
