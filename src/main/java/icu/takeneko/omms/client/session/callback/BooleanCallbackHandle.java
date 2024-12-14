package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.data.SessionContext;

public class BooleanCallbackHandle extends CallbackHandle1<Boolean, SessionContext> {
    public BooleanCallbackHandle(String key, Callback<Boolean> fn) {
        super(key, fn);
    }

    @Override
    protected Boolean parse(SessionContext context) {
        return Boolean.parseBoolean(context.getContent(key));
    }
}
