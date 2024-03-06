package icu.takeneko.omms.client.session.callback;

import com.google.gson.reflect.TypeToken;
import icu.takeneko.omms.client.session.SessionContext;

import java.util.function.Consumer;

import static icu.takeneko.omms.client.util.Util.gson;

public class JsonObjectCallbackHandle<T> extends CallbackHandle1<T, SessionContext> {
    public JsonObjectCallbackHandle(String key, Callback<T> fn) {
        super(key, fn);
    }

    protected TypeToken<T> getObjectType(){
        return new TypeToken<T>(){};
    }

    @Override
    protected final T parse(SessionContext context) {
        return gson.fromJson(context.getContent(key), getObjectType().getType());
    }
}
