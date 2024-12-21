package icu.takeneko.omms.client.exception;

import lombok.Getter;

@Getter
public class ControllerException extends RuntimeException{
    private final String requestedControllerId;

    public ControllerException(String requestedControllerId) {
        this.requestedControllerId = requestedControllerId;
    }
}
