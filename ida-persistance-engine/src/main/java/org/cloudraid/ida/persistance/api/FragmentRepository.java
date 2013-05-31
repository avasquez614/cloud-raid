package org.cloudraid.ida.persistance.api;

/**
 * Represents a repository where IDA fragments can be stored.
 *
 * @author avasquez
 */
public interface FragmentRepository {

    /**
     * Sets the URL for this repository.
     */
    void setRepositoryUrl(String repositoryUrl);

    /**
     * Stores the given IDA fragment at the given URL in the repository.
     *
     * @param url
     *          the URL where the fragment should be stored
     * @param fragment
     *          the IDA fragment
     */
    void storeFragment(String url, byte[] fragment);

    /**
     * Loads the fragment at the given URL from the repository.
     *
     * @param url
     *          the URL where the fragment is stored
     * @return the IDA fragment
     */
    byte[] loadFragment(String url);

}
