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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.onap.sdc.tosca.datatypes.model.CapabilityFilter;
import org.onap.sdc.tosca.datatypes.model.Constraint;

public class DataModelNormalizeUtil {

    private DataModelNormalizeUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static List<Map<String, CapabilityFilter>> getNormalizeCapabilitiesFilter(List<Map<String, CapabilityFilter>> capabilitiesFilter) {
        if (CollectionUtils.isEmpty(capabilitiesFilter)) {
            return capabilitiesFilter;
        }
        List<Map<String, CapabilityFilter>> normalizeCapabilities = new ArrayList<>();
        for (Map<String, CapabilityFilter> capabilityFilterEntry : capabilitiesFilter) {
            Map<String, CapabilityFilter> normalizeCapabilityEntry = getNormalizeCapabilityFilterEntry(capabilityFilterEntry);
            normalizeCapabilities.add(normalizeCapabilityEntry);
        }
        return normalizeCapabilities;
    }

    private static Map<String, CapabilityFilter> getNormalizeCapabilityFilterEntry(Map<String, CapabilityFilter> capabilityFilterEntry) {
        Map<String, CapabilityFilter> normalizeCapabilityEntry = new HashMap<>();
        String capabilityKey = capabilityFilterEntry.keySet().iterator().next(); //only one entry exist in the map
        Object capabilityFilterObj = capabilityFilterEntry.get(capabilityKey);
        CapabilityFilter normalizeCapabilityFilter = getNormalizeCapabilityFilter(capabilityFilterObj);
        normalizeCapabilityEntry.put(capabilityKey, normalizeCapabilityFilter);
        return normalizeCapabilityEntry;
    }

    private static CapabilityFilter getNormalizeCapabilityFilter(Object capabilityFilterObj) {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        CapabilityFilter capabilityFilter = toscaExtensionYamlUtil
            .yamlToObject(toscaExtensionYamlUtil.objectToYaml(capabilityFilterObj), CapabilityFilter.class);
        capabilityFilter.setProperties(getNormalizePropertiesFilter(capabilityFilter.getProperties()));
        return capabilityFilter;
    }

    public static List<Map<String, List<Constraint>>> getNormalizePropertiesFilter(List<Map<String, List<Constraint>>> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return properties;
        }
        List<Map<String, List<Constraint>>> normalizeProperties = new ArrayList<>();
        for (Map<String, List<Constraint>> propertyFilterEntry : properties) {
            Map<String, List<Constraint>> normalizePropertyEntry = getNormalizePropertyFilterEntry(propertyFilterEntry);
            normalizeProperties.add(normalizePropertyEntry);
        }
        return normalizeProperties;
    }

    private static Map<String, List<Constraint>> getNormalizePropertyFilterEntry(Map<String, List<Constraint>> propertyFilterEntry) {
        Map<String, List<Constraint>> normalizePropertyEntry = new HashMap<>();
        String propertyKey = propertyFilterEntry.keySet().iterator().next();  //only one entry exist in the map
        List<Constraint> constraints = propertyFilterEntry.get(propertyKey);
        List<Constraint> normalizeConstraints = getNormalizeConstrains(constraints);
        normalizePropertyEntry.put(propertyKey, normalizeConstraints);
        return normalizePropertyEntry;
    }

    private static List<Constraint> getNormalizeConstrains(List<Constraint> constraints) {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        List<Constraint> normalizeConstraints = new ArrayList<>();
        for (Object constraintObj : constraints) {
            Constraint normalizeConstraint = toscaExtensionYamlUtil
                .yamlToObject(toscaExtensionYamlUtil.objectToYaml(constraintObj), Constraint.class);
            normalizeConstraints.add(normalizeConstraint);
        }
        return normalizeConstraints;
    }
}
