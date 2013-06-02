package org.cloudraid.ida.persistance.impl;

import org.apache.log4j.Logger;
import org.cloudraid.ida.persistance.api.FragmentMetaData;
import org.cloudraid.ida.persistance.api.FragmentRepository;

import java.util.concurrent.Callable;

/**
 * Asynchronous task, implemented as a {@link Callable}, to load a fragment from the repository.
 *
 * @author avasquez
 */
public class FragmentLoadTask implements Callable<byte[]> {

    private static final Logger logger = Logger.getLogger(FragmentLoadTask.class);

    protected FragmentMetaData fragmentMetaData;
    protected FragmentRepository fragmentRepository;

    public FragmentLoadTask(FragmentMetaData fragmentMetaData, FragmentRepository fragmentRepository) {
        this.fragmentMetaData = fragmentMetaData;
        this.fragmentRepository = fragmentRepository;
    }

    @Override
    public byte[] call() throws Exception {
        String fragmentName = getFragmentName();
        try {
            return fragmentRepository.loadFragment(fragmentName);
        } catch (Exception e) {
            logger.error("Error while trying to load fragment '" + fragmentName + "' from repository [" +
                    fragmentRepository.getRepositoryUrl() + "]");

            return null;
        }
    }

    protected String getFragmentName() {
        return fragmentMetaData.getDataId() + "." + InformationDispersalPersistanceServiceImpl.FRAGMENT_FILE_EXT;
    }

}
