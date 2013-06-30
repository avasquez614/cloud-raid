package org.cloudraid.dropbox;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.WebAuthSession;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Client interface to a Dropbox account.
 *
 * @author avasquez
 */
public class DropboxClient {

    private static final int INITIAL_BUFFER_SIZE = 1024;

    private AppKeyPair appKeyPair;
    private AccessTokenPair accessTokenPair;
    private String rootPath;

    public DropboxClient(AppKeyPair appKeyPair, AccessTokenPair accessTokenPair, String rootPath) {
        if (!rootPath.startsWith("/")) {
            rootPath = "/" + rootPath;
        }
        if (rootPath.endsWith("/")) {
            StringUtils.stripEnd(rootPath, "/");
        }

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
        InputStream fileIn = null;
        // Download the file
        try {
            fileIn = client.getFileStream(fullPath, null);
            IOUtils.copy(fileIn, tempOut);
        } catch (Exception e) {
            throw new DropboxClientException("Error while trying to download file '" + fullPath + "' from Dropbox", e);
        } finally {
            IOUtils.closeQuietly(fileIn);
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
        // Upload the file
        try {
            client.putFileOverwrite(fullPath, tempIn, content.length, null);
        } catch (Exception e) {
            throw new DropboxClientException("Error while trying to upload file '" + fullPath + "' to Dropbox", e);
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
            throw new DropboxClientException("Error while trying to delete file '" + fullPath + "' from Dropbox", e);
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

}
