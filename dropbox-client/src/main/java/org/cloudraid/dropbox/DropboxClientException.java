package org.cloudraid.dropbox;

/**
 * Exception thrown by the {@link DropboxClient}.
 *
 * @author avasquez
 */
public class DropboxClientException extends Exception {

    public DropboxClientException() {
    }

    public DropboxClientException(String s) {
        super(s);
    }

    public DropboxClientException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public DropboxClientException(Throwable throwable) {
        super(throwable);
    }

}
