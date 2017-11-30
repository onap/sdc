package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResourceBuilder {

    private Resource resource;

    public ResourceBuilder() {
        this.resource = new Resource();
    }

    public ResourceBuilder(Resource resource) {
        this.resource = resource;
    }

    public ResourceBuilder setUniqueId(String id) {
        resource.setUniqueId(id);
        return this;
    }

    public ResourceBuilder setInvariantUUid(String invariantUUid) {
        resource.setInvariantUUID(invariantUUid);
        return this;
    }

    public ResourceBuilder setName(String name) {
        resource.setName(name);
        return this;
    }

    public ResourceBuilder setComponentType(ComponentTypeEnum type) {
        resource.setComponentType(type);
        return this;
    }

    public ResourceBuilder setSystemName(String systemName) {
        resource.setSystemName(systemName);
        return this;
    }

    public ResourceBuilder addComponentInstance(ComponentInstance componentInstance) {
        if (resource.getComponentInstances() == null) {
            resource.setComponentInstances(new ArrayList<>());
        }
        resource.getComponentInstances().add(componentInstance);
        return this;
    }

    public ResourceBuilder addInput(InputDefinition input) {
        if (resource.getInputs() == null) {
            resource.setInputs(new ArrayList<>());
        }
        resource.getInputs().add(input);
        return this;
    }

    public ResourceBuilder addInput(String inputName) {
        InputDefinition inputDefinition = new InputDefinition();
        inputDefinition.setName(inputName);
        inputDefinition.setUniqueId(inputName);
        this.addInput(inputDefinition);
        return this;
    }

    public ResourceBuilder addProperty(PropertyDefinition propertyDefinition) {
        if (resource.getProperties() == null) {
            resource.setProperties(new ArrayList<>());
        }
        resource.getProperties().add(propertyDefinition);
        return this;
    }

    public ResourceBuilder addInstanceProperty(String instanceId, ComponentInstanceProperty prop) {
        if (resource.getComponentInstancesProperties() == null) {
            resource.setComponentInstancesProperties(new HashMap<>());
        }
        resource.getComponentInstancesProperties().computeIfAbsent(instanceId, key -> new ArrayList<>()).add(prop);
        return this;
    }

    public ResourceBuilder addInstanceProperty(String instanceId, String propName) {
        ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty();
        componentInstanceProperty.setName(propName);
        this.addInstanceProperty(instanceId, componentInstanceProperty);
        return this;
    }

    public ResourceBuilder addInstanceInput(String instanceId, ComponentInstanceInput prop) {
        if (resource.getComponentInstancesInputs() == null) {
            resource.setComponentInstancesInputs(new HashMap<>());
        }
        resource.getComponentInstancesInputs().computeIfAbsent(instanceId, key -> new ArrayList<>()).add(prop);
        return this;
    }

    public ResourceBuilder addInstanceInput(String instanceId, String propName) {
        ComponentInstanceInput componentInstanceInput = new ComponentInstanceInput();
        componentInstanceInput.setName(propName);
        this.addInstanceInput(instanceId, componentInstanceInput);
        return this;
    }

    public ResourceBuilder addRelationship(RequirementCapabilityRelDef requirementCapabilityRelDef) {
        if (resource.getComponentInstancesRelations() == null) {
            resource.setComponentInstancesRelations(new ArrayList<>());
        }
        resource.getComponentInstancesRelations().add(requirementCapabilityRelDef);
        return this;
    }


    public Resource build() {
        return resource;
    }
}
