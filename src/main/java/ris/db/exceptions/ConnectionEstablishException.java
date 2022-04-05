package ris.db.exceptions;

public class ConnectionEstablishException extends RuntimeException {
    public ConnectionEstablishException(String exception) {
        super(exception);
    }
}
