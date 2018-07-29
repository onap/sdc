package org.openecomp.sdc.be.datatypes.elements;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Annotation {
    private String name;
    private String type;
    private String description;
    private List<PropertyDataDefinition> properties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<PropertyDataDefinition> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyDataDefinition> properties) {
        this.properties = properties;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static void setAnnotationsName(Map<String, Annotation> annotations) {
        annotations.forEach((name, annotation) -> annotation.setName(name));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Annotation that = (Annotation) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
