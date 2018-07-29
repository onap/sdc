package org.openecomp.sdc.be.components.utils;

import org.assertj.core.api.Condition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class Conditions {

    public static Condition<List<PropertyDataDefinition>> hasPropertiesWithNames(String ... expectedPropsName) {
        return new Condition<List<PropertyDataDefinition>>(){
            public boolean matches(List<PropertyDataDefinition> props) {
                List<String> propsNames = props.stream().map(PropertyDataDefinition::getName).collect(Collectors.toList());
                return propsNames.containsAll(asList(expectedPropsName));
            }
        };
    }

}
