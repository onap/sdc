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

package org.onap.sdc.tosca.datatypes.model;

import org.apache.commons.collections4.CollectionUtils;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NodeFilter {

    private List<Map<String, List<Constraint>>> properties;
    private List<Map<String, CapabilityFilter>> capabilities;

    public List<Map<String, CapabilityFilter>> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Map<String, CapabilityFilter>> capabilities) {
        this.capabilities = getNormalizeCapabilities(capabilities);
    }

    public void setProperties(List<Map<String, List<Constraint>>> properties) {
        this.properties = getNormalizeProperties(properties);
    }

    public List<Map<String, List<Constraint>>> getProperties() {
        return properties;
    }

    private List<Map<String, List<Constraint>>> getNormalizeProperties(List<Map<String, List<Constraint>>> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return properties;
        }
        List<Map<String, List<Constraint>>> normalizeProperties = new ArrayList<>();
        for (Map<String, List<Constraint>> propertyEntry : properties) {
            Map<String, List<Constraint>> normalizePropertyEntry = getNormalizePropertyEntry(propertyEntry);
            normalizeProperties.add(normalizePropertyEntry);
        }
        return normalizeProperties;

    }

    private Map<String, List<Constraint>> getNormalizePropertyEntry(Map<String, List<Constraint>> propertyEntry) {
        Map<String, List<Constraint>> normalizePropertyEntry = new HashMap<>();
        String propertyKey = propertyEntry.keySet().iterator().next();  //only one entry exist in the map
        List<Constraint> constraints = propertyEntry.get(propertyKey);
        List<Constraint> normalizeConstraints = getNormalizeConstrains(constraints);
        normalizePropertyEntry.put(propertyKey, normalizeConstraints);
        return normalizePropertyEntry;
    }

    private List<Constraint> getNormalizeConstrains(List<Constraint> constraints) {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        List<Constraint> normalizeConstraints = new ArrayList<>();
        for (Object constraintObj : constraints) {
            Constraint normalizeConstraint = toscaExtensionYamlUtil
                                                     .yamlToObject(toscaExtensionYamlUtil.objectToYaml(constraintObj),
                                                             Constraint.class);
            normalizeConstraints.add(normalizeConstraint);
        }
        return normalizeConstraints;
    }

    public List<Map<String, CapabilityFilter>> getNormalizeCapabilities(List<Map<String, CapabilityFilter>> capabilities) {
        if (CollectionUtils.isEmpty(capabilities)) {
            return capabilities;
        }
        List<Map<String, CapabilityFilter>> normalizeCapabilities = new ArrayList<>();
        for (Map<String, CapabilityFilter> capabilityEntry : capabilities) {
            Map<String, CapabilityFilter> normalizeCapabilityEntry = getNormalizeCapabilityEntry(capabilityEntry);
            normalizeCapabilities.add(normalizeCapabilityEntry);
        }
        return normalizeCapabilities;
    }

    private Map<String, CapabilityFilter> getNormalizeCapabilityEntry(Map<String, CapabilityFilter> capabilityEntry) {
        Map<String, CapabilityFilter> normalizeCapabilityEntry = new HashMap<>();
        String capabilityKey = capabilityEntry.keySet().iterator().next(); //only one entry exist in the map
        Object capabilityFilterObj = capabilityEntry.get(capabilityKey);
        CapabilityFilter normalizeCapabilityFilter = getNormalizeCapabilityFilter(capabilityFilterObj);
        normalizeCapabilityEntry.put(capabilityKey, normalizeCapabilityFilter);
        return normalizeCapabilityEntry;
    }


    private CapabilityFilter getNormalizeCapabilityFilter(Object capabilityFilterObj) {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        CapabilityFilter capabilityFilter = toscaExtensionYamlUtil.yamlToObject(
                toscaExtensionYamlUtil.objectToYaml(capabilityFilterObj), CapabilityFilter.class);
        capabilityFilter.setProperties(getNormalizeProperties(capabilityFilter.getProperties()));

        return capabilityFilter;
    }
}
