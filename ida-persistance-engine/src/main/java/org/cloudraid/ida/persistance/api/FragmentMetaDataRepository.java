package org.cloudraid.ida.persistance.api;

import java.util.List;

/**
 * Repository where the stored fragments metadata is kept.
 *
 * @author avasquez
 */
public interface FragmentMetaDataRepository {

    /**
     * Saves the specified fragment metadata.
     *
     * @param metaData
     *          the metadata to save
     */
    void saveFragmentMetaData(FragmentMetaData metaData);

    /**
     * Returns the metadata of all fragments of a particular data.
     *
     * @param dataId
     *          the data ID
     * @return a list of {@link FragmentMetaData} objects for the fragments of the data.
     */
    List<FragmentMetaData> getAllFragmentMetaDataForData(String dataId);

    /**
     * Returns the metadata of a single fragment.
     *
     * @param dataId
     *          the data ID
     * @param fragmentNumber
     *          the fragment number
     * @return the fragment metadata
     */
    FragmentMetaData getFragmentMetaData(String dataId, String fragmentNumber);

}
