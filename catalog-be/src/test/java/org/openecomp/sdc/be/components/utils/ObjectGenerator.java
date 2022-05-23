/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceAttribute;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.HeatParameterDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.Resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static List<ComponentInstanceAttribute> buildInstanceAttributes(String ... attributesNames) {
        return Stream.of(attributesNames).map(name ->  {
            ComponentInstanceAttribute instAttribute = new ComponentInstanceAttribute();
            instAttribute.setName(name);
            return instAttribute;
        }).collect(Collectors.toList());
    }

    public static List<OutputDefinition> buildOutputs(String ... outputNames) {
        List<OutputDefinition> outputs = new ArrayList<>();
        for (String outputName : outputNames) {
            OutputDefinition outputDefinition = new OutputDefinition();
            outputDefinition.setName(outputName);
            outputs.add(outputDefinition);
        }
        return outputs;
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

    public static HeatParameterDefinition buildHeatParam(String defaultVal, String currValue) {
        return new HeatParameterBuilder().setDefaultValue(defaultVal).setCurrentValue(currValue).build();
    }

}
