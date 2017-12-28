package org.openecomp.sdc.ci.tests.datatypes;

import java.util.ArrayList;
import java.util.List;

public class PropertyObject {

    private String defaultValue;
    private String name;
    private String parentUniqueId;
    private boolean password;
    private boolean required;
    private List<Schema> Schema;
    private String type;
    private String uniqueId;
    private boolean definition;

    public PropertyObject(String defaultValue, String name, String parentUniqueId, String uniqueId) {
        this.defaultValue = defaultValue;
        this.name = name;
        this.parentUniqueId = parentUniqueId;
        this.uniqueId = uniqueId;
        this.password = false;
        this.required = false;
        this.type = "String";
        this.definition = false;
        this.Schema = new ArrayList<Schema>();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentUniqueId() {
        return parentUniqueId;
    }

    public void setParentUniqueId(String parentUniqueId) {
        this.parentUniqueId = parentUniqueId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}

class Schema {

    private List<Property> property;
}

class Property {}

