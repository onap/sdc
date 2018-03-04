package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.model.ComponentInstanceProperty;

public class ComponentInstancePropertyBuilder extends PropertyDataDefinitionAbstractBuilder<ComponentInstanceProperty, ComponentInstancePropertyBuilder> {

    @Override
    protected PropertyDataDefinitionAbstractBuilder<ComponentInstanceProperty, ComponentInstancePropertyBuilder> self() {
        return this;
    }

    @Override
    ComponentInstanceProperty propertyDefinition() {
        return new ComponentInstanceProperty();
    }

    @Override
    public ComponentInstanceProperty build() {
        return propertyDefinition;
    }

}
