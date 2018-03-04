package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;

import java.util.ArrayList;

public abstract class PropertyDataDefinitionAbstractBuilder<B extends PropertyDataDefinition, T extends PropertyDataDefinitionAbstractBuilder<B, T>> {

    B propertyDefinition;

    protected abstract PropertyDataDefinitionAbstractBuilder<B, T> self();

    abstract B propertyDefinition();

    PropertyDataDefinitionAbstractBuilder() {
        propertyDefinition = propertyDefinition();
    }

    public PropertyDataDefinitionAbstractBuilder<B, T> setUniqueId(String id) {
        this.propertyDefinition.setUniqueId(id);
        return self();
    }

    public PropertyDataDefinitionAbstractBuilder<B, T> setName(String name) {
        this.propertyDefinition.setName(name);
        return self();
    }

    public PropertyDataDefinitionAbstractBuilder<B, T> setValue(String value) {
        this.propertyDefinition.setValue(value);
        return self();
    }

    public PropertyDataDefinitionAbstractBuilder<B, T> setDefaultValue(String value) {
        this.propertyDefinition.setDefaultValue(value);
        return self();
    }

    public PropertyDataDefinitionAbstractBuilder<B, T> setType(String type) {
        this.propertyDefinition.setType(type);
        return self();
    }

    public PropertyDataDefinitionAbstractBuilder<B, T> setOwnerId(String ownerId) {
        this.propertyDefinition.setOwnerId(ownerId);
        return self();
    }

    public PropertyDataDefinitionAbstractBuilder<B, T> setSchemaType(String type) {
        if (propertyDefinition.getSchema() == null) {
            propertyDefinition.setSchema(new SchemaDefinition());
        }
        if (propertyDefinition.getSchema().getProperty() == null) {
            propertyDefinition.getSchema().setProperty(new PropertyDataDefinition());
        }
        propertyDefinition.getSchema().getProperty().setType(type);
        return self();
    }

    public PropertyDataDefinitionAbstractBuilder<B, T> addGetInputValue(String inputName) {
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setInputName(inputName);
        getInputValueDataDefinition.setInputId(inputName);
        if (propertyDefinition.getGetInputValues() == null) {
            propertyDefinition.setGetInputValues(new ArrayList<>());
        }
        propertyDefinition.getGetInputValues().add(getInputValueDataDefinition);
        return self();
    }

    public PropertyDataDefinitionAbstractBuilder<B, T> setIsRequired(boolean required) {
        this.propertyDefinition.setRequired(required);
        return self();
    }

    public PropertyDataDefinitionAbstractBuilder<B, T> setDescription(String description) {
        this.propertyDefinition.setDescription(description);
        return self();
    }

    public PropertyDataDefinitionAbstractBuilder<B, T> setIsPassword(boolean isPassword) {
        this.propertyDefinition.setRequired(isPassword);
        return self();
    }

    public PropertyDataDefinitionAbstractBuilder<B, T> setStatus(String status) {
        this.propertyDefinition.setStatus(status);
        return self();
    }

    public abstract B build();
}
