package org.cloudraid.ida.persistance.api;

/**
 * The fragment information, stored in a {@link FragmentRegistry} to be used for later fragment retrieval.
 *
 * @author avasquez
 */
public class FragmentInfo {

    private String dataId;
    private int fragmentNumber;
    private String repositoryUrl;

    public FragmentInfo(String dataId, String repositoryUrl) {
        this.dataId = dataId;
        this.repositoryUrl = repositoryUrl;
    }

    public FragmentInfo(String dataId, int fragmentNumber, String repositoryUrl) {
        this.dataId = dataId;
        this.fragmentNumber = fragmentNumber;
        this.repositoryUrl = repositoryUrl;
    }

    /**
     * Returns the complete data (unfragmented) identifier.
     */
    public String getDataId() {
        return dataId;
    }

    /**
     * Returns the fragment number.
     */
    public int getFragmentNumber() {
        return fragmentNumber;
    }

    /**
     * Returns the fragment repository URL.
     */
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

}
