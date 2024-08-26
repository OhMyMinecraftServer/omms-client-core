package icu.takeneko.omms.client.session;

public class RateExceedException extends RuntimeException {
    public RateExceedException(String reason) {
        super(reason);
    }
}
