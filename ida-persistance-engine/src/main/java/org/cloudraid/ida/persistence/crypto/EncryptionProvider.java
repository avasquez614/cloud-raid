package org.cloudraid.ida.persistence.crypto;

import org.cloudraid.ida.persistence.api.Configuration;
import org.cloudraid.ida.persistence.exception.CryptoException;

/**
 * Provides a {@link Encryptor} to encrypt data and a {@link Decryptor} to decrypt encrypted data. The implementation
 * decides which encryption algorithm to use.
 *
 * @author avasquez.
 */
public interface EncryptionProvider {

    /**
     * Initializes the crypto provider.
     *
     * @param config
     *          the configuration that contains the initialization parameters and possible dependencies
     */
    void init(Configuration config) throws CryptoException;

    /**
     * Creates default encryption params. All the implementations should at least supply randomly generated key and IV.
     *
     * @return the default encryption params.
     * @throws CryptoException
     */
    EncryptionParams createDefaultParams() throws CryptoException;

    /**
     * Creates a {@link EncryptionParams} from the specified key and IV.
     *
     * @param key
     * @param iv
     * @return
     * @throws CryptoException
     */
    EncryptionParams createParams(byte[] key, byte[] iv) throws CryptoException;

    /**
     * Creates a new encryptor using the implementation-specific algorithm and the specified key. The encryptor is not
     * reusable.
     *
     * @param params
     *          the parameters the encryption algorithm should use
     * @return the new encryptor
     */
    Encryptor getEncryptor(EncryptionParams params) throws CryptoException;

    /**
     * Creates a new decryptor using the implementation-specific algorithm and the specified key. The decryptor is not
     * reusable.
     *
     * @param params
     *          the parameters the encryption algorithm should use
     * @return the new decryptor
     */
    Decryptor getDecryptor(EncryptionParams params) throws CryptoException;

}
