package icu.takeneko.omms.client.session.handler;

import icu.takeneko.omms.client.session.data.StatusEvent;

public interface ResponseAccess {
    String requestId();

    StatusEvent event();
}
