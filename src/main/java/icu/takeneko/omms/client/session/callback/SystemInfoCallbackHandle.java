package icu.takeneko.omms.client.session.callback;

import com.google.gson.reflect.TypeToken;
import icu.takeneko.omms.client.system.SystemInfo;

import java.util.function.Consumer;

public class SystemInfoCallbackHandle extends JsonObjectCallbackHandle<SystemInfo> {
    public SystemInfoCallbackHandle(Consumer<SystemInfo> fn) {
        super("systemInfo", fn);
    }

    @Override
    protected TypeToken<SystemInfo> getObjectType() {
        return new TypeToken<SystemInfo>() {
        };
    }
}
