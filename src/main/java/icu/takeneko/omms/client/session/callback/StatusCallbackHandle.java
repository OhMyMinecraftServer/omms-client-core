package icu.takeneko.omms.client.session.callback;

import com.google.gson.reflect.TypeToken;
import icu.takeneko.omms.client.data.controller.Status;

public class StatusCallbackHandle extends JsonObjectCallbackHandle<Status> {
    public StatusCallbackHandle(Callback<Status> fn) {
        super("status", fn);
    }

    @Override
    protected TypeToken<Status> getObjectType() {
        return TypeToken.get(Status.class);
    }
}
