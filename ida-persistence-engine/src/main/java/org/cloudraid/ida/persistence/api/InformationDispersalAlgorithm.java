package org.cloudraid.ida.persistence.api;

import org.cloudraid.ida.persistence.exception.IdaException;

import java.util.List;

/**
 * Represents an actual information dispersal algorithm, which splits data into N fragments, and can
 * reconstruct said data from M fragments (where N >= M and N and M are configurable).
 *
 * @author avasquez
 */
public interface InformationDispersalAlgorithm {

    /**
     * Returns the number of fragments in which the data is splitted.
     */
    int getFragmentNumber();

    /**
     * Returns the number of redundant fragments (number of fragments that can be spared in case the data
     * needs to be restored from less fragments).
     */
    int getRedundantFragmentNumber();

    /**
     * Initializes the algorithm.
     *
     * @param config
     *          the configuration that contains the initialization parameters and possible dependencies
     */
    void init(Configuration config) throws IdaException;

    /**
     * Splits the given data into {@code numberOfFragments}.
     *
     * @param data
     *          the data to split
     * @return the fragments obtained from the given data
     */
    List<byte[]> split(byte[] data) throws IdaException;

    /**
     * Combines the given fragments to reconstruct the original data.
     *
     * @param fragments
     *          the fragments to reconstruct the data from (should be at least
     *          {@code numberOfFragments} - {@code numberOfRedundantFragments});
     * @return the reconstructed, original data.
     */
    byte[] combine(List<byte[]> fragments) throws IdaException;

}
