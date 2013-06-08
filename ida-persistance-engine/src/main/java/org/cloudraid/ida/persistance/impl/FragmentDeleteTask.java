package org.cloudraid.ida.persistance.impl;

import org.apache.log4j.Logger;
import org.cloudraid.ida.persistance.api.FragmentMetaData;
import org.cloudraid.ida.persistance.api.FragmentMetaDataRepository;
import org.cloudraid.ida.persistance.api.FragmentRepository;

import java.util.concurrent.Callable;

/**
 * Asynchronous task, implemented as a {@link Callable}, to delete a fragment from the repository.
 */
public class FragmentDeleteTask implements Callable<Boolean> {

    private static final Logger logger = Logger.getLogger(FragmentDeleteTask.class);

    protected FragmentMetaData metaData;
    protected FragmentRepository fragmentRepository;
    protected FragmentMetaDataRepository metaDataRepository;

    public FragmentDeleteTask(FragmentMetaData metaData, FragmentRepository fragmentRepository,
                              FragmentMetaDataRepository metaDataRepository) {
        this.metaData = metaData;
        this.fragmentRepository = fragmentRepository;
        this.metaDataRepository = metaDataRepository;
    }

    @Override
    public Boolean call() throws Exception {
        String fragmentName = getFragmentName();

        if (logger.isDebugEnabled()) {
            logger.debug("Deleting fragment metadata " + metaData);
        }

        try {
            metaDataRepository.deleteFragmentMetaData(metaData);
        } catch (Exception e) {
            logger.error("Error while trying to delete fragment metadata " + metaData, e);

            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Deleting fragment '" + fragmentName + "' from " + fragmentRepository);
        }

        try {
            boolean deleted = fragmentRepository.deleteFragment(fragmentName);
            if (!deleted) {
                logger.warn("Fragment '" + fragmentName + "' wasn't deleted from " + fragmentRepository);
            }

            return deleted;
        } catch (Exception e) {
            logger.error("Error while trying to delete fragment '" + fragmentName + "' from " + fragmentRepository, e);

            return false;
        }
    }

    protected String getFragmentName() {
        return metaData.getDataId() + "." + InformationDispersalPersistanceServiceImpl.FRAGMENT_FILE_EXT;
    }

    @Override
    public String toString() {
        return "FragmentDeleteTask[" +
                "metaData=" + metaData +
                ", fragmentRepository=" + fragmentRepository +
                ", metaDataRepository=" + metaDataRepository +
                ']';
    }

}
