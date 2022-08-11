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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.datamodel.utils.ConstraintConvertor;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterPropertyDataDefinition;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.be.ui.model.UINodeFilter;

public class NodeFilterConverter {

    public Map<String, UINodeFilter> convertDataMapToUI(Map<String, CINodeFilterDataDefinition> inMap) {
        return inMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, o -> convertToUi(o.getValue())));
    }

    public UINodeFilter convertToUi(final CINodeFilterDataDefinition inNodeFilter) {
        final UINodeFilter uiNodeFilter = new UINodeFilter();
        final ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        final ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> nodeFilterProperties = inNodeFilter.getProperties();
        if (nodeFilterProperties != null && !nodeFilterProperties.isEmpty()) {
            final List<UIConstraint> propertiesConstraint = nodeFilterProperties.getListToscaDataDefinition().stream().map(property -> {
                    if(property.getType() != null) {
                        return constraintConvertor.convert(property.getConstraints().iterator().next(), property.getType());
                    } else {
                        return constraintConvertor.convert(property.getConstraints().iterator().next());
                    }}).collect(Collectors.toList());
            uiNodeFilter.setProperties(propertiesConstraint);
        }
        final ListDataDefinition<RequirementNodeFilterCapabilityDataDefinition> nodeFilterCapabilities = inNodeFilter.getCapabilities();
        if (nodeFilterCapabilities != null && !nodeFilterCapabilities.isEmpty()) {
            final List<UIConstraint> capabilitiesConstraint = new ArrayList<>();
            nodeFilterCapabilities.getListToscaDataDefinition().forEach(
                requirementNodeFilterCapabilityDataDefinition -> convertCapabilityConstraint(requirementNodeFilterCapabilityDataDefinition,
                    capabilitiesConstraint));
            uiNodeFilter.setCapabilities(capabilitiesConstraint);
        }
        return uiNodeFilter;
    }

    private void convertCapabilityConstraint(final RequirementNodeFilterCapabilityDataDefinition requirementNodeFilterCapabilityDataDefinition,
                                             final List<UIConstraint> capabilitiesConstraint) {
        final ConstraintConvertor constraintConvertor = new ConstraintConvertor();
        requirementNodeFilterCapabilityDataDefinition.getProperties().getListToscaDataDefinition().forEach(property -> {
            final UIConstraint uiConstraint = new UIConstraint();
            uiConstraint.setCapabilityName(requirementNodeFilterCapabilityDataDefinition.getName());
            capabilitiesConstraint.add(
                    constraintConvertor.getUiConstraint(property.getConstraints().iterator().next(), uiConstraint));
        });
    }
}
