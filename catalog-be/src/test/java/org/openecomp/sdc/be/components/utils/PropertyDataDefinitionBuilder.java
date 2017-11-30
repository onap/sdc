package org.openecomp.sdc.be.components.utils;

import java.util.ArrayList;

import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;

public class PropertyDataDefinitionBuilder {

    private PropertyDataDefinition propertyDataDefinition;

    public PropertyDataDefinitionBuilder() {
        propertyDataDefinition = new PropertyDataDefinition();
    }

    public PropertyDataDefinitionBuilder setUniqueId(String id) {
        this.propertyDataDefinition.setUniqueId(id);
        return this;
    }

    public PropertyDataDefinitionBuilder setName(String name) {
        this.propertyDataDefinition.setName(name);
        return this;
    }

    public PropertyDataDefinitionBuilder setValue(String value) {
        this.propertyDataDefinition.setValue(value);
        return this;
    }

    public PropertyDataDefinitionBuilder setType(String type) {
        this.propertyDataDefinition.setType(type);
        return this;
    }

    public PropertyDataDefinitionBuilder setSchemaType(String type) {
        if (propertyDataDefinition.getSchema() == null) {
            propertyDataDefinition.setSchema(new SchemaDefinition());
        }
        if (propertyDataDefinition.getSchema().getProperty() == null) {
            propertyDataDefinition.getSchema().setProperty(new PropertyDataDefinition());
        }
        propertyDataDefinition.getSchema().getProperty().setType(type);
        return this;
    }

    public PropertyDataDefinitionBuilder addGetInputValue(String inputName) {
        GetInputValueDataDefinition getInputValueDataDefinition = new GetInputValueDataDefinition();
        getInputValueDataDefinition.setInputName(inputName);
        getInputValueDataDefinition.setInputId(inputName);
        if (propertyDataDefinition.getGetInputValues() == null) {
            propertyDataDefinition.setGetInputValues(new ArrayList<>());
        }
        propertyDataDefinition.getGetInputValues().add(getInputValueDataDefinition);
        return this;
    }

    public PropertyDataDefinition build() {
        return propertyDataDefinition;
    }
}
