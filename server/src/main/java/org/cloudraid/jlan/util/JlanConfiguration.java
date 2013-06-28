package org.cloudraid.jlan.util;

import org.cloudraid.ida.persistence.api.Configuration;
import org.cloudraid.ida.persistence.api.Context;
import org.springframework.extensions.config.ConfigElement;

/**
 * Implementation of {@link Configuration} for the JLan server. Uses a {@link ConfigElement} to retrieve the init params.
 */
public class JlanConfiguration implements Configuration {

    protected ConfigElement configElement;
    protected Context context;

    public JlanConfiguration(ConfigElement configElement, Context context) {
        this.configElement = configElement;
        this.context = context;
    }

    @Override
    public String getInitParameter(String name) {
        ConfigElement childConfig = configElement.getChild(name);
        if (childConfig != null) {
            return childConfig.getValue();
        } else {
            return null;
        }
    }

    @Override
    public Context getContext() {
        return context;
    }

}
