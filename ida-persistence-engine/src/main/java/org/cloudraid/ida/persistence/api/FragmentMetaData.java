package org.cloudraid.ida.persistence.api;

/**
 * The fragment metadata, stored in a {@link FragmentMetaDataRepository} to be used for later fragment retrieval.
 *
 * @author avasquez
 */
public class FragmentMetaData {

    private String dataId;
    private int fragmentNumber;
    private String repositoryUrl;

    public FragmentMetaData(String dataId, int fragmentNumber, String repositoryUrl) {
        this.dataId = dataId;
        this.fragmentNumber = fragmentNumber;
        this.repositoryUrl = repositoryUrl;
    }

    /**
     * Returns the data from where this fragment was obtained.
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
     * Returns the repository URL where the fragment is stored.
     */
    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    @Override
    public String toString() {
        return "FragmentMetaData[" +
                "dataId='" + dataId + '\'' +
                ", fragmentNumber=" + fragmentNumber +
                ", repositoryUrl='" + repositoryUrl + '\'' +
                ']';
    }

}
