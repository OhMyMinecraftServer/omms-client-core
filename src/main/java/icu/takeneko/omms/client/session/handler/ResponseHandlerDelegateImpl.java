package icu.takeneko.omms.client.session.handler;

import icu.takeneko.omms.client.session.callback.Callback;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResponseHandlerDelegateImpl<C extends ResponseAccess> implements ResponseHandlerDelegate<C, CallbackHandle<C>> {

    private final ExecutorService dispatchThread = Executors.newWorkStealingPool();
    private final Map<String, EventSubscription<C>> subscriptionMap = new HashMap<>();
    @Setter
    private Callback<Throwable> exceptionHandler = (e) -> {};


    @Override
    public void handle(C context) {
        EventSubscription<C> subscription = subscriptionMap.get(context.requestId());
        if (subscription == null) return;
        dispatchThread.submit(() -> {
            try {
                subscription.handle(context.event(), context);
            } catch (Throwable e){
                exceptionHandler.accept(e);
            }
        });
    }

    @Override
    public void register(EventSubscription<C> subscription) {
        subscriptionMap.put(subscription.getRequestId(), subscription);
    }

    @Override
    public void shutdown() {
        dispatchThread.shutdown();
    }
}
