package icu.takeneko.omms.client.exception;

import icu.takeneko.omms.client.Constants;

public class VersionNotMatchException extends RuntimeException{

    private final long serverVersion;

    public VersionNotMatchException() {
        super(String.format("Version mismatch, current client version: %d", Constants.PROTOCOL_VERSION));
        serverVersion = 0;
    }

    public VersionNotMatchException(long serverVersion) {
        super(String.format("Version mismatch, server version: %d, current client version: %d", serverVersion, Constants.PROTOCOL_VERSION));
        this.serverVersion = serverVersion;
    }

    public long getServerVersion() {
        return serverVersion;
    }

    public long getClientVersion() {
        return Constants.PROTOCOL_VERSION;
    }
}
