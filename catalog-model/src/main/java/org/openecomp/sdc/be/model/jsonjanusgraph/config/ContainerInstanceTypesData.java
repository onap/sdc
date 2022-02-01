/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.model.jsonjanusgraph.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ContainerInstanceTypesData {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerInstanceTypesData.class);
    private static final String WILDCARD = "*";
    private final ConfigurationManager configurationManager;

    public ContainerInstanceTypesData() {
        this.configurationManager = ConfigurationManager.getConfigurationManager();
    }

    /**
     * Checks if a resource instance type is allowed in a Service component.
     *
     * @param resourceTypeToCheck the resource instance type that will be added to the container component
     * @return {@code true} if the resource instance is allowed, {@code false} otherwise
     */
    public boolean isAllowedForServiceComponent(final ResourceTypeEnum resourceTypeToCheck, final String modelName) {
        final List<String> allowedResourceInstanceTypeList = getServiceAllowedList(modelName);
        if (CollectionUtils.isEmpty(allowedResourceInstanceTypeList)) {
            return false;
        }
        return allowedResourceInstanceTypeList.contains(resourceTypeToCheck.getValue());
    }

    /**
     * Checks if a resource instance type is allowed for a resource component.
     *
     * @param containerComponentResourceType the container component type that the instance will be added
     * @param resourceToCheck                the resource instance type that will be added to the container component
     * @return {@code true} if the resource instance is allowed in the container component, {@code false} otherwise
     */
    public boolean isAllowedForResourceComponent(final ResourceTypeEnum containerComponentResourceType, final ResourceTypeEnum resourceToCheck) {
        final List<String> allowedResourceInstanceTypeList = getComponentAllowedList(ComponentTypeEnum.RESOURCE, containerComponentResourceType);
        if (CollectionUtils.isEmpty(allowedResourceInstanceTypeList)) {
            return false;
        }
        return allowedResourceInstanceTypeList.contains(resourceToCheck.getValue());
    }
    
    /**
     * Gets the list of allowed component instances for a service of the given model.
     *
     * @param model the model
     * @return the list of allowed component instances
     */
    public List<String> getServiceAllowedList(final String modelName) {
        List<String> allowedInstanceResourceType = getComponentAllowedList(ComponentTypeEnum.SERVICE, null);
        if (modelName == null || modelName.isEmpty() || modelName.equals("SDC AID")){
            allowedInstanceResourceType.remove("VFC");
        }
        return allowedInstanceResourceType;
    }

    /**
     * Gets the list of allowed component instances for a component type.
     *
     * @param componentType        the component type
     * @param resourceInstanceType the instance type to check, or null for any instance
     * @return the list of allowed component instances for the given component
     */
    public List<String> getComponentAllowedList(final ComponentTypeEnum componentType, final ResourceTypeEnum resourceInstanceType) {
        final Map<String, List<String>> componentAllowedResourceTypeMap = getComponentAllowedInstanceTypes().get(componentType.getValue());
        if (MapUtils.isEmpty(componentAllowedResourceTypeMap)) {
            final String resourceTypeString = resourceInstanceType == null ? WILDCARD : resourceInstanceType.getValue();
            LOGGER.warn("No '{}' instance resource type configuration found for '{}'", componentType.getValue(), resourceTypeString);
            return Collections.emptyList();
        }
        return getAllowedInstanceType(resourceInstanceType, componentAllowedResourceTypeMap);
    }

    private Map<String, Map<String, List<String>>> getComponentAllowedInstanceTypes() {
        return configurationManager.getConfiguration().getComponentAllowedInstanceTypes();
    }

    private List<String> getAllowedInstanceType(final ResourceTypeEnum resourceInstanceType,
                                                final Map<String, List<String>> instanceAllowedResourceTypeMap) {
        if (MapUtils.isEmpty(instanceAllowedResourceTypeMap)) {
            return Collections.emptyList();
        }
        List<String> allowedInstanceResourceType = null;
        if (resourceInstanceType == null) {
            if (instanceAllowedResourceTypeMap.containsKey(WILDCARD)) {
                allowedInstanceResourceType = instanceAllowedResourceTypeMap.get(WILDCARD);
            }
        } else {
            allowedInstanceResourceType = instanceAllowedResourceTypeMap.get(resourceInstanceType.getValue());
        }
        if (CollectionUtils.isEmpty(allowedInstanceResourceType)) {
            return Collections.emptyList();
        }
        return allowedInstanceResourceType.stream().collect(Collectors.toList());
    }
}
