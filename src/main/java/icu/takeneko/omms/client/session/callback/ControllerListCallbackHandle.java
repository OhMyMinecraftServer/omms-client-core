package icu.takeneko.omms.client.session.callback;

import com.google.gson.reflect.TypeToken;
import icu.takeneko.omms.client.data.controller.Controller;
import icu.takeneko.omms.client.session.SessionContext;
import icu.takeneko.omms.client.session.handler.CallbackHandle;
import icu.takeneko.omms.client.session.request.Request;
import icu.takeneko.omms.client.util.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

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
        List<String> a = new ArrayList<>(controllerNames);
        String id = Long.toString(System.nanoTime());
        CallbackHandle<SessionContext> handle = new JsonObjectCallbackHandle<Controller>("controller", s -> {
            context.getSession().getControllerMap().put(s.getId(), s);
            a.remove(s.getId());
        }) {
            @Override
            protected TypeToken<Controller> getObjectType() {
                return TypeToken.get(Controller.class);
            }
        };
        CallbackHandle<SessionContext> notExist = new StringCallbackHandle("controllerId", a::remove);
        handle.setAssociateGroupId(id);
        notExist.setAssociateGroupId(id);
        context.getSession().getDelegate().register(Result.CONTROLLER_GOT, handle, false);
        context.getSession().getDelegate().register(Result.CONTROLLER_NOT_EXIST, notExist, false);
        for (String name : controllerNames) {
            Request request = new Request("CONTROLLER_GET").withContentKeyPair("controller", name);
            context.getSession().send(request);
        }
        while (!a.isEmpty()){
            LockSupport.parkNanos(1);
        }
        context.getSession().getDelegate().removeAssocGroup(id);
        return context.getSession().getControllerMap();
    }
}
