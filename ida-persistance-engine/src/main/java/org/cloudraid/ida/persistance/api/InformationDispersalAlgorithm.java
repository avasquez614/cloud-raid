package org.cloudraid.ida.persistance.api;

import java.util.List;

/**
 * Represents an actual information dispersal algorithm, which splits data into N fragments, and can
 * reconstruct said data from M fragments (where N >= M and N and M are configurable).
 */
public interface InformationDispersalAlgorithm {

    /**
     * Sets the number of fragments in which the data should be splitted.
     */
    void setNumberOfFragments(int numberOfFragments);

    /**
     * Sets the number of redundant fragments (number of fragments that can be spared in case the data
     * needs to be restored from less fragments).
     */
    void setNumberOfRedundantFragments(int numberOfRedundantFragments);

    /**
     * Splits the given data into {@code numberOfFragments}.
     *
     * @param data
     *          the data to split
     * @return the fragments obtained from the given data
     */
    List<byte[]> split(byte[] data);

    /**
     * Combines the given fragment to reconstruct the original data.
     *
     * @param fragments
     *          the fragments to reconstruct the data from (should be at least
     *          {@code numberOfFragments} - {@code numberOfRedundantFragments});
     * @return the reconstructed, original data.
     */
    byte[] combine(List<byte[]> fragments);

}
