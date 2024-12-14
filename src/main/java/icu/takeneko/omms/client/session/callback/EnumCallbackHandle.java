package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.data.SessionContext;

import java.util.function.Function;

public class EnumCallbackHandle<E extends Enum<E>> extends CallbackHandle1<E, SessionContext>{
    private final Function<String, E> remappingFunction;

    public EnumCallbackHandle(String key, Function<String, E> remappingFunction, Callback<E> fn) {
        super(key, fn);
        this.remappingFunction = remappingFunction;
    }

    @Override
    protected E parse(SessionContext context) {
        String k = context.getContent(key);
        return remappingFunction.apply(k);
    }
}
