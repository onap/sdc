package org.openecomp.sdc.be.model;

import org.openecomp.sdc.be.datatypes.elements.AnnotationTypeDataDefinition;
import org.openecomp.sdc.be.model.utils.TypeCompareUtils;

import java.util.List;
import java.util.Objects;

public class AnnotationTypeDefinition extends AnnotationTypeDataDefinition {


    public AnnotationTypeDefinition() {
        super();
    }

    public AnnotationTypeDefinition(AnnotationTypeDataDefinition annotationTypeDataDefinition) {
        super(annotationTypeDataDefinition);
    }

    protected List<PropertyDefinition> properties;

    public List<PropertyDefinition> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyDefinition> properties) {
        this.properties = properties;
    }

    /**
     * This method compares definition properties and ignores products such as
     * actual graph ids that were already assigned
     */
    public boolean isSameDefinition(AnnotationTypeDefinition other) {
        if (this == other) return true;
        if (other == null) return false;

        return Objects.equals(type, other.type) &&
               Objects.equals(description, other.description) &&
               TypeCompareUtils.propertiesEquals(properties, other.properties);
    }
}
