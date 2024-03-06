package icu.takeneko.omms.client.session.handler;

import icu.takeneko.omms.client.util.Result;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class ResponseHandlerDelegateImpl<C> implements ResponseHandlerDelegate<Result, C, CallbackHandle<C>> {

    Map<Result, List<Callback<C>>> eventCallbackMap = new ConcurrentHashMap<>();

    @Override
    public void handle(Result event, C context) {
        List<Callback<C>> callbacks = eventCallbackMap.get(event);
        if (callbacks == null) return;
        List<Callback<C>> removed = callbacks.stream().filter(it -> it.emitOnce).collect(Collectors.toList());
        boolean[] bl = new boolean[]{false};
        RuntimeException re = new RuntimeException();
        callbacks.forEach(it -> {
            if (it == null)return;
            try {
                it.invoke(context);
            } catch (Exception e) {
                re.addSuppressed(e);
                bl[0] = true;
            }
        });
        removed.forEach(callbacks::remove);
        removed.stream()
                .filter(it -> it.handle.getAssociateGroupId() != null)
                .map(it -> it.handle.getAssociateGroupId())
                .forEach(this::removeAssocGroup);
        if (bl[0]) throw re;
    }

    @Override
    public void registerOnce(Result event, CallbackHandle<C> handle) {
        register(event, handle, true);
    }

    @Override
    public void register(Result event, CallbackHandle<C> handle, boolean emitOnce) {
        if (!eventCallbackMap.containsKey(event)) {
            eventCallbackMap.put(event, new CopyOnWriteArrayList<>());
        } else {
            eventCallbackMap.get(event).add(new Callback<>(emitOnce, handle));
        }
    }


    @Override
    public void remove(Result event, CallbackHandle<C> handle) {
        List<Callback<C>> callbacks = eventCallbackMap.get(event);
        if (callbacks == null) return;
        callbacks.removeIf(c -> c.handle == handle);
    }

    @Override
    public void removeAssocGroup(String groupId) {
        for (List<Callback<C>> callbacks : eventCallbackMap.values()) {
            callbacks.removeIf(it -> it.handle.getAssociateGroupId() != null && it.handle.getAssociateGroupId().equals(groupId));
        }
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
            if (handle == null)return;
            handle.invoke(context);
        }
    }
}
