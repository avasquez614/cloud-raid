package org.cloudraid.ida.persistence.api;

import org.cloudraid.ida.persistence.exception.IdaPersistenceException;

/**
 * Service for persisting files through IDAs.
 *
 * @author avasquez
 */
public interface InformationDispersalPersistenceService {

    /**
     * Initializes the service.
     *
     * @param config
     *          the configuration that contains the initialization parameters and possible dependencies
     */
    void init(Configuration config) throws IdaPersistenceException;

    /**
     * Stores the given data across several fragment repository through an IDA.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @param data
     *          the data to store
     */
    void saveData(String id, byte[] data) throws IdaPersistenceException;

    /**
     * Loads the data, which is recombined from the fragments of several repositories.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @return the loaded data
     */
    byte[] loadData(String id) throws IdaPersistenceException;

    /**
     * Deletes the fragments for the given data ID from their respective repositories.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @return the number of fragments that were deleted.
     */
    int deleteData(String id) throws IdaPersistenceException;

}
