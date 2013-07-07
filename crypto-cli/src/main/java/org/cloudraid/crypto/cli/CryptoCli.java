package org.cloudraid.crypto.cli;

import org.apache.commons.cli.*;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.Key;

/**
 * Command line interface app to encrypt/decrypt files.
 *
 * @author avasquez
 */
public class CryptoCli {

    public static void main(String... args) throws Exception {
        CommandLineParser parser = new BasicParser();

        Options opts = new Options();
        opts.addOption("enc", true, "encrypt with the following argument as the cipher transformation");
        opts.addOption("dec", true, "decrypt with the following argument as the cipher transformation");
        opts.addOption("in", true, "the encryption/decryption file input");
        opts.addOption("out", true, "the encryption/decryption file output");
        opts.addOption("key", true, "the encryption/decryption key (in hexadecimal)");
        opts.addOption("iv", true, "the encryption/decryption IV (in hexadecimal)");
        opts.addOption("help", false, "print usage help");

        try {
            CommandLine commandLine = parser.parse(opts, args);

            int cipherMode;
            String cipherTransformation;
            File inputFile;
            File outputFile;
            String hexKey;
            String hexIv;

            if (commandLine.hasOption("help")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("crypto", opts);

                System.exit(0);
            }

            if (commandLine.hasOption("enc")) {
                cipherMode = Cipher.ENCRYPT_MODE;
                cipherTransformation = commandLine.getOptionValue("enc");
            } else if (commandLine.hasOption("dec")) {
                cipherMode = Cipher.DECRYPT_MODE;
                cipherTransformation = commandLine.getOptionValue("dec");
            } else {
                throw new ParseException("You need to specify either -enc or -dec with a cipher transformation argument");
            }

            if (commandLine.hasOption("in")) {
                inputFile = new File(commandLine.getOptionValue("in"));
            } else {
                throw new ParseException("You need to specify -in with the input file as argument");
            }

            if (commandLine.hasOption("out")) {
                outputFile = new File(commandLine.getOptionValue("out"));
            } else {
                throw new ParseException("You need to specify -out with the output file as argument");
            }

            if (commandLine.hasOption("key")) {
                hexKey = commandLine.getOptionValue("key");
            } else {
                throw new ParseException("You need to specify -key with the encryption/decryption key as argument (in hexadecimal)");
            }

            if (commandLine.hasOption("iv")) {
                hexIv = commandLine.getOptionValue("iv");
            } else {
                throw new ParseException("You need to specify -iv with the encryption/decryption IV as argument (in hexadecimal)");
            }

            cipher(cipherMode, cipherTransformation, inputFile, outputFile, hexKey, hexIv);
        } catch (ParseException e) {
            die("Parse error: " + e.getMessage());
        } catch (Exception e) {
            throw e;
        }
    }

    private static void cipher(int cipherMode, String cipherTransformation, File inputFile, File outputFile, String hexKey, String hexIv)
            throws Exception {
        Cipher cipher = Cipher.getInstance(cipherTransformation);
        Key key = getKey(cipherTransformation, hexKey);
        IvParameterSpec iv = getIv(hexIv);
        byte[] input = getInput(inputFile);

        cipher.init(cipherMode, key, iv);

        byte[] output = cipher.doFinal(input);
        writeOutput(output, outputFile);
    }

    private static Key getKey(String cipherTransformation, String hexKey) throws Exception {
        // The algorithm is the first part of the transformation, separated by /.
        String algorithm = StringUtils.substringBefore(cipherTransformation, "/");
        byte[] key = Hex.decodeHex(hexKey.toCharArray());

        return new SecretKeySpec(key, algorithm);
    }

    private static IvParameterSpec getIv(String hexIv) throws Exception {
        byte[] iv = Hex.decodeHex(hexIv.toCharArray());

        return new IvParameterSpec(iv);
    }

    private static byte[] getInput(File inputFile) throws Exception {
        InputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
        try {
            return IOUtils.toByteArray(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private static void writeOutput(byte[] output, File outputFile) throws Exception {
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
        try {
            IOUtils.write(output, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private static void die(String msg) {
        System.out.println(msg);
        System.exit(-1);
    }

}
