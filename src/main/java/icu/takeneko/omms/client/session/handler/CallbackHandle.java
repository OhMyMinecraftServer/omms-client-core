package icu.takeneko.omms.client.session.handler;

public abstract class CallbackHandle<C> {

    abstract public void invoke(C context);
}
