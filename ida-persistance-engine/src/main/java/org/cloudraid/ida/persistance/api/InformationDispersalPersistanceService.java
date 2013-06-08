package org.cloudraid.ida.persistance.api;

import org.cloudraid.ida.persistance.exception.IdaPersistanceException;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Service for persisting files through IDAs.
 *
 * @author avasquez
 */
public interface InformationDispersalPersistanceService {

    /**
     * Sets the the repositories to use to store the fragments. Each fragment will be stored in a different repository.
     */
    void setFragmentRepositories(List<FragmentRepository> repositories);

    /**
     * Sets the repository for fragment metadata.
     */
    void setFragmentMetaDataRepository(FragmentMetaDataRepository metaDataRepository);

    /**
     * Sets the IDA to use for fragmenting data.
     */
    void setInformationDispersalAlgorithm(InformationDispersalAlgorithm ida);

    /**
     * Sets the executor to use for save/load/delete background tasks.
     */
    void setTaskExecutor(Executor taskExecutor);

    /**
     * Initializes the service.
     */
    void init() throws IdaPersistanceException;

    /**
     * Stores the given data across several fragment repository through an IDA.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @param data
     *          the data to store
     */
    void saveData(String id, byte[] data) throws IdaPersistanceException;

    /**
     * Loads the data, which is recombined from the fragments of several repositories.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @return the loaded data
     */
    byte[] loadData(String id) throws IdaPersistanceException;

    /**
     * Deletes the fragments for the given data ID from their respective repositories.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @return the number of fragments that were deleted.
     */
    int deleteData(String id) throws IdaPersistanceException;

}
