package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;

import java.util.ArrayList;
import java.util.List;

public class AnnotationBuilder {

    private Annotation annotation;

    private AnnotationBuilder() {
        annotation = new Annotation();
    }

    public static AnnotationBuilder create() {
        return new AnnotationBuilder();
    }

    public AnnotationBuilder setType(String type) {
        annotation.setType(type);
        return this;
    }

    public AnnotationBuilder setName(String name) {
        annotation.setName(name);
        return this;
    }

    public AnnotationBuilder addProperty(String name) {
        PropertyDefinition prop = new PropertyDataDefinitionBuilder()
                .setName(name)
                .build();
        List<PropertyDataDefinition> annotationProps = getAnnotationProps();
        annotationProps.add(prop);
        return this;
    }

    public Annotation build() {
        return annotation;
    }

    private List<PropertyDataDefinition> getAnnotationProps() {
        if (annotation.getProperties() == null) {
            annotation.setProperties(new ArrayList<>());
        }
        return annotation.getProperties();
    }

}
