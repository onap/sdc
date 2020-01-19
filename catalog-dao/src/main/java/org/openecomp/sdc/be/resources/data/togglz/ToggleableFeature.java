package org.openecomp.sdc.be.resources.data.togglz;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

import java.util.Arrays;

public enum ToggleableFeature implements Feature{
    @Label("Default feature")
    DEFAULT_FEATURE;

    public static Feature getFeatureByName(String featureName) {
        return Arrays.stream(values()).
                filter(e -> e.name().equals(featureName))
                .findFirst()
                .orElse(null);
    }

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}
