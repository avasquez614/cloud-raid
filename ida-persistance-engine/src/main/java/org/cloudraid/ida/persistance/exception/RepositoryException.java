package org.cloudraid.ida.persistance.exception;

/**
 * Exception used by {@link org.cloudraid.ida.persistance.api.FragmentRepository} and
 * {@link org.cloudraid.ida.persistance.api.FragmentMetaDataRepository} when an error occurs.
 *
 * @author avasquez
 */
public class RepositoryException extends IdaPersistanceException {

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
