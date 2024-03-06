package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.handler.CallbackHandle;

public abstract class CallbackHandle2<T, U, C> extends CallbackHandle<C> {

    private final Callback2<T, U> fn;
    protected final String key1;
    protected final String key2;

    protected CallbackHandle<C> associatedHandle;

    public CallbackHandle2(String key1, String key2, Callback2<T, U> fn) {
        this.fn = fn;
        this.key1 = key1;
        this.key2 = key2;
    }

    @Override
    public void invoke(C context) {
        if (fn != null) {
            fn.accept(parse1(context), parse2(context));
        }
    }

    abstract protected T parse1(C context);

    abstract protected U parse2(C context);
}
