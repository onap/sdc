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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.dto.PropertyDefinitionDto;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyDefinitionDtoMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyDefinitionDtoMapper.class);

    public static PropertyDefinition mapTo(final PropertyDefinitionDto propertyDefinitionDto) {
        final var propertyDefinition = new PropertyDefinition();
        propertyDefinition.setUniqueId(propertyDefinitionDto.getUniqueId());
        propertyDefinition.setType(propertyDefinitionDto.getType());
        propertyDefinition.setRequired(propertyDefinitionDto.getRequired());
        propertyDefinition.setName(propertyDefinitionDto.getName());
        if (StringUtils.isNotBlank(propertyDefinitionDto.getSchemaType())) {
            final PropertyDefinition schemaProperty = new PropertyDefinition();
            schemaProperty.setType(propertyDefinitionDto.getSchemaType());
            final SchemaDefinition schema = new SchemaDefinition();
            schema.setProperty(schemaProperty);
            propertyDefinition.setSchema(schema);
        }
        if (CollectionUtils.isNotEmpty(propertyDefinitionDto.getConstraints())) {
            final List<PropertyConstraint> propertyConstraints = new ArrayList<>();

            propertyDefinitionDto.getConstraints().forEach(rawConstraint -> {
                ObjectMapper mapper = new ObjectMapper();

                SimpleModule module = new SimpleModule("customDeserializationModule");
                module.addDeserializer(PropertyConstraint.class, new PropertyOperation.PropertyConstraintJacksonDeserializer());
                mapper.registerModule(module);
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                try {
                    PropertyConstraint constraint =
                        mapper.readValue(new Gson().toJson(rawConstraint, Map.class), PropertyConstraint.class);
                    propertyConstraints.add(constraint);
                } catch (JsonProcessingException e) {
                    LOGGER.error("Could not parse constraint '{}' for property '{}'", rawConstraint, propertyDefinitionDto.getName());
                }
            });
            propertyDefinition.setConstraints(propertyConstraints);
        }
        propertyDefinition.setDescription(propertyDefinitionDto.getDescription());
        propertyDefinition.setValue(new Gson().toJson(propertyDefinitionDto.getValue()));
        propertyDefinition.setDefaultValue(new Gson().toJson(propertyDefinitionDto.getDefaultValue()));
        return propertyDefinition;
    }

    public static PropertyDefinitionDto mapFrom(final PropertyDataDefinition propertyDataDefinition) {
        final var propertyDefinition = new PropertyDefinition(propertyDataDefinition);
        final var propertyDefinitionDto = new PropertyDefinitionDto();
        propertyDefinitionDto.setUniqueId(propertyDefinition.getUniqueId());
        propertyDefinitionDto.setName(propertyDefinition.getName());
        propertyDefinitionDto.setType(propertyDefinition.getType());
        propertyDefinitionDto.setDescription(propertyDefinition.getDescription());
        propertyDefinitionDto.setRequired(propertyDefinition.getRequired());
        propertyDefinitionDto.setSchemaType(propertyDefinition.getSchemaType());
        if (CollectionUtils.isNotEmpty(propertyDefinition.getConstraints())) {
            propertyDefinitionDto.setConstraints(new ArrayList<>(propertyDefinition.getConstraints()));
        }
        propertyDefinitionDto.setValue(new Gson().fromJson(propertyDataDefinition.getValue(), Object.class));
        propertyDefinitionDto.setDefaultValue(new Gson().fromJson(propertyDataDefinition.getDefaultValue(), Object.class));
        return propertyDefinitionDto;
    }
}
