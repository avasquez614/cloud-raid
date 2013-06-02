package org.cloudraid.ida.persistance.exception;

/**
 * Root exception for the IDA Persistance Engine.
 *
 * @author avasquez
 */
public class IdaPersistanceException extends Exception {

    public IdaPersistanceException() {
    }

    public IdaPersistanceException(String message) {
        super(message);
    }

    public IdaPersistanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdaPersistanceException(Throwable cause) {
        super(cause);
    }

}
