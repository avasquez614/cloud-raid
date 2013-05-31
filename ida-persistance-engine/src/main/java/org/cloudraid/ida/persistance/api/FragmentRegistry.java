package org.cloudraid.ida.persistance.api;

import java.util.List;

/**
 * Registry where the stored fragment information is kept.
 *
 * @author avasquez
 */
public interface FragmentRegistry {

    void saveFragmentInfo(FragmentInfo info);

    List<FragmentInfo> getFragmentsInfo(String dataId);

    FragmentInfo getFragmentInfo(String dataId, String fragmentNumber);

}
