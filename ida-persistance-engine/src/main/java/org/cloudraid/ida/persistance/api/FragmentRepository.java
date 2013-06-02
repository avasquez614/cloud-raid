package org.cloudraid.ida.persistance.api;

import org.cloudraid.ida.persistance.exception.RepositoryException;

/**
 * Represents a repository where IDA fragments can be stored.
 *
 * @author avasquez
 */
public interface FragmentRepository {

    /**
     * Returns the URL of this repository.
     */
    String getRepositoryUrl();

    /**
     * Sets the URL for this repository.
     *
     * <p>The URL indicates the repository type and the root path. For example, if a repository URL is
     * file://my/repo/path, it indicates that the repo is in the local filesystem and its root path is
     * /my/repo/path.</p>
     */
    void setRepositoryUrl(String repositoryUrl);

    /**
     * Stores the given IDA fragment at the given URL in the repository.
     *
     * @param name
     *          the name of the fragment
     * @param fragment
     *          the IDA fragment
     */
    void saveFragment(String name, byte[] fragment) throws RepositoryException;

    /**
     * Loads the fragment at the given URL from the repository.
     *
     * @param name
     *          the name of the fragment.
     * @return the IDA fragment
     */
    byte[] loadFragment(String name) throws RepositoryException;

}
