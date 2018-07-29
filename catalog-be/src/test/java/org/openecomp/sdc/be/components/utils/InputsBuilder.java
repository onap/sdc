package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.datatypes.elements.Annotation;
import org.openecomp.sdc.be.model.InputDefinition;

import java.util.ArrayList;
import java.util.List;

public class InputsBuilder {

    private InputDefinition input;

    private InputsBuilder() {
        this.input = new InputDefinition();
    }

    public static InputsBuilder create() {
        return new InputsBuilder();
    }

    public InputsBuilder setName(String name) {
        input.setName(name);
        return this;
    }

    public InputsBuilder setPropertyId(String propertyId) {
        input.setPropertyId(propertyId);
        return this;
    }

    public InputsBuilder addAnnotation(Annotation annotation) {
        List<Annotation> annotations = getAnnotations();
        annotations.add(annotation);
        return this;
    }

    private List<Annotation> getAnnotations() {
        if (input.getAnnotations() == null) {
            input.setAnnotations(new ArrayList<>());
        }
        return input.getAnnotations();
    }

    public InputDefinition build() {
        return input;
    }

}
