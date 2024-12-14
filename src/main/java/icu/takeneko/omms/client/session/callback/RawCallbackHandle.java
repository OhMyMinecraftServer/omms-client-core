package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.handler.CallbackHandle;

public class RawCallbackHandle<C> extends CallbackHandle<C> {
    private final Callback<C> callback;

    public RawCallbackHandle(Callback<C> callback) {
        this.callback = callback;
    }

    @Override
    public void invoke(C context) {
        callback.accept(context);
    }
}
