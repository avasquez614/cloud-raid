package org.cloudraid.ida.persistence.crypto;

import org.cloudraid.ida.persistence.exception.RepositoryException;

/**
 * Repository for encryption keys.
 *
 * @author  avasquez
 */
public interface EncryptionKeyRepository {

    /**
     * Saves the specified encryption key for the specified data ID.
     *
     * @param dataId
     * @param key
     */
    void saveKey(String dataId, String key) throws RepositoryException;

    /**
     * Returns the specified encryption key for the specified data ID.
     *
     * @param dataId
     * @return the encryption key for the data ID
     */
    String getKey(String dataId) throws RepositoryException;

    /**
     * Deletes the encryption key for the specified data ID.
     *
     * @param dataId
     */
    void deleteKey(String dataId) throws RepositoryException;

}
