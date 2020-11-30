package ru.kbakaras.sugar.restclient;

public class StatusAssertionFailed extends RuntimeException {

    public StatusAssertionFailed() {}

    public StatusAssertionFailed(String message) {
        super(message);
    }

    public StatusAssertionFailed(String message, Throwable cause) {
        super(message, cause);
    }

    public StatusAssertionFailed(Throwable cause) {
        super(cause);
    }

}
