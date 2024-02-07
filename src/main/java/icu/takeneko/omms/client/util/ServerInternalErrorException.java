package icu.takeneko.omms.client.util;

public class ServerInternalErrorException extends RuntimeException{
    public ServerInternalErrorException(String message) {
        super(message);
    }
}
