package org.cloudraid.ida.persistance.api;

import java.util.Collection;

/**
 * Service for persisting files through IDAs.
 *
 * @author avasquez
 */
public interface InformationDispersalPersistanceService {

    /**
     * Sets the IDA to use for fragmenting data.
     */
    void setInformationDispersalAlgorithm(InformationDispersalAlgorithm ida);

    /**
     * Stores the given data across several fragment repository through an IDA.
     *
     * @param repositories
     *          the repositories to use to store the fragments. Each fragment will be stored in a different repository.
     * @param url
     *          the URL where each fragment will be stored in its repository
     * @param data
     *          the data to store
     */
    void storeData(Collection<FragmentRepository> repositories, String url, byte[] data);

    /**
     * Loads the data, which is recombined from the fragments of several repositories.
     *
     * @param repositories
     *          the repositories where the fragments are stored. Each fragment of the data is stored in a different
     *          repository.
     * @param url
     *          the URL where each fragment is stored in it's respective repository
     * @return the loaded data
     */
    byte[] loadData(Collection<FragmentRepository> repositories, String url);

}
