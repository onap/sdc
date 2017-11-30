package org.openecomp.sdc.be.components.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;

public class ObjectGenerator {

    public static Resource buildResourceWithInputs(String ... inputNames) {
        Resource resource = buildBasicResource();
        resource.setInputs(ObjectGenerator.buildInputs(inputNames));
        return resource;
    }

    public static Resource buildResourceWithProperties(String ... propertiesNames) {
        ResourceBuilder resourceBuilder = new ResourceBuilder();
        resourceBuilder.setUniqueId("id");
        for (String propertyName : propertiesNames) {
            PropertyDefinition propertyDefinition = new PropertyDefinition();
            propertyDefinition.setName(propertyName);
            resourceBuilder.addProperty(propertyDefinition);
        }
        return resourceBuilder.build();
    }

    public static List<ComponentInstanceProperty> buildInstanceProperties(String ... propertiesNames) {
        return Stream.of(propertiesNames).map(name ->  {
            ComponentInstanceProperty instProp = new ComponentInstanceProperty();
            instProp.setName(name);
            return instProp;
        }).collect(Collectors.toList());
    }

    public static List<ComponentInstanceInput> buildInstanceInputs(String ... inputsNames) {
        return Stream.of(inputsNames).map(name ->  {
            ComponentInstanceInput instProp = new ComponentInstanceInput();
            instProp.setName(name);
            return instProp;
        }).collect(Collectors.toList());
    }

    public static List<InputDefinition> buildInputs(String ... inputNames) {
        List<InputDefinition> inputs = new ArrayList<>();
        for (String inputName : inputNames) {
            InputDefinition inputDefinition = new InputDefinition();
            inputDefinition.setName(inputName);
            inputs.add(inputDefinition);
        }
        return inputs;
    }

    public static Resource buildResourceWithComponentInstance(String ... instanceNames) {
        List<ComponentInstance> instances = new ArrayList<>();
        for (String instanceName : instanceNames) {
            ComponentInstance componentInstance = new ComponentInstanceBuilder().setName(instanceName).setComponentUid(instanceName).build();
            instances.add(componentInstance);
        }
        return buildResourceWithComponentInstances(instances);
    }

    public static Resource buildResourceWithComponentInstances(List<ComponentInstance> instances) {
        Resource resource = buildBasicResource();
        resource.setComponentInstances(instances);
        return resource;
    }

    public static Resource buildResourceWithComponentInstances(ComponentInstance ... instances) {
        return buildResourceWithComponentInstances(Arrays.asList(instances));
    }

    public static Resource buildResourceWithRelationships(RequirementCapabilityRelDef ... relations) {
        Resource resource = buildBasicResource();
        ResourceBuilder resourceBuilder = new ResourceBuilder(resource);
        for (RequirementCapabilityRelDef relation : relations) {
            resourceBuilder.addRelationship(relation);
        }
        return resourceBuilder.build();
    }

    public static Resource buildBasicResource() {
        Resource resource = new Resource();
        resource.setUniqueId("id");
        return resource;
    }

}
