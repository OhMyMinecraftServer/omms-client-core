package icu.takeneko.omms.client.exception;

import lombok.Getter;

@Getter
public class ConsoleNotFoundException extends RuntimeException{
    private final String requestedConsoleId;

    public ConsoleNotFoundException(String requestedConsoleId) {
        this.requestedConsoleId = requestedConsoleId;
    }
}
