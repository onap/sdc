package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.model.HeatParameterDefinition;

public class HeatParameterBuilder {

    private HeatParameterDefinition heatParameterDefinition;

    public HeatParameterBuilder() {
        heatParameterDefinition = new HeatParameterDefinition();
    }

    public HeatParameterBuilder setName(String name) {
        heatParameterDefinition.setName(name);
        return this;
    }

    public HeatParameterBuilder setType(String type) {
        heatParameterDefinition.setType(type);
        return this;
    }

    public HeatParameterBuilder setCurrentValue(String value) {
        heatParameterDefinition.setCurrentValue(value);
        return this;
    }

    public HeatParameterDefinition build() {
        return heatParameterDefinition;
    }
}
