package icu.takeneko.omms.client.session.callback;

import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class MapCallbackHandle<K, V> extends JsonObjectCallbackHandle<Map<K, V>> {
    public MapCallbackHandle(String key, Callback<Map<K, V>> fn) {
        super(key, fn);
    }
}
