package org.openecomp.sdc.be.utils;

import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class PropertyDefinitionUtils {

    private PropertyDefinitionUtils() {
    }

    public static <T extends PropertyDataDefinition> List<PropertyDataDefinition> convertListOfProperties(List<T> toConvert) {
        return toConvert.stream().map(PropertyDataDefinition::new).collect(toList());
    }

    public static Map<String, List<PropertyDataDefinition>> resolveGetInputProperties(Map<String, List<PropertyDataDefinition>> properties) {
        if (properties == null) {
            return emptyMap();
        }
        return properties.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> filterGetInputProps(entry.getValue())));
    }

    private static <T extends PropertyDataDefinition> List<PropertyDataDefinition> filterGetInputProps(List<T> propDefinitions) {
        return propDefinitions
                .stream()
                .filter(PropertyDataDefinition::isGetInputProperty)
                .collect(Collectors.toList());
    }
}
