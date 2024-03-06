package icu.takeneko.omms.client.session;

import icu.takeneko.omms.client.session.response.Response;
import icu.takeneko.omms.client.util.Result;

public class SessionContext {
    private final Response response;
    private final ClientSession session;

    public SessionContext(Response response, ClientSession session) {
        this.response = response;
        this.session = session;
    }

    public Response getResponse() {
        return response;
    }

    public ClientSession getSession() {
        return session;
    }

    public Result getResponseCode() {
        return response.getResponseCode();
    }

    public String getContent(String key) {
        return response.getContent(key);
    }
}
