package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.SessionContext;

public class StringCallbackHandle extends CallbackHandle1<String, SessionContext> {
    public StringCallbackHandle(String key, Callback<String> fn) {
        super(key, fn);
    }

    @Override
    protected String parse(SessionContext context) {
        return context.getContent(key);
    }
}
