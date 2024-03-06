package icu.takeneko.omms.client.exception;

import icu.takeneko.omms.client.util.Util;

public class VersionNotMatchException extends RuntimeException{

    private final long serverVersion;

    public VersionNotMatchException() {
        super(String.format("Version mismatch, current client version: %d", Util.PROTOCOL_VERSION));
        serverVersion = 0;
    }

    public VersionNotMatchException(long serverVersion) {
        super(String.format("Version mismatch, server version: %d, current client version: %d", serverVersion, Util.PROTOCOL_VERSION));
        this.serverVersion = serverVersion;
    }

    public long getServerVersion() {
        return serverVersion;
    }

    public long getClientVersion() {
        return Util.PROTOCOL_VERSION;
    }
}
