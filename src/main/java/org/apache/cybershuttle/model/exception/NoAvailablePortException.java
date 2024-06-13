package org.apache.cybershuttle.model.exception;

public class NoAvailablePortException extends RuntimeException {

    public NoAvailablePortException(String message) {
        super(message);
    }

    public NoAvailablePortException(String message, Throwable cause) {
        super(message, cause);
    }
}
