package org.cloudraid.ida.persistance.impl;

import org.apache.log4j.Logger;
import org.cloudraid.ida.persistance.api.*;
import org.cloudraid.ida.persistance.exception.IdaPersistanceException;
import org.cloudraid.ida.persistance.exception.RepositoryException;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

/**
 * Default implementation of the {@link InformationDispersalPersistanceService}.
 *
 * @author avasquez
 */
public class InformationDispersalPersistanceServiceImpl implements InformationDispersalPersistanceService {

    private static final Logger logger = Logger.getLogger(InformationDispersalPersistanceServiceImpl.class);

    public static final String FRAGMENT_FILE_EXT = "frag";

    protected List<FragmentRepository> repositories;
    protected FragmentMetaDataRepository metaDataRepository;
    protected InformationDispersalAlgorithm ida;
    protected File temporaryFragmentDirectory;
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
    public void setTemporaryFragmentDirectory(File directory) {
        this.temporaryFragmentDirectory = directory;
    }

    @Override
    public void init() {
        taskExecutor = Executors.newCachedThreadPool();
    }

    /**
     * Stores the given data across several fragment repository through the IDA.
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

        CompletionService<Boolean> saveCompletionService = new ExecutorCompletionService<Boolean>(taskExecutor);

        for (int i = 0; i < fragments.size(); i++) {
            byte[] fragment = fragments.get(i);
            FragmentRepository repository = repositories.get(i);
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

        // TODO: Try to save on the background the fragments that couldn't be saved, only if the total of required fragments
        // to restore the data were successfully saved.
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
        List<FragmentMetaData> allMetaData;
        try {
            allMetaData = metaDataRepository.getAllFragmentMetaDataForData(id);
        } catch (RepositoryException e) {
            throw new IdaPersistanceException("Error while trying to retrieve all fragment metadata for the data", e);
        }

        int requiredFragmentNum = ida.getFragmentNumber() - ida.getRedundantFragmentNumber();
        List<FragmentLoadTask> loadTasks = new ArrayList<FragmentLoadTask>();
        CompletionService<byte[]> loadCompletionService = new ExecutorCompletionService<byte[]>(taskExecutor);

        // Don't immediately submit the task: we want to make sure all repository URLs in the metadata are correct.
        for (FragmentMetaData metaData : allMetaData) {
            FragmentRepository repository = getRepositoryForMetaData(metaData);
            if (repository == null) {
                throw new IdaPersistanceException("No repository found for URL [" + metaData.getRepositoryUrl() + "]");
            }

            loadTasks.add(new FragmentLoadTask(metaData, repository));
        }

        // Submit the main tasks (number of main tasks = required fragment number). Backup tasks are added to a queue.
        Queue<FragmentLoadTask> backupTasks = new LinkedList<FragmentLoadTask>();
        for (int i = 0; i < loadTasks.size(); i++) {
            if (i < requiredFragmentNum) {
                loadCompletionService.submit(loadTasks.get(i));
            } else {
                backupTasks.add(loadTasks.get(i));
            }
        }

        // Keep polling for fragments until we reach the required number. If a fragment couldn't be loaded, try with a
        // backup task. If backup tasks queue has been depleted, it means we couldn't load all required fragments, so
        // stop polling.
        List<byte[]> fragments = new ArrayList<byte[]>();
        while (fragments.size() < requiredFragmentNum) {
            byte[] fragment = null;
            try {
                fragment = loadCompletionService.take().get();
                fragments.add(fragment);
            } catch (Exception e) {
                logger.error("Error while trying to retrieve load task result", e);
            }

            if (fragment == null) {
                FragmentLoadTask backupTask = backupTasks.poll();
                if (backupTask == null) {
                    break;
                }

                loadCompletionService.submit(backupTask);
            }
        }

        if (fragments.size() == requiredFragmentNum) {
            try {
                return ida.combine(fragments);
            } catch (Exception e) {
                throw new IdaPersistanceException("Error while trying to combine the fragments", e);
            }
        } else {
            throw new IdaPersistanceException("Not enough fragments could be retrieved to rebuild the data");
        }
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
