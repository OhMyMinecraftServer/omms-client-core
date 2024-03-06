package icu.takeneko.omms.client.session.callback;

import com.google.gson.reflect.TypeToken;
import icu.takeneko.omms.client.controller.Status;

import java.util.function.Consumer;

public class StatusCallbackHandle extends JsonObjectCallbackHandle<Status> {
    public StatusCallbackHandle(Callback<Status> fn) {
        super("status", fn);
    }
}
