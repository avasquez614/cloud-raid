package org.cloudraid.ida.persistence.crypto.jce;

import org.apache.commons.lang.StringUtils;
import org.cloudraid.ida.persistence.api.Configuration;
import org.cloudraid.ida.persistence.crypto.Decryptor;
import org.cloudraid.ida.persistence.crypto.EncryptionParams;
import org.cloudraid.ida.persistence.crypto.EncryptionProvider;
import org.cloudraid.ida.persistence.crypto.Encryptor;
import org.cloudraid.ida.persistence.exception.CryptoException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * {@link EncryptionProvider} implementation that uses the Java Cryptography Extension.
 *
 * @author avasquez
 */
public class JceEncryptionProvider implements EncryptionProvider {

    private String cipherTransformation;
    private String algorithm;
    private int keySize;
    private SecureRandom secureRandom;

    /**
     * Initializes the provider, extracting the CipherTransformation and KeySize params from the {@link Configuration}.
     * It also creates a {@link SecureRandom} used to generate the random encryption params (key and IV).
     *
     * @param config
     *          the configuration with the CipherTransformation param.
     * @throws CryptoException
     */
    @Override
    public void init(Configuration config) throws CryptoException {
        cipherTransformation = config.getInitParameter("CipherTransformation");
        if (StringUtils.isNotEmpty(cipherTransformation)) {
            // The algorithm is the first part of the transformation, separated by /.
            algorithm = StringUtils.substringBefore(cipherTransformation, "/");
        } else {
            throw new CryptoException("No CipherTransformation param specified");
        }

        String keySizeParam = config.getInitParameter("KeySize");
        if (StringUtils.isNotEmpty(keySizeParam)) {
            try {
                keySize = Integer.parseInt(keySizeParam);
            } catch (Exception e) {
                throw new CryptoException("Invalid format for KeySize param '" + keySizeParam + "'", e);
            }
        } else {
            throw new CryptoException("No KeySize param specified");
        }

        secureRandom = new SecureRandom();
    }

    /**
     * Creates a new {@link JceEncryptionParams}, using randomly generated key and IV.
     *
     * @return a new {@link JceEncryptionParams}
     * @throws CryptoException
     */
    @Override
    public EncryptionParams createDefaultParams() throws CryptoException {
        SecretKey secretKey = new SecretKeySpec(generateRandomBytes(keySize), algorithm);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(generateRandomBytes(keySize));

        return new JceEncryptionParams(createCipher(), secretKey, ivParameterSpec);
    }

    /**
     * Creates a new {@link JceEncryptionParams}, using the specified key and IV.
     *
     * @param key
     * @param iv
     * @return
     * @throws CryptoException
     */
    @Override
    public EncryptionParams createParams(byte[] key, byte[] iv) throws CryptoException {
        SecretKey secretKey = new SecretKeySpec(key, algorithm);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        return new JceEncryptionParams(createCipher(), secretKey, ivParameterSpec);
    }

    /**
     * Creates a new {@link JceEncryptor}, with the specified params. The params should be an instance of
     * {@link JceEncryptionParams}.
     *
     * @param params
     *          the parameters the encryption algorithm should use
     * @return the new {@link JceEncryptor}
     * @throws CryptoException
     */
    @Override
    public Encryptor getEncryptor(EncryptionParams params) throws CryptoException {
        if (!(params instanceof JceEncryptionParams)) {
            throw new CryptoException("EncryptionParams should be an instance of " + JceEncryptionParams.class.getName());
        }

        return new JceEncryptor((JceEncryptionParams) params);
    }

    /**
     * Creates a new {@link JceDecryptor}, with the specified params. The params should be an instance of
     * {@link JceEncryptionParams}.
     *
     * @param params
     *          the parameters the encryption algorithm should use
     * @return the new {@link JceDecryptor}
     * @throws CryptoException
     */
    @Override
    public Decryptor getDecryptor(EncryptionParams params) throws CryptoException {
        if (!(params instanceof JceEncryptionParams)) {
            throw new CryptoException("EncryptionParams should be an instance of " + JceEncryptionParams.class.getName());
        }

        return new JceDecryptor((JceEncryptionParams) params);
    }

    /**
     * Returns a new {@link Cipher}, based on the {@code cipherTransformation}.
     *
     * @throws CryptoException
     */
    protected Cipher createCipher() throws CryptoException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(cipherTransformation);
        } catch (Exception e) {
            throw new CryptoException("Unable to create cipher for transformation '" + cipherTransformation + "'", e);
        }

        return cipher;
    }

    /**
     * Returns random bytes (used for key and IV).
     *
     * @param numBytes
     *          the number of random bytes to generate
     */
    protected byte[] generateRandomBytes(int numBytes) {
        byte[] bytes = new byte[numBytes];

        secureRandom.nextBytes(bytes);

        return bytes;
    }

}
