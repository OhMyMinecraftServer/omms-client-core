package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.ClientSession;
import icu.takeneko.omms.client.session.handler.CallbackHandle;
import lombok.Setter;

@Setter
public class PermissionDeniedCallbackHandle<C> extends CallbackHandle<C> {

    private Callback<C> callback = this::handleNothing;

    @Override
    public void invoke(C context) {
        callback.accept(context);
    }

    public void handleNothing(C context){

    }
}
