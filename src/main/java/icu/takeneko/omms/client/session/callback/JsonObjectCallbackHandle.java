package icu.takeneko.omms.client.session.callback;

import com.google.gson.reflect.TypeToken;
import icu.takeneko.omms.client.session.data.SessionContext;

import static icu.takeneko.omms.client.util.Util.gson;

public abstract class JsonObjectCallbackHandle<T> extends CallbackHandle1<T, SessionContext> {
    public JsonObjectCallbackHandle(String key, Callback<T> fn) {
        super(key, fn);
    }

    protected abstract TypeToken<T> getObjectType();

    @Override
    protected final T parse(SessionContext context) {
        TypeToken<?> tt = getObjectType();
        return gson.fromJson(context.getContent(key), tt.getType());
    }
}
