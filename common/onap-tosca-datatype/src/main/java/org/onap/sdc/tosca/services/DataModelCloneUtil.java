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

package org.onap.sdc.tosca.services;



import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.onap.sdc.tosca.datatypes.model.AttributeDefinition;
import org.onap.sdc.tosca.datatypes.model.Constraint;
import org.onap.sdc.tosca.datatypes.model.OperationDefinition;
import org.onap.sdc.tosca.datatypes.model.OperationDefinitionTemplate;
import org.onap.sdc.tosca.datatypes.model.OperationDefinitionType;
import org.onap.sdc.tosca.datatypes.model.PropertyDefinition;


import java.util.ArrayList;

public class DataModelCloneUtil {

    private DataModelCloneUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Clone constraints list.
     *
     * @param constraints the constraints
     * @return the list
     */
    public static List<Constraint> cloneConstraints(List<Constraint> constraints) {

        if (constraints == null) {
            return null;
        }
        return constraints.stream().map(Constraint::clone).collect(Collectors.toList());
    }

    /**
     * Clone property definitions map.
     *
     * @param propertyDefinitions the property definitions
     * @return the map
     */
    public static Map<String, PropertyDefinition> clonePropertyDefinitions(
            Map<String, PropertyDefinition> propertyDefinitions) {

        if (propertyDefinitions == null) {
            return null;
        }
        Map<String, PropertyDefinition> clonedProperties = new HashMap<>();
        for (Map.Entry<String,PropertyDefinition> propertyDefinitionEntry : propertyDefinitions.entrySet()) {
            clonedProperties.put(propertyDefinitionEntry.getKey(), propertyDefinitionEntry.getValue().clone());
        }

        return clonedProperties;
    }

    /**
     * Clone attribute definitions map.
     *
     * @param attributeDefinitions the attribute definitions
     * @return the map
     */
    public static Map<String, AttributeDefinition> cloneAttributeDefinitions(
            Map<String, AttributeDefinition> attributeDefinitions) {

        if (attributeDefinitions == null) {
            return null;
        }
        Map<String, AttributeDefinition> clonedAttributeDefinitions = new HashMap<>();
        for (Map.Entry<String, AttributeDefinition> attributeDefinitionEntry : attributeDefinitions.entrySet()) {
            clonedAttributeDefinitions.put(attributeDefinitionEntry.getKey(),
                    attributeDefinitionEntry.getValue().clone());
        }

        return clonedAttributeDefinitions;
    }

    /**
     * Clone valid source types list.
     *
     * @param validSourceTypes the valid source types
     * @return the list
     */
    public static List<String> cloneValidSourceTypes(List<String> validSourceTypes) {
        if (validSourceTypes == null) {
            return null;
        }
        return validSourceTypes.stream().collect(Collectors.toList());
    }

    /**
     * Clone Map of key String and value String .
     *
     * @param stringStringMap the map that will be cloned
     * @return the cloned map
     */
    public static Map<String, String> cloneStringStringMap(Map<String, String> stringStringMap) {
        if (Objects.isNull(stringStringMap)) {
            return null;
        }

        Map<String, String> cloneMap = new HashMap<>();
        for (Map.Entry<String, String> mapEntry : stringStringMap.entrySet()) {
            cloneMap.put(mapEntry.getKey(), mapEntry.getValue());
        }
        return cloneMap;
    }

    /**
     * Clone Map of key String and value PropertyDefinition .
     *
     * @param stringPropertyDefinitionMap the map that will be cloned
     * @return the cloned map
     */
    public static Map<String, PropertyDefinition> cloneStringPropertyDefinitionMap(
            Map<String, PropertyDefinition> stringPropertyDefinitionMap) {
        if (Objects.isNull(stringPropertyDefinitionMap)) {
            return null;
        }

        Map<String, PropertyDefinition> cloneMap = new HashMap<>();
        ToscaExtensionYamlUtil toscaExtYamlUtil = new ToscaExtensionYamlUtil();
        for (Map.Entry<String, PropertyDefinition> mapEntry : stringPropertyDefinitionMap.entrySet()) {
            PropertyDefinition propertyDefinition = toscaExtYamlUtil.yamlToObject(
                    toscaExtYamlUtil.objectToYaml(mapEntry.getValue()), PropertyDefinition.class);
            cloneMap.put(mapEntry.getKey(), propertyDefinition.clone());
        }
        return cloneMap;
    }

    /**
     * Clone Map of key String and value OperationDefinition .
     *
     * @param stringOperationDefinitionMap the map that will be cloned
     * @return the cloned map
     */
    public static Map<String, OperationDefinition> cloneStringOperationDefinitionMap(
            Map<String, OperationDefinition> stringOperationDefinitionMap) {
        if (Objects.isNull(stringOperationDefinitionMap)) {
            return null;
        }

        Map<String, OperationDefinition> cloneMap = new HashMap<>();
        for (Map.Entry<String, OperationDefinition> mapEntry : stringOperationDefinitionMap.entrySet()) {
            cloneMap.put(mapEntry.getKey(), Objects.isNull(mapEntry.getValue()) ? null : mapEntry.getValue().clone());
        }
        return cloneMap;
    }

    /**
     * Clone Map of key String and value OperationDefinitionTemplate .
     *
     * @param stringOperationTemplateMap the map that will be cloned
     * @return the cloned map
     */
    public static Map<String, OperationDefinitionTemplate> cloneStringOperationTemplateMap(
            Map<String, OperationDefinitionTemplate> stringOperationTemplateMap) {
        if (Objects.isNull(stringOperationTemplateMap)) {
            return null;
        }

        Map<String, OperationDefinitionTemplate> cloneMap = new HashMap<>();
        for (Map.Entry<String, OperationDefinitionTemplate> mapEntry : stringOperationTemplateMap.entrySet()) {
            cloneMap.put(mapEntry.getKey(), Objects.isNull(mapEntry.getValue()) ? null : mapEntry.getValue().clone());
        }
        return cloneMap;
    }

    /**
     * Clone Map of key String and value OperationDefinitionType .
     *
     * @param stringOperationDefinitionTypeMap the map that will be cloned
     * @return the cloned map
     */
    public static Map<String, OperationDefinitionType> cloneStringOperationDefinitionTypeMap(
            Map<String, OperationDefinitionType> stringOperationDefinitionTypeMap) {
        if (Objects.isNull(stringOperationDefinitionTypeMap)) {
            return null;
        }

        Map<String, OperationDefinitionType> cloneMap = new HashMap<>();
        for (Map.Entry<String, OperationDefinitionType> mapEntry : stringOperationDefinitionTypeMap.entrySet()) {
            cloneMap.put(mapEntry.getKey(), Objects.isNull(mapEntry.getValue()) ? null : mapEntry.getValue().clone());
        }
        return cloneMap;
    }

    /**
     * Clone Map of key String and value Object .
     *
     * @param stringObjectMap the map that will be cloned
     * @return the cloned map
     */
    public static Map<String, Object> cloneStringObjectMap(Map<String, Object> stringObjectMap) {
        if (Objects.isNull(stringObjectMap)) {
            return null;
        }

        Map<String, Object> cloneMap = new HashMap<>();
        for (Map.Entry<String, Object> mapEntry : stringObjectMap.entrySet()) {
            YamlUtil yamlUtil = new YamlUtil();
            if (mapEntry.getValue() instanceof Map) {
                Map cloneObj = yamlUtil.yamlToObject(yamlUtil.objectToYaml(mapEntry.getValue()), Map.class);
                cloneMap.put(mapEntry.getKey(), cloneObj);
            } else if (mapEntry.getValue() instanceof List) {
                List cloneObj = yamlUtil.yamlToObject(yamlUtil.objectToYaml(mapEntry.getValue()), List.class);
                cloneMap.put(mapEntry.getKey(), cloneObj);
            } else {
                cloneMap.put(mapEntry.getKey(), mapEntry.getValue());
            }
        }
        return cloneMap;
    }

    /**
     * Clone List of String.
     *
     * @param listString the list that will be cloned
     * @return the cloned list
     */
    public static List<String> cloneListString(List<String> listString) {
        if (Objects.isNull(listString)) {
            return null;
        }
        return new ArrayList<>(listString);
    }


}
