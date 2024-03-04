package icu.takeneko.omms.client.session.callback;

import com.google.gson.reflect.TypeToken;
import icu.takeneko.omms.client.controller.Controller;
import icu.takeneko.omms.client.request.Request;
import icu.takeneko.omms.client.response.Response;
import icu.takeneko.omms.client.session.SessionContext;
import icu.takeneko.omms.client.util.Result;

import java.util.Map;
import java.util.function.Consumer;

import static icu.takeneko.omms.client.util.Util.gson;

public class ControllerListCallbackHandle extends CallbackHandle1<Map<String, Controller>, SessionContext> {

    public ControllerListCallbackHandle(String key, Consumer<Map<String, Controller>> fn) {
        super(key, fn);
    }

    @Override
    protected Map<String, Controller> parse(SessionContext context) {

        String names = context.getContent("names");
        if (names == null) return null;
        String[] controllerNames = gson.fromJson(names, new TypeToken<String[]>() {
        }.getType());
        boolean hasError = false;
        RuntimeException re = new RuntimeException();
        for (String controllerName : controllerNames) {
            try {
                Response response1 = context.getSession().sendBlocking(new Request("CONTROLLER_GET").withContentKeyPair("controller", controllerName));
                context.getSession().getControllerMap().clear();
                if (response1.getResponseCode() == Result.CONTROLLER_GOT) {
                    String jsonString = response1.getContent("controller");
                    Controller controller = gson.fromJson(jsonString, Controller.class);
                    context.getSession().getControllerMap().put(controllerName, controller);
                }
            } catch (Throwable e) {
                re.addSuppressed(e);
                hasError = true;
            }
        }
        if (hasError) throw re;
        return context.getSession().getControllerMap();
    }
}
