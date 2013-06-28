package org.cloudraid.ida.persistence.exception;

/**
 * Exception used by repositories when an error occurs.
 *
 * @author avasquez
 */
public class RepositoryException extends IdaPersistenceException {

    public RepositoryException() {
    }

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(Throwable cause) {
        super(cause);
    }

}
