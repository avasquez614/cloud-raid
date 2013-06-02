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
     * Saves the specified fragment metadata. The metadata shouldn't exist in the repository.
     *
     * @param metaData
     *          the metadata to save
     */
    void saveFragmentMetaData(FragmentMetaData metaData) throws RepositoryException;

    /**
     * Updates the specified fragment metadata. The metadata should already exist in the repository, through a
     * call to {@link #saveFragmentMetaData(FragmentMetaData)}.
     *
     * @param metaData
     *          the metadata to update
     */
    void updateFragmentMetaData(FragmentMetaData metaData) throws RepositoryException;

    /**
     * Returns the metadata of all fragments of a particular data.
     *
     * @param dataId
     *          the data ID
     * @return a list of {@link FragmentMetaData} objects for the fragments of the data.
     */
    List<FragmentMetaData> getAllFragmentMetaDataForData(String dataId) throws RepositoryException;

    /**
     * Returns the metadata of a single fragment.
     *
     * @param dataId
     *          the data ID
     * @param fragmentNumber
     *          the fragment number
     * @return the fragment metadata
     */
    FragmentMetaData getFragmentMetaData(String dataId, String fragmentNumber) throws RepositoryException;

}
