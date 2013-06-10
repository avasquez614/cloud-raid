package org.cloudraid.ida.persistance.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.cloudraid.ida.persistance.api.*;
import org.cloudraid.ida.persistance.exception.IdaPersistanceException;
import org.cloudraid.ida.persistance.exception.RepositoryException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;

/**
 * Default implementation of the {@link InformationDispersalPersistanceService}.
 *
 * <p>
 *     <strong>WARN:</strong> This class is not thread-safe, for performance reasons (an instance of this object will
 *     probably used in JLAN, which handles locking). So make sure classes that use it are thread-safe.
 * </p>
 *
 * @author avasquez
 */
public class InformationDispersalPersistanceServiceImpl implements InformationDispersalPersistanceService {

    private static final Logger logger = Logger.getLogger(InformationDispersalPersistanceServiceImpl.class);

    public static final String FRAGMENT_FILE_EXT = "frag";

    protected List<FragmentRepository> repositories;
    protected FragmentMetaDataRepository metaDataRepository;
    protected InformationDispersalAlgorithm ida;
    protected Executor taskExecutor;

    @Override
    public void setFragmentRepositories(List<FragmentRepository> repositories) {
        this.repositories = repositories;
    }

    @Override
    public void setFragmentMetaDataRepository(FragmentMetaDataRepository metaDataRepository) {
        this.metaDataRepository = metaDataRepository;
    }

    @Override
    public void setInformationDispersalAlgorithm(InformationDispersalAlgorithm ida) {
        this.ida = ida;
    }

    @Override
    public void setTaskExecutor(Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void init() {
    }

    /**
     * Stores the given data across several fragment repositories through the IDA.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @param data
     *          the data to store
     */
    @Override
    public void saveData(String id, byte[] data) throws IdaPersistanceException {
        List<byte[]> fragments;
        try {
            fragments = ida.split(data);
        } catch (Exception e) {
            throw new IdaPersistanceException("Error while trying to split the data", e);
        }

        AvailableFragmentRepositories availableRepositories = new AvailableFragmentRepositories(repositories);
        CompletionService<Boolean> saveCompletionService = new ExecutorCompletionService<Boolean>(taskExecutor);

        for (int i = 0; i < fragments.size(); i++) {
            byte[] fragment = fragments.get(i);
            FragmentRepository repository = availableRepositories.take();
            FragmentMetaData metaData = new FragmentMetaData(id, i, repository.getRepositoryUrl());

            saveCompletionService.submit(new FragmentSaveTask(fragment, metaData, repository, metaDataRepository));
        }

        int savedNum = 0;

        for (int i = 0; i < fragments.size(); i++) {
            boolean saved;
            try {
                saved = saveCompletionService.take().get();
                if (saved) {
                    savedNum++;
                }
            } catch (Exception e) {
                logger.error("Error while trying to retrieve save task result", e);
            }
        }

        // TODO: Try to save on the background the fragments that couldn't be saved, only if the total of required
        // fragments to restore the data were successfully saved.
        if (savedNum < fragments.size()) {
            throw new IdaPersistanceException("Some fragments couldn't be saved");
        }
    }

    /**
     * Loads the data, which is recombined (through the IDA) from the fragments of several repositories.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @return the loaded data.
     */
    @Override
    public byte[] loadData(String id) throws IdaPersistanceException {
        List<FragmentMetaData> fragmentsMetaData;
        try {
            fragmentsMetaData = metaDataRepository.getAllFragmentMetaDataForData(id);
        } catch (RepositoryException e) {
            throw new IdaPersistanceException("Error while trying to retrieve all fragment metadata for the data", e);
        }

        int requiredFragmentNum = ida.getFragmentNumber() - ida.getRedundantFragmentNumber();
        if (fragmentsMetaData.size() < requiredFragmentNum) {
            throw new IdaPersistanceException("Not enough fragments are saved to rebuild the data");
        }

        // Make sure all repository URLs in the metadata are correct.
        List<FragmentRepository> repositoriesUsed = new ArrayList<FragmentRepository>(fragmentsMetaData.size());
        for (FragmentMetaData metaData : fragmentsMetaData) {
            FragmentRepository repository = getRepositoryForMetaData(metaData);
            if (repository == null) {
                throw new IdaPersistanceException("No repository found for URL [" + metaData.getRepositoryUrl() + "]");
            }

            repositoriesUsed.add(repository);
        }

        AvailableFragmentRepositories availableRepositories = getAvailableFragmentRepositories(repositoriesUsed);
        CompletionService<byte[]> loadCompletionService = new ExecutorCompletionService<byte[]>(taskExecutor);

        // Submit the main tasks (number of main tasks = required fragment number). If there are not enough repositories
        // for the main tasks, then throw an exception indicating the data can't be loaded.
        for (int i = 0; i < requiredFragmentNum; i++) {
            FragmentRepository repository = availableRepositories.take();
            if (repository == null) {
                throw new IdaPersistanceException("Not enough available repositories to rebuild the data");
            }

            loadCompletionService.submit(new FragmentLoadTask(id, repository));
        }

        // Keep polling for fragments until we reach the required number. If a fragment couldn't be loaded, try with a
        // backup task. If there are no more repositories for backup tasks, then stop.
        List<byte[]> fragments = new ArrayList<byte[]>();
        while (fragments.size() < requiredFragmentNum) {
            byte[] fragment = null;
            try {
                fragment = loadCompletionService.take().get();
                if (fragment != null) {
                    fragments.add(fragment);
                }
            } catch (Exception e) {
                logger.error("Error while trying to retrieve load task result", e);
            }

            if (fragment == null) {
                FragmentRepository repository = availableRepositories.take();
                if (repository == null) {
                    throw new IdaPersistanceException("Not enough available repositories to rebuild the data");
                }

                loadCompletionService.submit(new FragmentLoadTask(id, repository));
            }
        }

        try {
            return ida.combine(fragments);
        } catch (Exception e) {
            throw new IdaPersistanceException("Error while trying to combine the fragments", e);
        }
    }

    @Override
    public int deleteData(String id) throws IdaPersistanceException {
        List<FragmentMetaData> fragmentsMetaData;
        try {
            fragmentsMetaData = metaDataRepository.getAllFragmentMetaDataForData(id);
        } catch (RepositoryException e) {
            throw new IdaPersistanceException("Error while trying to retrieve all fragment metadata for the data", e);
        }

        if (CollectionUtils.isEmpty(fragmentsMetaData)) {
            return 0;
        }

        // Make sure all repository URLs in the metadata are correct before submitting any delete task.
        List<FragmentDeleteTask> deleteTasks = new ArrayList<FragmentDeleteTask>(fragmentsMetaData.size());
        for (FragmentMetaData metaData : fragmentsMetaData) {
            FragmentRepository repository = getRepositoryForMetaData(metaData);
            if (repository == null) {
                throw new IdaPersistanceException("No repository found for URL [" + metaData.getRepositoryUrl() + "]");
            }

            deleteTasks.add(new FragmentDeleteTask(metaData, repository, metaDataRepository));
        }

        CompletionService<Boolean> deleteCompletionService = new ExecutorCompletionService<Boolean>(taskExecutor);
        for (FragmentDeleteTask task : deleteTasks) {
            deleteCompletionService.submit(task);
        }

        int deletedNum = 0;

        for (int i = 0; i < fragmentsMetaData.size(); i++) {
            boolean deleted;
            try {
                deleted = deleteCompletionService.take().get();
                if (deleted) {
                    deletedNum++;
                }
            } catch (Exception e) {
                logger.error("Error while trying to retrieve delete task result", e);
            }
        }

        return deletedNum;
    }

    protected AvailableFragmentRepositories getAvailableFragmentRepositories(Collection<FragmentRepository> repositories) {
        return new AvailableFragmentRepositories(repositories);
    }

    protected FragmentRepository getRepositoryForMetaData(FragmentMetaData metaData) {
        for (FragmentRepository repository : repositories) {
            if (repository.getRepositoryUrl().equals(metaData.getRepositoryUrl())) {
                return repository;
            }
        }

        return null;
    }

}
