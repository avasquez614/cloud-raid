package org.cloudraid.ida.persistence.impl;

import org.apache.log4j.Logger;
import org.cloudraid.ida.persistence.api.FragmentMetaData;
import org.cloudraid.ida.persistence.api.FragmentMetaDataRepository;
import org.cloudraid.ida.persistence.api.FragmentRepository;

import java.util.concurrent.Callable;

/**
 * Asynchronous task, implemented as a {@link Callable}, to save a fragment in the repository.
 *
 * @author avasquez
 */
public class FragmentSaveTask implements Callable<Boolean> {

    private static final Logger logger = Logger.getLogger(FragmentSaveTask.class);

    protected byte[] fragment;
    protected FragmentMetaData fragmentMetaData;
    protected FragmentRepository fragmentRepository;
    protected FragmentMetaDataRepository fragmentMetaDataRepository;

    public FragmentSaveTask(byte[] fragment, FragmentMetaData fragmentMetaData, FragmentRepository fragmentRepository,
                            FragmentMetaDataRepository fragmentMetaDataRepository) {
        this.fragment = fragment;
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

        if (logger.isDebugEnabled()) {
            logger.debug("Saving fragment '" + fragmentName + "' in " + fragmentRepository);
        }

        try {
            fragmentRepository.saveFragment(fragmentName, fragment);
        } catch (Exception e) {
            logger.error("Error while trying to save fragment '" + fragmentName + "' in " + fragmentRepository, e);

            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Saving fragment metadata " + fragmentMetaData);
        }

        try {
            fragmentMetaDataRepository.saveFragmentMetaData(fragmentMetaData);
        } catch (Exception e) {
            logger.error("Error while trying to save fragment metadata " + fragmentMetaData, e);

            return false;
        }

        return true;
    }

    protected String getFragmentName() {
        return fragmentMetaData.getDataId() + "." + InformationDispersalPersistenceServiceImpl.FRAGMENT_FILE_EXT;
    }

    @Override
    public String toString() {
        return "FragmentSaveTask[" +
                "fragmentMetaData=" + fragmentMetaData +
                ", fragmentRepository=" + fragmentRepository +
                ", fragmentMetaDataRepository=" + fragmentMetaDataRepository +
                ']';
    }

}
