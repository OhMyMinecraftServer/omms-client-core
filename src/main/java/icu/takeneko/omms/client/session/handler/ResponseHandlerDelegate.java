package icu.takeneko.omms.client.session.handler;

import icu.takeneko.omms.client.session.callback.Callback;

public interface ResponseHandlerDelegate<E, C, H> {
    void handle(E event, C context);

    void registerOnce(E event, H handle);

    void register(E event, H handle, boolean emitOnce);

    void remove(E event, H handle);

    void removeAssocGroup(String groupId);

    void setOnExceptionThrownHandler(Callback<Throwable> cb);

    void shutdown();
}
