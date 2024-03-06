package icu.takeneko.omms.client.session.callback;

import com.google.gson.reflect.TypeToken;
import icu.takeneko.omms.client.data.system.SystemInfo;

public class SystemInfoCallbackHandle extends JsonObjectCallbackHandle<SystemInfo> {
    public SystemInfoCallbackHandle(Callback<SystemInfo> fn) {
        super("systemInfo", fn);
    }

    @Override
    protected TypeToken<SystemInfo> getObjectType() {
        return TypeToken.get(SystemInfo.class);
    }
}
