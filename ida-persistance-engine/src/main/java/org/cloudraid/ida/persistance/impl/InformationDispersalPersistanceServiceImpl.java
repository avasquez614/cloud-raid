package org.cloudraid.ida.persistance.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.cloudraid.ida.persistance.api.*;
import org.cloudraid.ida.persistance.exception.IdaPersistanceException;
import org.cloudraid.ida.persistance.exception.RepositoryException;

import java.io.File;
import java.io.IOException;
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
    protected File temporaryFragmentDir;
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
    public void setTemporaryFragmentDir(File directory) {
        this.temporaryFragmentDir = directory;
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
        List<FragmentMetaData> savedFragmentsMetaData;
        try {
            savedFragmentsMetaData = metaDataRepository.getAllFragmentMetaDataForData(id);
        } catch (Exception e) {
            throw new IdaPersistanceException("Error while retrieving metadata for fragments of data '" + id + "'", e);
        }

        CompletionService<Boolean> saveCompletionService = new ExecutorCompletionService<Boolean>(taskExecutor);
        int savedNum = 0;
        List<FragmentSaveTask> saveTasks;

        if (CollectionUtils.isNotEmpty(savedFragmentsMetaData)) {
            saveTasks = getPendingFragmentSaveTasks(id, savedFragmentsMetaData);
        } else {
            saveTasks = getAllFragmentSaveTasks(id, data);
        }

        for (FragmentSaveTask task : saveTasks) {
            saveCompletionService.submit(task);
        }

        for (int i = 0; i < saveTasks.size(); i++) {
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
        if (savedNum < saveTasks.size()) {
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

        List<FragmentLoadTask> loadTasks = new ArrayList<FragmentLoadTask>();
        CompletionService<byte[]> loadCompletionService = new ExecutorCompletionService<byte[]>(taskExecutor);

        // Don't immediately submit the task: we want to make sure all repository URLs in the metadata are correct.
        for (FragmentMetaData metaData : fragmentsMetaData) {
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

    /**
     * Returns the save tasks for all fragments obtained through IDA, when there hasn't been a previous save operation. It
     * also saves the fragments to temporary files in case the save operation fails, so it can be resumed on a next call.
     */
    protected List<FragmentSaveTask> getAllFragmentSaveTasks(String dataId, byte[] data) throws IdaPersistanceException {
        List<byte[]> fragments;
        try {
            fragments = ida.split(data);
        } catch (Exception e) {
            throw new IdaPersistanceException("Error while trying to split the data", e);
        }

        File unsavedFragmentsTempDir = new File(temporaryFragmentDir, dataId);
        List<FragmentSaveTask> saveTasks = new ArrayList<FragmentSaveTask>(fragments.size());

        for (int i = 0; i < fragments.size(); i++) {
            byte[] fragment = fragments.get(i);
            FragmentRepository repository = repositories.get(i);
            FragmentMetaData metaData = new FragmentMetaData(dataId, i, repository.getRepositoryUrl());

            File tempFile = new File(unsavedFragmentsTempDir, i + "." + FRAGMENT_FILE_EXT);
            try {
                FileUtils.writeByteArrayToFile(tempFile, fragment);
            } catch (IOException e) {
                throw new IdaPersistanceException("Unable to write fragment to temp file " + tempFile, e);
            }

            saveTasks.add(new FragmentSaveTask(fragment, tempFile, metaData, repository, metaDataRepository));
        }

        return saveTasks;
    }

    /**
     * Returns the save tasks for unsaved fragments of a previous save operation.
     */
    protected List<FragmentSaveTask> getPendingFragmentSaveTasks(String dataId, List<FragmentMetaData> savedFragmentsMetaData)
            throws IdaPersistanceException {
        File unsavedFragmentsTempDir = new File(temporaryFragmentDir, dataId);

        if (unsavedFragmentsTempDir.exists()) {
            File[] fragmentFiles = unsavedFragmentsTempDir.listFiles();

            if (ArrayUtils.isNotEmpty(fragmentFiles)) {
                List<FragmentRepository> unusedRepositories = getUnusedFragmentRepositories(savedFragmentsMetaData);
                List<FragmentSaveTask> saveTasks = new ArrayList<FragmentSaveTask>(fragmentFiles.length);

                for (int i = 0; i < fragmentFiles.length; i++) {
                    File tempFile = fragmentFiles[i];
                    int fragmentNumber = Integer.parseInt(FilenameUtils.getBaseName(tempFile.getName()));

                    // Check if the fragment has actually been saved. This only happens if the fragment was saved but
                    // the temp fragment file couldn't be deleted. In case it's saved, delete the temp file and don't
                    // continue trying to save the file.
                    if (isFragmentSaved(fragmentNumber, savedFragmentsMetaData)) {
                        if (!tempFile.delete()) {
                            throw new IdaPersistanceException("Unable to delete temporary fragment file " + tempFile);
                        }
                    } else {
                        byte[] fragment;
                        try {
                            fragment = FileUtils.readFileToByteArray(tempFile);
                        } catch (IOException e) {
                            throw new IdaPersistanceException("Unable to read temporary fragment file " + tempFile, e);
                        }

                        FragmentRepository repository = unusedRepositories.get(i);
                        String repositoryUrl = repository.getRepositoryUrl();
                        FragmentMetaData metaData = new FragmentMetaData(dataId, fragmentNumber, repositoryUrl);

                        saveTasks.add(new FragmentSaveTask(fragment, tempFile, metaData, repository, metaDataRepository));
                    }
                }

                return saveTasks;
            } else {
                throw new IdaPersistanceException("There are unsaved fragments, but there are no files under the " +
                        "temporary directory " + unsavedFragmentsTempDir + " where they should be");
            }
        } else {
            throw new IdaPersistanceException("There are unsaved fragments, but the temporary directory where they " +
                    "should be " + unsavedFragmentsTempDir + " doesn't exist");
        }
    }

    protected List<FragmentRepository> getUnusedFragmentRepositories(List<FragmentMetaData> savedFragmentsMetaData) {
        List<FragmentRepository> unusedRepositories = new ArrayList<FragmentRepository>();

        for (FragmentRepository repository : repositories) {
            boolean repositoryFound = false;

            for (FragmentMetaData metaData : savedFragmentsMetaData) {
                if (repository.getRepositoryUrl().equals(metaData.getRepositoryUrl())) {
                    repositoryFound = true;
                    break;
                }
            }

            if (!repositoryFound) {
                unusedRepositories.add(repository);
            }
        }

        return unusedRepositories;
    }

    protected boolean isFragmentSaved(int fragmentNumber, List<FragmentMetaData> savedFragmentsMetaData) {
        for (FragmentMetaData metaData : savedFragmentsMetaData) {
            if (fragmentNumber == metaData.getFragmentNumber()) {
                return true;
            }
        }

        return false;
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
