package org.cloudraid.ida.persistence.exception;

/**
 * Exception thrown by {@link org.cloudraid.ida.persistence.api.InformationDispersalAlgorithm}.
 *
 * @author avasquez
 */
public class IdaException extends IdaPersistenceException {

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
