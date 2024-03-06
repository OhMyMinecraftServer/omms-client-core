package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.request.Request;
import icu.takeneko.omms.client.response.Response;
import icu.takeneko.omms.client.session.SessionContext;
import icu.takeneko.omms.client.util.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static icu.takeneko.omms.client.util.Util.gson;

public class WhitelistListCallbackHandle extends CallbackHandle1<Map<String, List<String>>, SessionContext> {

    public WhitelistListCallbackHandle(Callback<Map<String, List<String>>> fn) {
        super("whitelists", fn);
    }

    @Override
    protected Map<String, List<String>> parse(SessionContext context) {
        String whitelists = context.getContent("whitelists");
        if (whitelists == null) return null;
        String[] whitelistNames = gson.fromJson(whitelists, String[].class);
        context.getSession().getWhitelistMap().clear();
        boolean hasError = false;
        RuntimeException re = new RuntimeException();
        try {
            for (String whitelistName : whitelistNames) {
                Response response = context.getSession().sendBlocking(new Request("WHITELIST_GET").withContentKeyPair("whitelist", whitelistName));
                if (response.getResponseCode() == Result.WHITELIST_GOT) {
                    context.getSession().getWhitelistMap().put(whitelistName, new ArrayList<>(Arrays.asList(gson.fromJson(response.getContent("players"), String[].class))));
                }
            }
        } catch (Exception e) {
            re.addSuppressed(e);
            hasError = true;
        }
        if (hasError) throw re;
        return context.getSession().getWhitelistMap();
    }
}
