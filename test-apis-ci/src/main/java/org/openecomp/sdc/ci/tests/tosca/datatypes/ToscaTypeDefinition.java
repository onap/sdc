package org.openecomp.sdc.ci.tests.tosca.datatypes;

import java.util.HashMap;
import java.util.Map;

public class ToscaTypeDefinition {

    private String description;
    private Map<String, Object> properties = new HashMap<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
