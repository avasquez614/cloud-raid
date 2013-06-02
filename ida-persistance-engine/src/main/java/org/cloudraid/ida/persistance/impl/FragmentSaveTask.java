package org.cloudraid.ida.persistance.impl;

import org.apache.log4j.Logger;
import org.cloudraid.ida.persistance.api.FragmentMetaData;
import org.cloudraid.ida.persistance.api.FragmentMetaDataRepository;
import org.cloudraid.ida.persistance.api.FragmentRepository;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * Asynchronous task, implemented as a {@link Callable}, to save a fragment in the repository.
 *
 * @author avasquez
 */
public class FragmentSaveTask implements Callable<Boolean> {

    private static final Logger logger = Logger.getLogger(FragmentSaveTask.class);

    protected byte[] fragment;
    protected File fragmentTempFile;
    protected FragmentMetaData fragmentMetaData;
    protected FragmentRepository fragmentRepository;
    protected FragmentMetaDataRepository fragmentMetaDataRepository;

    public FragmentSaveTask(byte[] fragment, File fragmentTempFile, FragmentMetaData fragmentMetaData,
                            FragmentRepository fragmentRepository, FragmentMetaDataRepository fragmentMetaDataRepository) {
        this.fragment = fragment;
        this.fragmentTempFile = fragmentTempFile;
        this.fragmentMetaData = fragmentMetaData;
        this.fragmentRepository = fragmentRepository;
        this.fragmentMetaDataRepository = fragmentMetaDataRepository;
    }

    /**
     * Saves the fragment in the repository.
     *
     * @return the FragmentMetaData, indicating if the fragment was saved successfully or not.
     */
    @Override
    public Boolean call() {
        String fragmentName = getFragmentName();
        try {
            fragmentRepository.saveFragment(fragmentName, fragment);
        } catch (Exception e) {
            logger.error("Error while trying to save fragment '" + fragmentName + "' in repository [" +
                    fragmentRepository.getRepositoryUrl() + "]", e);

            return false;
        }

        try {
            fragmentMetaDataRepository.saveFragmentMetaData(fragmentMetaData);
        } catch (Exception e) {
            logger.error("Error while trying to save fragment metadata " + fragmentMetaData, e);

            return false;
        }

        if (!fragmentTempFile.delete()) {
            logger.error("Unable to delete temporary fragment file " + fragmentTempFile);

            return false;
        }

        return true;
    }

    protected String getFragmentName() {
        return fragmentMetaData.getDataId() + "." + InformationDispersalPersistanceServiceImpl.FRAGMENT_FILE_EXT;
    }

}
