package org.openecomp.sdc.be.tosca.model;

import java.util.HashMap;
import java.util.Map;

public class ToscaAnnotation {
    private String type;

    private Map<String ,Object> properties ;

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    private String description;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public void addProperty(String name, Object property){
        if ( properties == null ){
            properties = new HashMap<>();
        }
        properties.put(name, property);
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }




}
