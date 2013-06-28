package org.cloudraid.ida.persistence.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.cloudraid.ida.persistence.api.Configuration;
import org.cloudraid.ida.persistence.api.FragmentRepository;
import org.cloudraid.ida.persistence.exception.RepositoryException;

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
    public void init(Configuration config) throws RepositoryException {
        repositoryUrl = config.getInitParameter("Url");
        if (StringUtils.isEmpty(repositoryUrl)) {
            throw new RepositoryException("No Url param specified");
        }

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

    @Override
    public boolean deleteFragment(String name) throws RepositoryException {
        File fragmentFile = new File(rootDir, name);

        try {
            return fragmentFile.delete();
        } catch (Exception e) {
            throw new RepositoryException("Error while trying to delete fragment " + fragmentFile, e);
        }
    }

    @Override
    public String toString() {
        return "FilesystemFragmentRepository[" +
                "rootDir=" + rootDir +
                ']';
    }

}
