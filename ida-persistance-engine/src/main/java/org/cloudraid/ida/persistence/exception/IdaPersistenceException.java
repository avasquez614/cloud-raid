package org.cloudraid.ida.persistence.exception;

/**
 * Root exception for the IDA Persistence Engine.
 *
 * @author avasquez
 */
public class IdaPersistenceException extends Exception {

    public IdaPersistenceException() {
    }

    public IdaPersistenceException(String message) {
        super(message);
    }

    public IdaPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdaPersistenceException(Throwable cause) {
        super(cause);
    }

}
