/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.model.mapper;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.dto.PropertyDefinitionDto;

public class PropertyDefinitionDtoMapper {

    public static PropertyDefinition mapTo(final PropertyDefinitionDto propertyDefinitionDto) {
        final var propertyDefinition = new PropertyDefinition();
        propertyDefinition.setUniqueId(propertyDefinitionDto.getUniqueId());
        propertyDefinition.setType(propertyDefinitionDto.getType());
        propertyDefinition.setRequired(propertyDefinitionDto.getRequired());
        propertyDefinition.setName(propertyDefinitionDto.getName());
        if (CollectionUtils.isNotEmpty(propertyDefinitionDto.getConstraints())) {
            final List<PropertyConstraint> propertyConstraints = new ArrayList<>();
            propertyDefinition.setConstraints(propertyConstraints);
            propertyConstraints.addAll(propertyDefinitionDto.getConstraints());
        }
        propertyDefinition.setValue(new Gson().toJson(propertyDefinitionDto.getValue()));
        propertyDefinition.setDefaultValue(new Gson().toJson(propertyDefinitionDto.getDefaultValue()));
        return propertyDefinition;
    }

    public static PropertyDefinitionDto mapFrom(final PropertyDataDefinition propertyDataDefinition) {
        final var propertyDefinition = new PropertyDefinition(propertyDataDefinition);
        final var propertyDefinitionDto = new PropertyDefinitionDto();
        propertyDefinitionDto.setUniqueId(propertyDefinition.getUniqueId());
        propertyDefinitionDto.setType(propertyDefinition.getType());
        propertyDefinitionDto.setRequired(propertyDefinition.getRequired());
        propertyDefinitionDto.setName(propertyDefinition.getName());
        if (CollectionUtils.isNotEmpty(propertyDefinition.getConstraints())) {
            final List<PropertyConstraint> propertyConstraints = new ArrayList<>();
            propertyDefinitionDto.setConstraints(propertyConstraints);
            propertyConstraints.addAll(propertyDefinition.getConstraints());
        }
        propertyDefinitionDto.setValue(new Gson().fromJson(propertyDataDefinition.getValue(), Object.class));
        propertyDefinitionDto.setDefaultValue(new Gson().fromJson(propertyDataDefinition.getDefaultValue(), Object.class));
        return propertyDefinitionDto;
    }
}
