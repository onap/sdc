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

import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class NodeFilter {

    private List<Map<String, List<Constraint>>> properties;
    private List<Map<String, CapabilityFilter>> capabilities;

    //can't not be removed, in used in snake yaml
    public List<Map<String, CapabilityFilter>> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Map<String, CapabilityFilter>> capabilities) {
        this.capabilities = capabilities;
    }

    //can't not be removed, in used in snake yaml
    public List<Map<String, List<Constraint>>> getProperties() {
        return properties;
    }


    //use this function in order to get node filter properties instead of getProperties function
    public List<Map<String, List<Constraint>>> getNormalizeProperties() {
        return getNormalizeProperties(properties);
    }

    private List<Map<String, List<Constraint>>> getNormalizeProperties(List<Map<String, List<Constraint>>> properties) {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        if (CollectionUtils.isEmpty(properties)) {
            return properties;
        }
        for (Map<String, List<Constraint>> propertyConstraintsEntity : properties) {
            String propertyKey = propertyConstraintsEntity.keySet().iterator().next();
            List<Constraint> constraints = propertyConstraintsEntity.get(propertyKey);
            Iterator<Constraint> iterator = constraints.iterator();
            while (iterator.hasNext()) {
                Constraint constraintObj = iterator.next();
                Constraint constraint = toscaExtensionYamlUtil
                                                .yamlToObject(toscaExtensionYamlUtil.objectToYaml(constraintObj),
                                                        Constraint.class);
                constraints.remove(constraintObj);
                constraints.add(constraint);
            }
        }
        return properties;
    }

    //use this function in order to get node filter capabilities instead of getCapabilities function
    public List<Map<String, CapabilityFilter>> getNormalizeCapabilities() {
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        if (CollectionUtils.isEmpty(capabilities)) {
            return capabilities;
        }
        for (Map<String, CapabilityFilter> capabilityEntry : capabilities) {
            String capabilityKey = capabilityEntry.keySet().iterator().next();
            Object capabilityFilterObj = capabilityEntry.get(capabilityKey);
            CapabilityFilter capabilityFilter = toscaExtensionYamlUtil.yamlToObject(
                    toscaExtensionYamlUtil.objectToYaml(capabilityFilterObj), CapabilityFilter.class);
            capabilityFilter.setProperties(getNormalizeProperties(capabilityFilter.getProperties()));
            capabilityEntry.remove(capabilityKey);
            capabilityEntry.put(capabilityKey, capabilityFilter);
        }
        return capabilities;
    }

    public void setProperties(List<Map<String, List<Constraint>>> properties) {
        this.properties = properties;
    }
}
