package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.session.data.SessionContext;
import icu.takeneko.omms.client.util.Pair;
import icu.takeneko.omms.client.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static icu.takeneko.omms.client.util.Util.gson;

public class WhitelistListCallbackHandle extends CallbackHandle1<Map<String, List<String>>, SessionContext> {

    public WhitelistListCallbackHandle(Callback<Map<String, List<String>>> fn) {
        super("whitelists", fn);
    }

    @Override
    protected Map<String, List<String>> parse(SessionContext context) {
        String whitelists = context.getContent("whitelists");
        if (whitelists == null) return null;
        List<String> whitelistNames = Arrays.asList(gson.fromJson(whitelists, String[].class));
        context.getSession().getWhitelistMap().clear();
        return whitelistNames.stream()
            .map(it -> Pair.of(it, gson.fromJson(context.getContent(it), String[].class)))
            .map(it -> Pair.of(it.getA(), Arrays.asList(it.getB())))
            .collect(Util.toMapCollector());
    }
}
