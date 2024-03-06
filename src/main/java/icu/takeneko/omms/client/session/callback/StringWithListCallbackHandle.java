package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.SessionContext;

import java.util.Arrays;
import java.util.List;

import static icu.takeneko.omms.client.util.Util.gson;

public class StringWithListCallbackHandle extends CallbackHandle2<String, List<String>, SessionContext>{

    public StringWithListCallbackHandle(String key1, String key2, Callback2<String, List<String>> fn) {
        super(key1, key2, fn);
    }

    @Override
    protected String parse1(SessionContext context) {
        return context.getContent(key1);
    }

    @Override
    protected List<String> parse2(SessionContext context) {
        return Arrays.asList(gson.fromJson(context.getContent("players"), String[].class));
    }
}
