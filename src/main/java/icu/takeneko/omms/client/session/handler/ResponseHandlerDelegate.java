package icu.takeneko.omms.client.session.handler;

import icu.takeneko.omms.client.session.callback.Callback;

public interface ResponseHandlerDelegate<C, H> {
    void handle(C context);

    void register(EventSubscription<C> subscription);

    default EventSubscription<C> subscribe(String requestId){
        EventSubscription<C> subscription = new EventSubscription<>(requestId);
        register(subscription);
        return subscription;
    }

    void shutdown();

    void setExceptionHandler(Callback<Throwable> callback);
}
