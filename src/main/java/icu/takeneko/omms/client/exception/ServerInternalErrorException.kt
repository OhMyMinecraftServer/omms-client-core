package icu.takeneko.omms.client.exception;

public class ServerInternalErrorException extends RuntimeException {
    public ServerInternalErrorException(String message) {
        super(message);
    }
}
