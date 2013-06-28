package org.cloudraid.ida.persistence.exception;

/**
 * Exception thrown by classes in the crypto package.
 */
public class CryptoException extends IdaPersistenceException {

    public CryptoException() {
    }

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoException(Throwable cause) {
        super(cause);
    }

}
