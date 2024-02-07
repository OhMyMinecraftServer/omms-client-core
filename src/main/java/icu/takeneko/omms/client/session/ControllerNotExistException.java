package icu.takeneko.omms.client.session;

public class ControllerNotExistException extends RuntimeException {
    public ControllerNotExistException(String s) {
        super(s);
    }
}
