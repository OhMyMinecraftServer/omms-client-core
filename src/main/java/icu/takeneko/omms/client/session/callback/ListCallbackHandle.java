package icu.takeneko.omms.client.session.callback;

import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ListCallbackHandle<E> extends JsonObjectCallbackHandle<E[]> {

    private final Callback<List<E>> fn;

    public ListCallbackHandle(String key, Callback<List<E>> fn) {
        super(key, (es -> fn.accept(Arrays.asList(es))));
        this.fn = fn;
    }
}
