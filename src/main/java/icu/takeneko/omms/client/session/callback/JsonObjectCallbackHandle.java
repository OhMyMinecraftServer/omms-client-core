package icu.takeneko.omms.client.session.callback;

import com.google.gson.reflect.TypeToken;
import icu.takeneko.omms.client.session.SessionContext;
import icu.takeneko.omms.client.util.Util;

public abstract class JsonObjectCallbackHandle<T> extends CallbackHandle1<T, SessionContext> {
    public JsonObjectCallbackHandle(String key, Callback<T> fn) {
        super(key, fn);
    }

    protected abstract TypeToken<T> getObjectType();

    @Override
    protected final T parse(SessionContext context) {
        TypeToken<?> tt = getObjectType();
        return Util.getGson().fromJson(context.getContent(key), tt.getType());
    }
}
