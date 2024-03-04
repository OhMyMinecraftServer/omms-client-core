package icu.takeneko.omms.client.session.handler;

import icu.takeneko.omms.client.util.Result;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class ResponseHandlerDelegateImpl<C> implements ResponseHandlerDelegate<Result, C, CallbackHandle<C>> {

    Map<Result, Set<Callback<C>>> eventCallbackMap = new ConcurrentHashMap<>();

    @Override
    public void handle(Result event, C context) {
        Set<Callback<C>> set = eventCallbackMap.get(event);
        if (set == null) return;
        List<Callback<C>> removed = set.stream().filter(it -> it.emitOnce).collect(Collectors.toList());
        set.forEach(it -> it.invoke(context));
        removed.forEach(set::remove);
    }

    @Override
    public void registerOnce(Result event, CallbackHandle<C> handle) {
        register(event, handle, true);
    }

    @Override
    public void register(Result event, CallbackHandle<C> handle, boolean emitOnce) {
        if (!eventCallbackMap.containsKey(event)) {
            eventCallbackMap.put(event, new CopyOnWriteArraySet<>());
        } else {
            eventCallbackMap.get(event).add(new Callback<>(emitOnce, handle));
        }
    }


    @Override
    public void remove(Result event, CallbackHandle<C> handle) {
        Set<Callback<C>> set = eventCallbackMap.get(event);
        if (set == null) return;
        set.removeIf(c -> c.handle == handle);
    }

    private static final class Callback<R> {
        boolean emitOnce;
        CallbackHandle<R> handle;

        public Callback(boolean emitOnce, CallbackHandle<R> handle) {
            this.emitOnce = emitOnce;
            this.handle = handle;
        }

        public Callback(CallbackHandle<R> handle) {
            this.handle = handle;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Callback)) return false;

            Callback<?> callback = (Callback<?>) o;

            if (emitOnce != callback.emitOnce) return false;
            return Objects.equals(handle, callback.handle);
        }

        @Override
        public int hashCode() {
            int result = (emitOnce ? 1 : 0);
            result = 31 * result + (handle != null ? handle.hashCode() : 0);
            return result;
        }

        public void invoke(R context) {
            handle.invoke(context);
        }
    }
}
