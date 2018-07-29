package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;

import java.util.ArrayList;

public class CapabilityDefinitionBuilder {

    private CapabilityDefinition capabilityDefinition;

    public CapabilityDefinitionBuilder() {
        capabilityDefinition = new CapabilityDefinition();
        capabilityDefinition.setProperties(new ArrayList<>());
    }

    public CapabilityDefinitionBuilder addProperty(ComponentInstanceProperty property) {
        capabilityDefinition.getProperties().add(property);
        return this;
    }

    public CapabilityDefinitionBuilder setOwnerId(String ownerId) {
        capabilityDefinition.setOwnerId(ownerId);
        return this;
    }

    public CapabilityDefinitionBuilder setOwnerName(String ownerName) {
        capabilityDefinition.setOwnerName(ownerName);
        return this;
    }

    public CapabilityDefinitionBuilder setType(String type) {
        capabilityDefinition.setType(type);
        return this;
    }

    public CapabilityDefinitionBuilder setId(String ownerId) {
        capabilityDefinition.setUniqueId(ownerId);
        return this;
    }

    public CapabilityDefinitionBuilder setName(String name) {
        capabilityDefinition.setName(name);
        return this;
    }


    public CapabilityDefinition build() {
        return capabilityDefinition;
    }
}
