package org.cloudraid.ida.persistance.impl;

import org.apache.commons.io.FileUtils;
import org.cloudraid.ida.persistance.api.FragmentRepository;
import org.cloudraid.ida.persistance.exception.RepositoryException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Implementation of {@link FragmentRepository} that uses a directory in the local filesystem to store the fragments.
 */
public class FilesystemFragmentRepository implements FragmentRepository {

    private String repositoryUrl;
    private File rootDir;

    @Override
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    @Override
    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    @Override
    public void init() throws RepositoryException {
        try {
            rootDir = new File(new URI(repositoryUrl));
        } catch (URISyntaxException e) {
            throw new RepositoryException("Invalid file URI " + repositoryUrl, e);
        }

        if (!rootDir.exists()) {
            try {
                FileUtils.forceMkdir(rootDir);
            } catch (IOException e) {
                throw new RepositoryException("Unable to create folder " + rootDir, e);
            }
        }
    }

    @Override
    public void saveFragment(String name, byte[] fragment) throws RepositoryException {
        File fragmentFile = new File(rootDir, name);

        try {
            FileUtils.writeByteArrayToFile(fragmentFile, fragment);
        } catch (IOException e) {
            throw new RepositoryException("Error while trying to write fragment to file " + fragmentFile, e);
        }
    }

    @Override
    public byte[] loadFragment(String name) throws RepositoryException {
        File fragmentFile = new File(rootDir, name);

        try {
            return FileUtils.readFileToByteArray(fragmentFile);
        } catch (IOException e) {
            throw new RepositoryException("Error while trying to read fragment from file " + fragmentFile, e);
        }
    }

}
