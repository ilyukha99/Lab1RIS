package ris.db.exceptions;

public class DBException extends CustomRuntimeException {
    public DBException(String message, Throwable cause) {
        super(message, cause);
    }
}
