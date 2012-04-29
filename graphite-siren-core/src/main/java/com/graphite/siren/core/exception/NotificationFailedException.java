package com.graphite.siren.core.exception;

public class NotificationFailedException extends RuntimeException {
    public NotificationFailedException() {
        super();

    }

    public NotificationFailedException(String s) {
        super(s);

    }

    public NotificationFailedException(String s, Throwable throwable) {
        super(s, throwable);

    }

    public NotificationFailedException(Throwable throwable) {
        super(throwable);

    }
}
