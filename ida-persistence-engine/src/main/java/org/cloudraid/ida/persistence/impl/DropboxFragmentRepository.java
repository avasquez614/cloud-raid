package org.cloudraid.ida.persistence.impl;

import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import org.apache.commons.lang.StringUtils;
import org.cloudraid.dropbox.DropboxAccountManager;
import org.cloudraid.dropbox.DropboxClient;
import org.cloudraid.dropbox.DropboxContext;
import org.cloudraid.ida.persistence.api.Configuration;
import org.cloudraid.ida.persistence.api.FragmentRepository;
import org.cloudraid.ida.persistence.exception.RepositoryException;

import java.io.IOException;

/**
 * {@link FragmentRepository} that uses Dropbox as the backend repository.
 */
public class DropboxFragmentRepository implements FragmentRepository {

    public static DropboxContext dropboxContext;

    protected String repositoryUrl;
    protected DropboxClient dropboxClient;

    protected static synchronized DropboxContext getDropboxContext() throws RepositoryException {
        if (dropboxContext == null) {
            dropboxContext = new DropboxContext();
            try {
                dropboxContext.load(DropboxAccountManager.CONTEXT_FILE);
            } catch (IOException e) {
                throw new RepositoryException("Unable to read context file '" + DropboxAccountManager.CONTEXT_FILE, e);
            }
        }

        return dropboxContext;
    }

    @Override
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    @Override
    public void init(Configuration config) throws RepositoryException {
        String uid = config.getInitParameter("UID");
        if (StringUtils.isEmpty(uid)) {
            throw new RepositoryException("No UID param specified");
        }
        String rootPath = config.getInitParameter("RootPath");
        if (StringUtils.isEmpty(rootPath)) {
            throw new RepositoryException("No RootPath param specified");
        }        

        DropboxContext dropboxContext = getDropboxContext();

        AppKeyPair appKeyPair = dropboxContext.getAppKeyPair();
        if (appKeyPair == null) {
            throw new RepositoryException("No AppKeyPair found in DropboxContext");
        }
        AccessTokenPair accessTokenPair = dropboxContext.getAccessTokenPair(uid);
        if (accessTokenPair == null) {
            throw new RepositoryException("No AccessTokenPair found for uid '" + uid + "' in DropboxContext");
        }

        dropboxClient = new DropboxClient(appKeyPair, accessTokenPair, rootPath);
        repositoryUrl = "dropbox://" + uid + (rootPath.startsWith("/")? "" : "/") + rootPath;
    }

    @Override
    public void saveFragment(String name, byte[] fragment) throws RepositoryException {
        try {
            dropboxClient.upload(name, fragment);
        } catch (Exception e) {
            throw new RepositoryException("Unable to upload fragment '" + name + "' to " + repositoryUrl, e);
        }
    }

    @Override
    public byte[] loadFragment(String name) throws RepositoryException {
        try {
            return dropboxClient.download(name);
        } catch (Exception e) {
            throw new RepositoryException("Unable to download fragment '" + name + "' from " + repositoryUrl, e);
        }
    }

    @Override
    public boolean deleteFragment(String name) throws RepositoryException {
        try {
            dropboxClient.delete(name);
        } catch (Exception e) {
            throw new RepositoryException("Unable to delete fragment '" + name + "' from " + repositoryUrl, e);
        }

        return true;
    }

}
