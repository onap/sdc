package org.openecomp.sdc.be.components.utils;

import java.util.List;

import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;

public class PropertyDataDefinitionBuilder extends PropertyDataDefinitionAbstractBuilder<PropertyDefinition, PropertyDataDefinitionBuilder> {

    protected PropertyDataDefinitionBuilder self() {
        return this;
    }

    @Override
    PropertyDefinition propertyDefinition() {
        return new PropertyDefinition();
    }

    @Override
    public PropertyDefinition build() {
        return propertyDefinition;
    }

    public PropertyDataDefinitionBuilder setConstraints(List<PropertyConstraint> constraints) {
        propertyDefinition.setConstraints(constraints);
        return self();
    }

}
