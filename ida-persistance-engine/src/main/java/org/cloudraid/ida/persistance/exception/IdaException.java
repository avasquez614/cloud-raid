package org.cloudraid.ida.persistance.exception;

/**
 * Exceptions thrown by {@link org.cloudraid.ida.persistance.api.InformationDispersalAlgorithm}.
 *
 * @author avasquez
 */
public class IdaException extends IdaPersistanceException {

    public IdaException() {
    }

    public IdaException(String message) {
        super(message);
    }

    public IdaException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdaException(Throwable cause) {
        super(cause);
    }

}
