package icu.takeneko.omms.client.session.data;

import icu.takeneko.omms.client.session.ClientSession;
import icu.takeneko.omms.client.session.handler.ResponseAccess;
import lombok.Getter;

@Getter
public class SessionContext implements ResponseAccess {
    private final Response response;
    private final ClientSession session;

    public SessionContext(Response response, ClientSession session) {
        this.response = response;
        this.session = session;
    }

    public String getContent(String key) {
        return response.getContent(key);
    }

    @Override
    public String requestId() {
        return response.getRequestId();
    }

    @Override
    public StatusEvent event() {
        return response.getEvent();
    }

    public boolean hasMarker(String marker) {
        return response.getContent("marker_" + marker) != null;
    }

    public boolean hasReason(String reason) {
        return reason.equals(response.getContent("reason"));
    }
}
