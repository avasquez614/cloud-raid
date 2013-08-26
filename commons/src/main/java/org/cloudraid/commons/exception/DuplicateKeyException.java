package org.cloudraid.commons.exception;

/**
 * DAO exception thrown when an object's save operation results in a unique key constraint violation.
 *
 * @author avasquez
 */
public class DuplicateKeyException extends DaoException {

    public DuplicateKeyException() {
    }

    public DuplicateKeyException(String message) {
        super(message);
    }

    public DuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateKeyException(Throwable cause) {
        super(cause);
    }

}
