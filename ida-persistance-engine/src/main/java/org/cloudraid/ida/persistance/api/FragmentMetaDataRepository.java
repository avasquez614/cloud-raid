package org.cloudraid.ida.persistance.api;

import org.cloudraid.ida.persistance.exception.RepositoryException;

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
    void saveFragmentMetaData(FragmentMetaData metaData) throws RepositoryException;

    /**
     * Returns the metadata of all fragments of a particular data. The fragments must be associated to the current hash
     * of the data.
     *
     * @param dataId
     *          the data ID
     * @return a list of {@link FragmentMetaData} objects for the fragments of the data (empty if no metadata was found).
     */
    List<FragmentMetaData> getAllFragmentMetaDataForData(String dataId) throws RepositoryException;

    /**
     * Returns the metadata of a single fragment.
     *
     * @param dataId
     *          the data ID
     * @param fragmentNumber
     *          the fragment number
     * @return the fragment metadata, or null if not found
     */
    FragmentMetaData getFragmentMetaData(String dataId, int fragmentNumber) throws RepositoryException;

    /**
     * Deletes the fragment metadata specified by the data ID and fragment number.
     *
     * @param metaData
     *          the metadata to delete
     */
    void deleteFragmentMetaData(FragmentMetaData metaData) throws RepositoryException;

}
