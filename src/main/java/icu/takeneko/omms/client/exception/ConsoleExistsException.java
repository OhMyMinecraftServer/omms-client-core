package icu.takeneko.omms.client.exception;

public class ConsoleExistsException extends ControllerException {

    public ConsoleExistsException(String requestedControllerId) {
        super(requestedControllerId);
    }
}
