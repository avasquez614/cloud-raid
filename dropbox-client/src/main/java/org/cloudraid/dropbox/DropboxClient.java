package org.cloudraid.dropbox;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.WebAuthSession;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Client interface to a Dropbox account.
 *
 * @author avasquez
 */
public class DropboxClient {

    private static final int INITIAL_BUFFER_SIZE = 4096;
    private static final int PROGRESS_INTERVAL = 10000; // 10 secs

    private static final Logger logger = Logger.getLogger(DropboxClient.class);

    private String uid;
    private AppKeyPair appKeyPair;
    private AccessTokenPair accessTokenPair;
    private String rootPath;

    public DropboxClient(String uid, AppKeyPair appKeyPair, AccessTokenPair accessTokenPair, String rootPath) {
        if (!rootPath.startsWith("/")) {
            rootPath = "/" + rootPath;
        }
        if (rootPath.endsWith("/")) {
            StringUtils.stripEnd(rootPath, "/");
        }

        this.uid = uid;
        this.appKeyPair = appKeyPair;
        this.accessTokenPair = accessTokenPair;
        this.rootPath = rootPath;
    }

    /**
     * Downloads the file from the given path in the Dropbox account.
     *
     * @param path
     *          the path to download the file from
     * @return the file content
     * @throws DropboxClientException
     */
    public byte[] download(String path) throws DropboxClientException {
        // Connect to Dropbox
        DropboxAPI<?> client = connect();
        ByteArrayOutputStream tempOut = new ByteArrayOutputStream(INITIAL_BUFFER_SIZE);

        String fullPath = getFullPath(path);
        ProgressListener progressListener = null;

        if (logger.isDebugEnabled()) {
            progressListener = new LoggingProgressListener("Downloaded", PROGRESS_INTERVAL, fullPath);
        }

        // Download the file
        try {
            client.getFile(fullPath, null, tempOut, progressListener);
        } catch (Exception e) {
            throw new DropboxClientException("Error while trying to download file dropbox://" + uid + fullPath, e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Finished downloading file dropbox://" + uid + fullPath);
        }

        return tempOut.toByteArray();
    }

    /**
     * Uploads the file to the given path in the Dropbox account, creating it if it doesn't exist or overwriting it if it exists.
     *
     * @param path
     *          the path to upload the file to
     * @param content
     *          the file content
     * @throws DropboxClientException
     */
    public void upload(String path, byte[] content) throws DropboxClientException {
        // Connect to Dropbox
        DropboxAPI<?> client = connect();
        ByteArrayInputStream tempIn = new ByteArrayInputStream(content);

        String fullPath = getFullPath(path);
        ProgressListener progressListener = null;

        if (logger.isDebugEnabled()) {
            progressListener = new LoggingProgressListener("Uploaded", PROGRESS_INTERVAL, fullPath);
        }

        // Upload the file
        try {
            client.putFileOverwrite(fullPath, tempIn, content.length, progressListener);
        } catch (Exception e) {
            throw new DropboxClientException("Error while trying to upload file dropbox://" + uid + fullPath, e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Finished uploading file dropbox://" + uid + fullPath);
        }
    }

    /**
     * Deletes the file at the given path in the Dropbox account.
     *
     * @param path
     *          the path of the file to delete
     * @throws DropboxClientException
     */
    public void delete(String path) throws DropboxClientException {
        // Connect to Dropbox
        DropboxAPI<?> client = connect();

        String fullPath = getFullPath(path);
        try {
            client.delete(fullPath);
        } catch (Exception e) {
            throw new DropboxClientException("Error while trying to delete file dropbox://" + uid + fullPath, e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Finished deleting file dropbox://" + uid + fullPath);
        }
    }

    private DropboxAPI<?> connect() {
        WebAuthSession session = new WebAuthSession(appKeyPair, Session.AccessType.DROPBOX, accessTokenPair);

        return new DropboxAPI<WebAuthSession>(session);
    }

    private String getFullPath(String relativePath) {
        if (!relativePath.startsWith("/")) {
            relativePath = "/" + relativePath;
        }

        return rootPath + relativePath;
    }

    private class LoggingProgressListener extends ProgressListener {

        private String action;
        private long interval;
        private String fullPath;

        private LoggingProgressListener(String action, long interval, String fullPath) {
            this.action = action;
            this.interval = interval;
            this.fullPath = fullPath;
        }

        @Override
        public void onProgress(long bytes, long total) {
            long percentageDone = Math.round((((double) bytes) * 100D) / ((double) total));

            logger.debug(action + " " + FileUtils.byteCountToDisplaySize(bytes) + "/" + FileUtils.byteCountToDisplaySize(total) +
                    "  (" + percentageDone + "%) of file dropbox://" + uid + fullPath);
        }

        @Override
        public long progressInterval() {
            return interval;
        }
    }

}
