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
package org.openecomp.sdc.be.tosca.utils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementSubstitutionFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SubstitutionFilterPropertyDataDefinition;
import org.openecomp.sdc.be.ui.mapper.FilterConstraintMapper;
import org.openecomp.sdc.be.ui.model.UIConstraint;
import org.openecomp.sdc.be.ui.model.UINodeFilter;

public class SubstitutionFilterConverter {

    public UINodeFilter convertToUi(final SubstitutionFilterDataDefinition inSubstitutionFilter) {
        final UINodeFilter uiNodeFilter = new UINodeFilter();

        final List<UIConstraint> uiPropertyFilters = extractPropertyFilter(inSubstitutionFilter);
        if (!uiPropertyFilters.isEmpty()) {
            uiNodeFilter.setProperties(uiPropertyFilters);
        }
        final List<UIConstraint> uiCapabilityFilters = extractCapabilitiesFilter(inSubstitutionFilter);
        if (!uiCapabilityFilters.isEmpty()) {
            uiNodeFilter.setCapabilities(uiCapabilityFilters);
        }
        return uiNodeFilter;
    }

    private List<UIConstraint> extractPropertyFilter(final SubstitutionFilterDataDefinition substitutionFilter) {
        final ListDataDefinition<SubstitutionFilterPropertyDataDefinition> substitutionFilterProperties = substitutionFilter
            .getProperties();
        if (substitutionFilterProperties != null && !substitutionFilterProperties.isEmpty() && CollectionUtils
            .isNotEmpty(substitutionFilterProperties.getListToscaDataDefinition())) {
            final var filterConstraintMapper = new FilterConstraintMapper();
            return substitutionFilterProperties.getListToscaDataDefinition().stream()
                .map(property -> property.getConstraints().iterator().next())
                .map(filterConstraintMapper::mapFrom)
                .map(filterConstraintMapper::mapToUiConstraint)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private List<UIConstraint> extractCapabilitiesFilter(final SubstitutionFilterDataDefinition substitutionFilter) {
        final ListDataDefinition<RequirementSubstitutionFilterCapabilityDataDefinition> substitutionFilterCapabilities = substitutionFilter
            .getCapabilities();
        if (substitutionFilterCapabilities != null && !substitutionFilterCapabilities.isEmpty() && CollectionUtils
            .isNotEmpty(substitutionFilterCapabilities.getListToscaDataDefinition())) {
            final var filterConstraintMapper = new FilterConstraintMapper();
            return substitutionFilterCapabilities.getListToscaDataDefinition().stream()
                .map(capabilities -> capabilities.getProperties().getListToscaDataDefinition().iterator().next())
                .map(property -> property.getConstraints().iterator().next())
                .map(filterConstraintMapper::mapFrom)
                .map(filterConstraintMapper::mapToUiConstraint)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
