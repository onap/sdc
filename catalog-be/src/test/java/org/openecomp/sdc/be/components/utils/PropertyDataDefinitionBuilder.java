package org.openecomp.sdc.be.components.utils;

import java.util.ArrayList;

import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;

public class PropertyDataDefinitionBuilder {

    private PropertyDefinition propertyDefinition;

    public PropertyDataDefinitionBuilder() {
        propertyDefinition = new PropertyDefinition();
    }

    public PropertyDataDefinitionBuilder setUniqueId(String id) {
        this.propertyDefinition.setUniqueId(id);
        return this;
    }

    public PropertyDataDefinitionBuilder setName(String name) {
        this.propertyDefinition.setName(name);
        return this;
    }

    public PropertyDataDefinitionBuilder setValue(String value) {
        this.propertyDefinition.setValue(value);
        return this;
    }

    public PropertyDataDefinitionBuilder setDefaultValue(String value) {
        this.propertyDefinition.setDefaultValue(value);
        return this;
    }

    public PropertyDataDefinitionBuilder setType(String type) {
        this.propertyDefinition.setType(type);
        return this;
    }

    public PropertyDataDefinitionBuilder setSchemaType(String type) {
        if (propertyDefinition.getSchema() == null) {
            propertyDefinition.setSchema(new SchemaDefinition());
        }
        if (propertyDefinition.getSchema().getProperty() == null) {
            propertyDefinition.getSchema().setProperty(new PropertyDataDefinition());
        }
        propertyDefinition.getSchema().getProperty().setType(type);
        return this;
    }

    public PropertyDataDefinitionBuilder addGetInputValue(String inputName) {
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setInputName(inputName);
        getInputValueDataDefinition.setInputId(inputName);
        if (propertyDefinition.getGetInputValues() == null) {
            propertyDefinition.setGetInputValues(new ArrayList<>());
        }
        propertyDefinition.getGetInputValues().add(getInputValueDataDefinition);
        return this;
    }

    public PropertyDefinition build() {
        return propertyDefinition;
    }
}
