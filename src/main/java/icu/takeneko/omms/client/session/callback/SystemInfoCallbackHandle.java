package icu.takeneko.omms.client.session.callback;

import icu.takeneko.omms.client.system.SystemInfo;

public class SystemInfoCallbackHandle extends JsonObjectCallbackHandle<SystemInfo> {
    public SystemInfoCallbackHandle(Callback<SystemInfo> fn) {
        super("systemInfo", fn);
    }
}
