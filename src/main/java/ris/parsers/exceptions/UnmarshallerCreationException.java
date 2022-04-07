package ris.parsers.exceptions;

import javax.xml.bind.JAXBException;

public class UnmarshallerCreationException extends RuntimeException {
    public UnmarshallerCreationException(String message, JAXBException jaxbException) {
        super(message, jaxbException);
    }
}
