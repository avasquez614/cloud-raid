package org.cloudraid.ida.persistence.impl;

import org.apache.commons.lang.StringUtils;
import org.cloudraid.ida.persistence.api.Configuration;
import org.cloudraid.ida.persistence.api.InformationDispersalAlgorithm;
import org.cloudraid.ida.persistence.exception.IdaException;
import org.jigdfs.ida.base.InformationDispersalDecoder;
import org.jigdfs.ida.base.InformationDispersalEncoder;
import org.jigdfs.ida.cauchyreedsolomon.CauchyInformationDispersalCodec;

import java.util.List;

/**
 * The Cauchy-Reed-Solomon IDA, using the JigDFS implementation.
 *
 * @author avasquez
 */
public class CrsInformationDispersalAlgorithm implements InformationDispersalAlgorithm {

    public static final int DEFAULT_REDUNDANT_FRAG_NUM = 2;
    public static final int CHUNK_SIZE = 4096;

    private int fragmentNumber;
    private int redundantFragmentNumber;
    private CauchyInformationDispersalCodec codec;

    @Override
    public int getFragmentNumber() {
        return fragmentNumber;
    }

    @Override
    public int getRedundantFragmentNumber() {
        return redundantFragmentNumber;
    }

    @Override
    public void init(Configuration config) throws IdaException {
        String fragmentNumParam = config.getInitParameter("FragmentNum");
        String redundantFragmentNumParam = config.getInitParameter("RedundantFragmentNum");

        if (StringUtils.isEmpty(fragmentNumParam)) {
            throw new IdaException("No FragmentNum param specified");
        }

        try {
            fragmentNumber = Integer.parseInt(fragmentNumParam);
        } catch (NumberFormatException e) {
            throw new IdaException("Invalid format for FragmentNum param '" + fragmentNumParam + "'", e);
        }

        if (StringUtils.isNotEmpty(redundantFragmentNumParam)) {
            try {
                redundantFragmentNumber = Integer.parseInt(redundantFragmentNumParam);
            } catch (NumberFormatException e) {
                throw new IdaException("Invalid format for RedundantFragmentNum param '" + redundantFragmentNumParam +
                        "'", e);
            }
        } else {
            redundantFragmentNumber = DEFAULT_REDUNDANT_FRAG_NUM;
        }

        if (redundantFragmentNumber >= fragmentNumber) {
            throw new IdaException("Redundant fragment number '" + redundantFragmentNumber + "' can't be greater than " +
                    "or equal to fragment number '" + fragmentNumber + "'");
        }

        int numSlices = fragmentNumber;
        int threshold = fragmentNumber - redundantFragmentNumber;
        int chunkSize = CHUNK_SIZE;

        try {
            codec = new CauchyInformationDispersalCodec(numSlices, threshold, chunkSize);
        } catch (Exception e) {
            throw new IdaException("Unable to create CauchyInformationDispersalCodec with numSlices = " +  numSlices +
                    ", threshold = " + threshold + " and chunkSize = " + chunkSize, e);
        }
    }

    @Override
    public List<byte[]> split(byte[] data) throws IdaException {
        InformationDispersalEncoder encoder;
        try {
            encoder = codec.getEncoder();
        } catch (Exception e) {
            throw new IdaException("Unable to retrieve encoder", e);
        }

        try {
            return encoder.process(data);
        } catch (Exception e) {
            throw new IdaException("Error while splitting data", e);
        }
    }

    @Override
    public byte[] combine(List<byte[]> fragments) throws IdaException {
        InformationDispersalDecoder decoder;
        try {
            decoder = codec.getDecoder();
        } catch (Exception e) {
            throw new IdaException("Unable to retrieve decoder", e);
        }

        try {
            return decoder.process(fragments);
        } catch (Exception e) {
            throw new IdaException("Error while combining data", e);
        }
    }

    @Override
    public String toString() {
        return "CrsInformationDispersalAlgorithm[" +
                "fragmentNumber=" + fragmentNumber +
                ", redundantFragmentNumber=" + redundantFragmentNumber +
                ", codec=" + codec +
                ']';
    }

}
