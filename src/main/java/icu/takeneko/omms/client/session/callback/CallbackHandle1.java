package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.handler.CallbackHandle;

import java.util.function.Consumer;

public abstract class CallbackHandle1<T, C> implements CallbackHandle<C> {

    private final Consumer<T> fn;
    protected final String key;

    public CallbackHandle1(String key, Consumer<T> fn) {
        this.fn = fn;
        this.key = key;
    }

    @Override
    public void invoke(C context) {
        if (fn != null) {
            fn.accept(parse(context));
        }
    }

    abstract protected T parse(C context);
}
