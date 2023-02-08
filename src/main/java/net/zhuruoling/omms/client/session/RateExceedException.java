package net.zhuruoling.omms.client.session;

public class RateExceedException extends RuntimeException {
    public RateExceedException(String reason) {
        super(reason);
    }
}
