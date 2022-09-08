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
package org.openecomp.sdc.be.components.impl.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.model.UploadNodeFilterCapabilitiesInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterInfo;
import org.openecomp.sdc.be.model.UploadNodeFilterPropertyInfo;
import org.openecomp.sdc.be.utils.PropertyFilterConstraintDataDefinitionHelper;

public class CINodeFilterUtils {

    public CINodeFilterDataDefinition getNodeFilterDataDefinition(final UploadNodeFilterInfo uploadNodeFilterInfo, final String uniqueId) {
        final var nodeFilterDataDefinition = new CINodeFilterDataDefinition();
        nodeFilterDataDefinition.setName(uploadNodeFilterInfo.getName());
        final List<PropertyFilterDataDefinition> propertyFilterList = uploadNodeFilterInfo.getProperties().stream()
            .map(this::buildOnePropertyFilterForEachConstraint)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        final ListDataDefinition<PropertyFilterDataDefinition> listDataDefinition = new ListDataDefinition<>();
        listDataDefinition.getListToscaDataDefinition().addAll(propertyFilterList);
        nodeFilterDataDefinition.setProperties(listDataDefinition);
        nodeFilterDataDefinition.setCapabilities(convertCapabilities(uploadNodeFilterInfo.getCapabilities()));
        nodeFilterDataDefinition.setID(uniqueId);
        nodeFilterDataDefinition.setTosca_id(uploadNodeFilterInfo.getTosca_id());
        return nodeFilterDataDefinition;
    }

    private ListDataDefinition<RequirementNodeFilterCapabilityDataDefinition> convertCapabilities(
        Map<String, UploadNodeFilterCapabilitiesInfo> capabilities) {
        ListDataDefinition<RequirementNodeFilterCapabilityDataDefinition> listDataDefinition = new ListDataDefinition<>();
        for (UploadNodeFilterCapabilitiesInfo capability : capabilities.values()) {
            RequirementNodeFilterCapabilityDataDefinition requirementNodeFilterCapabilityDataDefinition = convertCapability(capability);
            listDataDefinition.add(requirementNodeFilterCapabilityDataDefinition);
        }
        return listDataDefinition;
    }

    private RequirementNodeFilterCapabilityDataDefinition convertCapability(UploadNodeFilterCapabilitiesInfo capability) {
        RequirementNodeFilterCapabilityDataDefinition retVal = new RequirementNodeFilterCapabilityDataDefinition();
        retVal.setName(capability.getName());
        List<PropertyFilterDataDefinition> propertyFilterList = capability.getProperties().stream()
            .map(filterPropertyInfo -> buildProperty(capability.getName(), filterPropertyInfo))
            .collect(Collectors.toList());
        ListDataDefinition<PropertyFilterDataDefinition> propsList = new ListDataDefinition<>();
        propsList.getListToscaDataDefinition().addAll(propertyFilterList);
        retVal.setProperties(propsList);
        return retVal;
    }

    private PropertyFilterDataDefinition buildProperty(final String capabilityName, final UploadNodeFilterPropertyInfo uploadNodeFilterPropertyInfo) {
        final var propertyFilter = new PropertyFilterDataDefinition();
        propertyFilter.setName(uploadNodeFilterPropertyInfo.getName());
        final List<String> propertyConstraints = uploadNodeFilterPropertyInfo.getValues();
        if (CollectionUtils.isNotEmpty(propertyConstraints)) {
            propertyFilter.setConstraints(
                propertyConstraints.stream()
                    .map(PropertyFilterConstraintDataDefinitionHelper::convertLegacyConstraint)
                    .peek(propertyFilterConstraintDataDefinition -> propertyFilterConstraintDataDefinition.setCapabilityName(capabilityName))
                    .collect(Collectors.toList())
            );
        }
        return propertyFilter;
    }

    private List<PropertyFilterDataDefinition> buildOnePropertyFilterForEachConstraint(final UploadNodeFilterPropertyInfo uploadNodeFilterProperty) {
        final List<String> propertyConstraints = uploadNodeFilterProperty.getValues();
        if (CollectionUtils.isNotEmpty(propertyConstraints)) {
            return propertyConstraints.stream()
                .map(PropertyFilterConstraintDataDefinitionHelper::convertLegacyConstraint)
                .map(propertyFilterConstraint -> {
                    final var propertyFilter = new PropertyFilterDataDefinition();
                    propertyFilter.setName(uploadNodeFilterProperty.getName());
                    propertyFilter.setConstraints(List.of(propertyFilterConstraint));
                    return propertyFilter;
                }).collect(Collectors.toList());
        } else {
            final var propertyFilter = new PropertyFilterDataDefinition();
            propertyFilter.setName(uploadNodeFilterProperty.getName());
            return List.of(propertyFilter);
        }
    }
}
