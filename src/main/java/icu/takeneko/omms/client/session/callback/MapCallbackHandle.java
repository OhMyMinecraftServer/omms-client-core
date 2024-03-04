package icu.takeneko.omms.client.session.callback;

import com.google.gson.reflect.TypeToken;

import java.util.Map;
import java.util.function.Consumer;

public class MapCallbackHandle<K, V> extends JsonObjectCallbackHandle<Map<K, V>> {
    public MapCallbackHandle(String key, Consumer<Map<K, V>> fn) {
        super(key, fn);
    }

    @Override
    protected TypeToken<Map<K, V>> getObjectType() {
        return new TypeToken<Map<K, V>>() {
        };
    }
}
