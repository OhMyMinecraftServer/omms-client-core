package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.SessionContext;

import java.util.function.BiConsumer;

public class BiStringCallbackHandle extends CallbackHandle2<String, String, SessionContext> {

    public BiStringCallbackHandle(String key1, String key2, BiConsumer<String, String> fn) {
        super(key1, key2, fn);
    }

    @Override
    protected String parse1(SessionContext context) {
        return context.getContent(key1);
    }

    @Override
    protected String parse2(SessionContext context) {
        return context.getContent(key2);
    }
}
