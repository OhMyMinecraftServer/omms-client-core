package icu.takeneko.omms.client.session.handler;

import icu.takeneko.omms.client.session.data.StatusEvent;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class EventSubscription<C> {
    @Getter
    private final String requestId;
    private final Map<StatusEvent, CallbackHandle<C>> subscriptions = new HashMap<>();
    private final Map<StatusEvent, SubscriptionStatus> statuses = new HashMap<>();

    EventSubscription(String requestId) {
        this.requestId = requestId;
    }

    public void handle(StatusEvent event, C context) {
        CallbackHandle<C> handle = subscriptions.get(event);
        SubscriptionStatus status = statuses.get(event);
        if (handle != null && status != null) {
            if (status == SubscriptionStatus.EMITTED) return;
            if (status != SubscriptionStatus.MULTIPLE) {
                statuses.put(event, SubscriptionStatus.EMITTED);
            }
            handle.invoke(context);
        }
    }

    public EventSubscription<C> subscribeSuccess(CallbackHandle<C> handle){
        return subscribe(StatusEvent.SUCCESS, handle);
    }

    public EventSubscription<C> subscribeFailure(CallbackHandle<C> handle){
        return subscribe(StatusEvent.FAIL, handle);
    }

    public EventSubscription<C> subscribe(StatusEvent event, CallbackHandle<C> handle) {
        subscriptions.put(event, handle);
        statuses.put(event, SubscriptionStatus.ONCE);
        return this;
    }

    public EventSubscription<C> subscribeAlways(StatusEvent event, CallbackHandle<C> handle) {
        subscriptions.put(event, handle);
        statuses.put(event, SubscriptionStatus.MULTIPLE);
        return this;
    }

    public boolean allHandlerCalled() {
        return statuses.values()
            .stream()
            .allMatch(it -> it == SubscriptionStatus.EMITTED);
    }

    public static <C1> EventSubscription<C1> of(String requestId) {
        return new EventSubscription<>(requestId);
    }
}
