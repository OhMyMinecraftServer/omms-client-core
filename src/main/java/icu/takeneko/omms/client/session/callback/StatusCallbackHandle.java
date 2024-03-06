package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.data.controller.Status;

public class StatusCallbackHandle extends JsonObjectCallbackHandle<Status> {
    public StatusCallbackHandle(Callback<Status> fn) {
        super("status", fn);
    }
}
