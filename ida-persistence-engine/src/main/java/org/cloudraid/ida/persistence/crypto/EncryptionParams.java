package org.cloudraid.ida.persistence.crypto;

/**
 * Params used for encryption/decryption.
 *
 * @author avasquez
 */
public interface EncryptionParams {

    /**
     * Returns the encryption key.
     */
    byte[] getKey();

    /**
     * Returns the encryption algorithm initialization vector.
     */
    byte[] getIv();

}
