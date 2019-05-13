/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.be.components.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;

import static org.openecomp.sdc.be.components.property.GetInputUtils.isGetInputValueForInput;

public class PropertiesUtils {

    private PropertiesUtils() {
        //Hiding implicit default constructor
    }

    public static List<PropertyDefinition> getProperties(Component service) {
        List<PropertyDefinition> properties = service.getProperties();
        if (properties == null) {
            properties = new ArrayList<>();
        }
        Set<PropertyDefinition> serviceProperties = new HashSet<>(properties);
        if (service.getInputs() != null) {
            Set<PropertyDefinition> inputs = service.getInputs().stream().map(PropertyDefinition::new)
                                                    .collect(Collectors.toSet());
            serviceProperties.addAll(inputs);
        }
        serviceProperties =
                serviceProperties.stream().filter(distinctByKey(PropertyDefinition::getName)).collect(Collectors.toSet());
        return new ArrayList<>(serviceProperties);
    }

    public static Optional<ComponentInstanceProperty> isCapabilityProperty(String propertyUniqueId,
                                               Component containerComponent) {

        Optional<List<ComponentInstanceProperty>> capPropertiesOptional = getCapProperties(containerComponent);

        if(capPropertiesOptional.isPresent()) {
            return capPropertiesOptional.get().stream().filter(propertyDefinition ->
                    propertyDefinition.getUniqueId().equals(propertyUniqueId)).findAny();
        } else {
            return Optional.empty();
        }
    }

    private static Optional<List<ComponentInstanceProperty>> getCapProperties(Component containerComponent) {
        Map<String, List<CapabilityDefinition>> componentCapabilities = containerComponent.getCapabilities();
        if(MapUtils.isEmpty(componentCapabilities)){
            return Optional.empty();
        }
        List<CapabilityDefinition> capabilityDefinitionList = componentCapabilities.values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(capabilityDefinitionList)){
            return Optional.empty();
        }
        List<ComponentInstanceProperty> allComponentInstanceCapProperties= new ArrayList<>();
        capabilityDefinitionList.stream().filter(capabilityDefinition -> CollectionUtils.isNotEmpty(capabilityDefinition
                .getProperties())).collect(Collectors.toList()).forEach(capabilityDefinition ->
                allComponentInstanceCapProperties.addAll(capabilityDefinition.getProperties()));
        return Optional.of(allComponentInstanceCapProperties);
    }

    public static Optional<CapabilityDefinition> getPropertyCapabilityOfChildInstance(String propertyParentUniqueId,
                                                                                      Map<String, List<CapabilityDefinition>>
                                                                               componentCapabilities) {
        if(MapUtils.isEmpty(componentCapabilities)){
            return Optional.empty();
        }
        List<CapabilityDefinition> capabilityDefinitionList = componentCapabilities.values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(capabilityDefinitionList)){
            return Optional.empty();
        }
        return capabilityDefinitionList.stream()
                .filter(capabilityDefinition -> capabilityDefinition.getUniqueId().equals(propertyParentUniqueId) &&
                        capabilityDefinition.getPath().size() == 1)
                .findAny();
    }

    public static Optional<CapabilityDefinition> getPropertyCapabilityFromAllCapProps(String propertyParentUniqueId,
                                                                                      List<CapabilityDefinition>
                                                                                   capabilityDefinitionList) {
        return capabilityDefinitionList.stream()
                .filter(capabilityDefinition -> capabilityDefinition.getUniqueId().equals(propertyParentUniqueId))
                .findAny();
    }

    public static boolean isNodeProperty(String propertyName, List<PropertyDefinition> properties) {

        return !CollectionUtils.isEmpty(properties) && properties.stream().anyMatch(property -> property.getName()
                .equals(propertyName));
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }

    public static Optional<ComponentInstanceProperty> getPropertyByInputId(Component component, String inputId) {
        List<InputDefinition> componentInputs = component.getInputs();
        if(CollectionUtils.isEmpty(componentInputs)) {
            return Optional.empty();
        }
        Optional<InputDefinition> inputDefinition = componentInputs.stream().filter(cip -> cip.getUniqueId()
                .equals(inputId)).findFirst();
        if(!inputDefinition.isPresent()) {
            return Optional.empty();
        }
        Optional<List<ComponentInstanceProperty>> capProperties = getCapProperties(component);
        if(!capProperties.isPresent()) {
            return Optional.empty();
        }

        return capProperties.get().stream().filter(capProp -> CollectionUtils.isNotEmpty(capProp.getGetInputValues()) &&
                capProp.getGetInputValues().stream().anyMatch(capPropInp -> capPropInp.getInputId().equals(inputId)) &&
                capProp.getUniqueId().equals(inputDefinition.get().getPropertyId())).findAny();
    }

    public static List<ComponentInstanceProperty> getCapabilityProperty(ComponentInstanceProperty capabilityProperty,
                                                                  String inputId) {
        List<ComponentInstanceProperty> resList = new ArrayList<>();
        List<GetInputValueDataDefinition> inputsValues = capabilityProperty.getGetInputValues();
        if (CollectionUtils.isNotEmpty(inputsValues) &&  inputsValues.stream().anyMatch(inputData ->
                isGetInputValueForInput(inputData, inputId))) {
                resList.add(capabilityProperty);
        }
        return resList;
    }

    public static boolean isNodeServiceProxy(Component component) {
        if (component.getComponentType().equals(ComponentTypeEnum.SERVICE)) {
            return true;
        }
        Resource resource = (Resource) component;
        ResourceTypeEnum resType = resource.getResourceType();
        return resType.equals(ResourceTypeEnum.ServiceProxy);
    }
}
