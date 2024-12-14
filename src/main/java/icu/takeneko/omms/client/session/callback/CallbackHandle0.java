package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.handler.CallbackHandle;

public class CallbackHandle0<C> extends CallbackHandle<C> {
    private final Callback0 callback0;

    public CallbackHandle0(Callback0 callback0) {
        this.callback0 = callback0;
    }

    @Override
    public void invoke(C context) {
        callback0.accept();
    }
}
