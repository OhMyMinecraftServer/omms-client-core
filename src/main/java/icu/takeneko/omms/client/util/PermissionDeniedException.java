package icu.takeneko.omms.client.util;

public class PermissionDeniedException extends RuntimeException{
    public PermissionDeniedException(String message) {
        super(message);
    }
}
