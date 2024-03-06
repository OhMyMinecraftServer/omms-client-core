package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.handler.CallbackHandle;

public abstract class CallbackHandle1<T, C> extends CallbackHandle<C> {

    private final Callback<T> fn;
    protected final String key;
    protected CallbackHandle<C> associatedHandle;

    public CallbackHandle1(String key, Callback<T> fn) {
        this.fn = fn;
        this.key = key;
        associatedHandle = null;
    }

    public CallbackHandle1(Callback<T> fn, String key, CallbackHandle<C> associatedHandle) {
        this.fn = fn;
        this.key = key;
        this.associatedHandle = associatedHandle;
    }

    @Override
    public void invoke(C context) {
        if (fn != null) {
            fn.accept(parse(context));
        }
    }

    abstract protected T parse(C context);
}
