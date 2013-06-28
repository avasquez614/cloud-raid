package org.cloudraid.ida.persistence.impl;

import org.apache.log4j.Logger;
import org.cloudraid.ida.persistence.api.FragmentRepository;

import java.util.concurrent.Callable;

/**
 * Asynchronous task, implemented as a {@link Callable}, to load a fragment from the repository.
 *
 * @author avasquez
 */
public class FragmentLoadTask implements Callable<byte[]> {

    private static final Logger logger = Logger.getLogger(FragmentLoadTask.class);

    protected String dataId;
    protected FragmentRepository fragmentRepository;

    public FragmentLoadTask(String dataId, FragmentRepository fragmentRepository) {
        this.dataId = dataId;
        this.fragmentRepository = fragmentRepository;
    }

    @Override
    public byte[] call() throws Exception {
        String fragmentName = getFragmentName();

        if (logger.isDebugEnabled()) {
            logger.debug("Loading fragment '" + fragmentName + "' from " + fragmentRepository);
        }

        try {
            return fragmentRepository.loadFragment(fragmentName);
        } catch (Exception e) {
            logger.error("Error while trying to load fragment '" + fragmentName + "' from " + fragmentRepository, e);

            return null;
        }
    }

    protected String getFragmentName() {
        return dataId + "." + InformationDispersalPersistenceServiceImpl.FRAGMENT_FILE_EXT;
    }

    @Override
    public String toString() {
        return "FragmentLoadTask[" +
                "dataId='" + dataId + '\'' +
                ", fragmentRepository=" + fragmentRepository +
                ']';
    }

}
