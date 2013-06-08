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
     */
    void setRepositoryUrl(String repositoryUrl);

    /**
     * Initializes the repository.
     */
    void init() throws RepositoryException;

    /**
     * Stores the given IDA fragment of the given name in the repository.
     *
     * @param name
     *          the name of the fragment
     * @param fragment
     *          the IDA fragment
     */
    void saveFragment(String name, byte[] fragment) throws RepositoryException;

    /**
     * Loads the fragment of the given name from the repository.
     *
     * @param name
     *          the name of the fragment.
     * @return the IDA fragment
     */
    byte[] loadFragment(String name) throws RepositoryException;

    /**
     * Loads the fragment of the given name from the repository.
     *
     * @param name
     *          the name of the fragment.
     * @return true if the fragment was deleted.
     */
    boolean deleteFragment(String name) throws RepositoryException;

}
