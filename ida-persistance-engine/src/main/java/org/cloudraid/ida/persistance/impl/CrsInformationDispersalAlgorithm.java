package org.cloudraid.ida.persistance.impl;

import org.cloudraid.ida.persistance.api.InformationDispersalAlgorithm;
import org.cloudraid.ida.persistance.exception.IdaException;
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

    public static final int CHUNK_SIZE = 4096;

    private int fragmentNumber;
    private int redundantFragmentNumber;
    private CauchyInformationDispersalCodec codec;

    @Override
    public int getFragmentNumber() {
        return fragmentNumber;
    }

    @Override
    public void setFragmentNumber(int fragmentNumber) {
        this.fragmentNumber = fragmentNumber;
    }

    @Override
    public int getRedundantFragmentNumber() {
        return redundantFragmentNumber;
    }

    @Override
    public void setRedundantFragmentNumber(int redundantFragmentNumber) {
        this.redundantFragmentNumber = redundantFragmentNumber;
    }

    @Override
    public void init() throws IdaException {
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

}
