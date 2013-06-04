package org.cloudraid.ida.persistance.impl;

import org.apache.commons.io.FileUtils;
import org.cloudraid.ida.persistance.api.FragmentRepository;
import org.cloudraid.ida.persistance.exception.RepositoryException;

import java.io.File;

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
        rootDir = new File(repositoryUrl);
        if (!rootDir.exists()) {
            try {
                FileUtils.forceMkdir(rootDir);
            } catch (Exception e) {
                throw new RepositoryException("Unable to create directory " + rootDir, e);
            }
        }
    }

    @Override
    public void saveFragment(String name, byte[] fragment) throws RepositoryException {
        File fragmentFile = new File(rootDir, name);

        try {
            FileUtils.writeByteArrayToFile(fragmentFile, fragment);
        } catch (Exception e) {
            throw new RepositoryException("Error while trying to write fragment to file " + fragmentFile, e);
        }
    }

    @Override
    public byte[] loadFragment(String name) throws RepositoryException {
        File fragmentFile = new File(rootDir, name);

        try {
            return FileUtils.readFileToByteArray(fragmentFile);
        } catch (Exception e) {
            throw new RepositoryException("Error while trying to read fragment from file " + fragmentFile, e);
        }
    }

}
