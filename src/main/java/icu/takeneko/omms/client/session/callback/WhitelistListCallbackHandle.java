package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.SessionContext;
import icu.takeneko.omms.client.session.handler.CallbackHandle;
import icu.takeneko.omms.client.session.request.Request;
import icu.takeneko.omms.client.util.Result;
import icu.takeneko.omms.client.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;

public class WhitelistListCallbackHandle extends CallbackHandle1<Map<String, List<String>>, SessionContext> {

    public WhitelistListCallbackHandle(Callback<Map<String, List<String>>> fn) {
        super("whitelists", fn);
    }

    @Override
    protected Map<String, List<String>> parse(SessionContext context) {
        String whitelists = context.getContent("whitelists");
        if (whitelists == null) return null;
        List<String> whitelistNames = Arrays.asList(Util.getGson().fromJson(whitelists, String[].class));
        context.session.getWhitelistMap().clear();
        List<String> a = new ArrayList<>(whitelistNames);
        String id = Long.toString(System.nanoTime());
        CallbackHandle<SessionContext> handle = new StringWithListCallbackHandle("whitelist", "players", (s, l) -> {
            context.session.getWhitelistMap().put(s, l);
            a.remove(s);
        });
        CallbackHandle<SessionContext> notExist = new StringCallbackHandle("whitelist", a::remove);
        handle.setAssociateGroupId(id);
        notExist.setAssociateGroupId(id);
        context.session.getDelegate().register(Result.WHITELIST_GOT, handle, false);
        context.session.getDelegate().register(Result.WHITELIST_NOT_EXIST, notExist, false);
        for (String whitelistName : whitelistNames) {
            Request request = new Request("WHITELIST_GET").withContentKeyPair("whitelist", whitelistName);
            context.session.send(request);
        }
        while (!a.isEmpty()){
            LockSupport.parkNanos(1);
        }
        context.session.getDelegate().removeAssocGroup(id);
        return context.session.getWhitelistMap();
    }
}
