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

package org.openecomp.sdc.be.tosca.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterPropertyDataDefinition;
import org.openecomp.sdc.be.tosca.model.CapabilityFilter;
import org.openecomp.sdc.be.tosca.model.NodeFilter;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.be.ui.model.UINodeFilter;

public class NodeFilterConverter {


    public NodeFilter convertNodeFilter(CINodeFilterDataDefinition nodeFilterData) {
        NodeFilter retVal = new NodeFilter();
        if (nodeFilterData.getCapabilities() != null) {
            retVal.setCapabilities(convertCapabilities(nodeFilterData.getCapabilities().getListToscaDataDefinition()));
        }
        if (nodeFilterData.getProperties() != null) {
            retVal.setProperties(convertProperties(nodeFilterData.getProperties().getListToscaDataDefinition()));
        }
        return retVal;
    }

    private List<Map<String, CapabilityFilter>> convertCapabilities(
            List<RequirementNodeFilterCapabilityDataDefinition> capabilities) {
        if (CollectionUtils.isEmpty(capabilities)) {
            return Collections.emptyList();
        }
        return capabilities.stream().map(this::transformCapability).collect(Collectors.toList());
    }

    private Map<String, CapabilityFilter> transformCapability(
            RequirementNodeFilterCapabilityDataDefinition capability) {
        Map<String, CapabilityFilter> retVal = new HashMap<>();
        if (capability.getProperties() == null) {
            return retVal;
        }
        List<RequirementNodeFilterPropertyDataDefinition> propertyDataDefinitionList =
                capability.getProperties().getListToscaDataDefinition();
        for (RequirementNodeFilterPropertyDataDefinition propertyDataDefinition : propertyDataDefinitionList) {
            retVal.put(capability.getName(), convertCapabilityProperty(propertyDataDefinition));
        }
        return retVal;
    }

    private List<Map<String, List<Object>>> convertProperties(
            List<RequirementNodeFilterPropertyDataDefinition> properties) {
        if (CollectionUtils.isEmpty(properties)) {
            return Collections.emptyList();
        }
        return properties.stream().map(this::transformProperty).collect(Collectors.toList());
    }

    private CapabilityFilter convertCapabilityProperty(RequirementNodeFilterPropertyDataDefinition property) {
        TransformCapabilityData transformCapabilityData = new TransformCapabilityData(property).invoke();
        Map<String, List<Object>> tranformedMap = transformCapabilityData.getRetVal();
        List<Object> constraints = transformCapabilityData.getConstraints();
        tranformedMap.put(property.getName(), constraints);
        CapabilityFilter capabilityFilter = new CapabilityFilter();
        capabilityFilter.setProperties(Collections.singletonList(tranformedMap));
        return capabilityFilter;
    }


    private Map<String, List<Object>> transformProperty(RequirementNodeFilterPropertyDataDefinition property) {
        TransformCapabilityData transformCapabilityData = new TransformCapabilityData(property).invoke();
        Map<String, List<Object>> retVal = transformCapabilityData.getRetVal();
        List<Object> constraints = transformCapabilityData.getConstraints();
        retVal.put(property.getName(), constraints);

        return retVal;
    }

    private class TransformCapabilityData {

        private RequirementNodeFilterPropertyDataDefinition property;
        private Map<String, List<Object>> retVal;
        private List<Object> constraints;

        public TransformCapabilityData(RequirementNodeFilterPropertyDataDefinition property) {
            this.property = property;
        }

        public Map<String, List<Object>> getRetVal() {
            return retVal;
        }

        public List<Object> getConstraints() {
            return constraints;
        }

        public TransformCapabilityData invoke() {
            final List<String> propertyConstraints = property.getConstraints();
            if (CollectionUtils.isEmpty(propertyConstraints)) {
                return this;
            }
            this.constraints = propertyConstraints.stream().map(c -> (Object) c).collect(Collectors.toList());
            return this;
        }
    }

    public Map<String, UINodeFilter> convertDataMapToUI(Map<String, CINodeFilterDataDefinition> inMap) {
        return inMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, o -> convertToUi(o.getValue())));
    }

    public UINodeFilter convertToUi(CINodeFilterDataDefinition inNodeFilter) {
        UINodeFilter retVal = new UINodeFilter();
        final ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        if (inNodeFilter.getProperties() == null || inNodeFilter.getProperties().isEmpty()) {
            return retVal;
        }
        List<UIConstraint> constraints = inNodeFilter.getProperties().getListToscaDataDefinition().stream()
                                                     .map(property -> property.getConstraints().iterator().next())
                                                     .map(constraintConvertor::convert)
                                                     .collect(Collectors.toList());
        retVal.setProperties(constraints);
        return retVal;
    }
}
