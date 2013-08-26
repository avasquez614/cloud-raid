package org.cloudraid.commons.exception;

/**
 * Root exception for the Cloud RAID system.
 *
 * @author avasquez
 */
public class CloudRaidException extends Exception {

    public CloudRaidException() {
    }

    public CloudRaidException(String message) {
        super(message);
    }

    public CloudRaidException(String message, Throwable cause) {
        super(message, cause);
    }

    public CloudRaidException(Throwable cause) {
        super(cause);
    }

}
