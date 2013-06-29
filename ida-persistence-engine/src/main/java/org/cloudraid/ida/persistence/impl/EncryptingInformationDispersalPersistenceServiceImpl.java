package org.cloudraid.ida.persistence.impl;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.cloudraid.ida.persistence.api.Configuration;
import org.cloudraid.ida.persistence.crypto.EncryptionKeyRepository;
import org.cloudraid.ida.persistence.crypto.EncryptionParams;
import org.cloudraid.ida.persistence.crypto.EncryptionProvider;
import org.cloudraid.ida.persistence.exception.IdaPersistenceException;

/**
 * Extends the {@link InformationDispersalPersistenceServiceImpl} to encrypt the data before saving, and decrypt it after
 * loading.
 *
 * @author avasquez
 */
public class EncryptingInformationDispersalPersistenceServiceImpl extends InformationDispersalPersistenceServiceImpl {

    private static final String SERIALIZED_KEY_SEPARATOR = "$";

    protected EncryptionProvider encryptionProvider;
    protected EncryptionKeyRepository keyRepository;

    @Override
    public void init(Configuration config) throws IdaPersistenceException {
        super.init(config);

        encryptionProvider = config.getContext().getEncryptionProvider();
        if (encryptionProvider == null) {
            throw new IdaPersistenceException("No EncryptionProvider found in Context");
        }
        keyRepository = config.getContext().getEncryptionKeyRepository();
        if (keyRepository == null) {
            throw new IdaPersistenceException("No EncryptionKeyRepository found in Context");
        }
    }

    /**
     * Saves the data, but first encrypts the data and stores the encryption key in the key repository.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @param data
     *          the data to save and encrypt
     * @throws IdaPersistenceException
     */
    @Override
    public void saveData(String id, byte[] data) throws IdaPersistenceException {
        EncryptionParams params;
        try {
            params = encryptionProvider.createDefaultParams();
        } catch (Exception e) {
            throw new IdaPersistenceException("Unable to create default encryption params", e);
        }

        try {
            data = encryptionProvider.getEncryptor(params).encrypt(data);
        } catch (Exception e) {
            throw new IdaPersistenceException("Failed to encrypt data '" + id + "'", e);
        }

        String serializedKey = Hex.encodeHexString(params.getKey()) + SERIALIZED_KEY_SEPARATOR + Hex.encodeHex(params.getIv());
        try {
            keyRepository.saveKey(id, serializedKey);
        } catch (Exception e) {
            throw new IdaPersistenceException("Unable to save encryption key for data '" + id + "' in repository", e);
        }

        super.saveData(id, data);
    }

    /**
     * Loads the data, and then decrypts it, using the key stored for the data ID.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @return the loaded data, decrypted
     * @throws IdaPersistenceException
     */
    @Override
    public byte[] loadData(String id) throws IdaPersistenceException {
        byte[] data = super.loadData(id);

        String serializedKey;
        try {
            serializedKey = keyRepository.getKey(id);
        } catch (Exception e) {
            throw new IdaPersistenceException("Unable to get encryption key for data '" + id + "'", e);
        }

        if (serializedKey == null) {
            throw new IdaPersistenceException("No encryption key found for data '" + id + "'");
        }

        byte[] key;
        byte[] iv;
        try {
            key = Hex.decodeHex(StringUtils.substringBefore(serializedKey, SERIALIZED_KEY_SEPARATOR).toCharArray());
            iv = Hex.decodeHex(StringUtils.substringAfter(serializedKey, SERIALIZED_KEY_SEPARATOR).toCharArray());
        } catch (Exception e) {
            throw new IdaPersistenceException("Unable to decode serialized encryption key from hex for data '" + id + "'", e);
        }

        EncryptionParams params;
        try {
            params = encryptionProvider.createParams(key, iv);
        } catch (Exception e) {
            throw new IdaPersistenceException("Unable to create encryption params from stored key and IV", e);
        }

        try {
            data = encryptionProvider.getDecryptor(params).decrypt(data);
        } catch (Exception e) {
            throw new IdaPersistenceException("Failed to decrypt data '" + id + "'", e);
        }

        return data;
    }

    /**
     * Deletes the fragments for the given data ID from their respective repositories. It also deletes the encryption key for the data
     * from the key repository.
     *
     * @param id
     *          the ID used to identify the data in all repositories
     * @return the number of fragments that were deleted.
     */
    @Override
    public int deleteData(String id) throws IdaPersistenceException {
        int fragmentsDeleted = super.deleteData(id);

        try {
            keyRepository.deleteKey(id);
        } catch (Exception e) {
            throw new IdaPersistenceException("Unable to delete encryption key for data '" + id + "'", e);
        }

        return fragmentsDeleted;
    }

}
