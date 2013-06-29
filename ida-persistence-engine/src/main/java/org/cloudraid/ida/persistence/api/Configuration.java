package org.cloudraid.ida.persistence.api;

/**
 * A configuration object used to pass information to a component during initialization.
 */
public interface Configuration {

    /**
     * Returns the param value for the specified param name.
     *
     * @param name
     *          the param name
     * @return the param value
     */
    String getInitParameter(String name);

    /**
     * Returns the {@link Context} instance.
     */
    Context getContext();

}
