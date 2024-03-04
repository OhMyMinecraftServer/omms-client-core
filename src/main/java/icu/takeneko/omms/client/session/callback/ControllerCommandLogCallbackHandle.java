package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.SessionContext;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class ControllerCommandLogCallbackHandle extends CallbackHandle2<String, List<String>, SessionContext> {
    public ControllerCommandLogCallbackHandle(String key1, String key2, BiConsumer<String, List<String>> fn) {
        super(key1, key2, fn);
    }

    @Override
    protected String parse1(SessionContext context) {
        return context.getContent("controllerId");
    }

    @Override
    protected List<String> parse2(SessionContext context) {
        return Arrays.asList(context.getContent("output").split("\n"));
    }
}
