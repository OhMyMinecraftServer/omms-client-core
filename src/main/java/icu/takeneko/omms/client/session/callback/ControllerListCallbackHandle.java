package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.data.controller.Controller;
import icu.takeneko.omms.client.session.data.SessionContext;
import icu.takeneko.omms.client.util.Pair;
import icu.takeneko.omms.client.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static icu.takeneko.omms.client.util.Util.gson;

public class ControllerListCallbackHandle extends CallbackHandle1<Map<String, Controller>, SessionContext> {

    public ControllerListCallbackHandle(Callback<Map<String, Controller>> fn) {
        super("names", fn);
    }

    @Override
    protected Map<String, Controller> parse(SessionContext context) {
        String controllerListString = context.getContent(key);
        if (controllerListString == null) return null;
        List<String> controllerNames = Arrays.asList(gson.fromJson(controllerListString, String[].class));
        context.getSession().getControllerMap().clear();
        return controllerNames.stream()
            .map(it -> Pair.of(it, gson.fromJson(context.getContent(it), Controller.class)))
            .collect(Util.toMapCollector());
    }
}
