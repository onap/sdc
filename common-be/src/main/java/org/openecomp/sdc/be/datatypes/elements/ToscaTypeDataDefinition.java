package org.openecomp.sdc.be.datatypes.elements;

import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

public class ToscaTypeDataDefinition extends ToscaDataDefinition {

    private String name;
    private String icon;
    private String type;

    ToscaTypeDataDefinition() {
    }

    ToscaTypeDataDefinition(ToscaTypeDataDefinition other) {
        this.name = other.getName();
        this.icon = other.icon;
        this.type = other.type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
