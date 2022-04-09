package ris.db.exceptions;

public class CustomRuntimeException extends RuntimeException {
    public CustomRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomRuntimeException(String message) {
        super(message);
    }
}
