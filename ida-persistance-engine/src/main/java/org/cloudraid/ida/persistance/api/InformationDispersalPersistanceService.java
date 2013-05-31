package org.cloudraid.ida.persistance.api;

import java.util.Collection;

/**
 * Service for persisting files through IDAs.
 *
 * @author avasquez
 */
public interface InformationDispersalPersistanceService {

    /**
     * Sets the the repositories to use to store the fragments. Each fragment will be stored in a different repository.
     */
    void setFragmentRepositories(Collection<FragmentRepository> repositories);

    /**
     * Sets the IDA to use for fragmenting data.
     */
    void setInformationDispersalAlgorithm(InformationDispersalAlgorithm ida);

    /**
     * Stores the given data across several fragment repository through an IDA.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @param data
     *          the data to store
     */
    void storeData(String id, byte[] data);

    /**
     * Loads the data, which is recombined from the fragments of several repositories.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @return the loaded data
     */
    byte[] loadData(Collection<FragmentRepository> repositories, String id);

}
