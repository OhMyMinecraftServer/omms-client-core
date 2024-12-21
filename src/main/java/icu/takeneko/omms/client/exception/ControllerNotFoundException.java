package icu.takeneko.omms.client.exception;

public class ControllerNotFoundException extends ControllerException {

    public ControllerNotFoundException(String requestedControllerId) {
        super(requestedControllerId);
    }
}
