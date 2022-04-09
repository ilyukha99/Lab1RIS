package ris.db.exceptions;

public class RollbackOccuredException extends CustomRuntimeException {
    public RollbackOccuredException(String message, Throwable cause) {
        super(message, cause);
    }
}
