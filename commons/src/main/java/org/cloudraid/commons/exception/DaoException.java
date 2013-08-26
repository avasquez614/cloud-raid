package org.cloudraid.commons.exception;

/**
 * Common exception for all Data Access Objects.
 *
 * @author avasquez
 */
public class DaoException extends CloudRaidException {

    public DaoException() {
    }

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoException(Throwable cause) {
        super(cause);
    }

}
