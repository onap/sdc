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

package org.openecomp.sdc.be.datatypes.elements;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.FilterValueType;
import org.openecomp.sdc.be.datatypes.enums.PropertyFilterTargetType;
import org.openecomp.sdc.be.utils.PropertyFilterConstraintDataDefinitionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyFilterConstraintDataDefinitionJsonDeserializer extends StdDeserializer<PropertyFilterConstraintDataDefinition> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyFilterConstraintDataDefinitionJsonDeserializer.class);
    private static final String COULD_NOT_PARSE_CLASS = "Could not parse {} value as {}";

    public PropertyFilterConstraintDataDefinitionJsonDeserializer() {
        this(null);
    }

    public PropertyFilterConstraintDataDefinitionJsonDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public PropertyFilterConstraintDataDefinition deserialize(final JsonParser jsonParser, final DeserializationContext context) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        if (node.isTextual()) {
            return PropertyFilterConstraintDataDefinitionHelper.convertLegacyConstraint(node.asText());
        }

        final var propertyFilterConstraint = new PropertyFilterConstraintDataDefinition();
        if (node.get("propertyName") != null) {
            propertyFilterConstraint.setPropertyName(node.get("propertyName").asText());
        }
        if (node.get("capabilityName") != null) {
            propertyFilterConstraint.setCapabilityName(node.get("capabilityName").asText());
        }
        if (node.get("targetType") != null) {
            propertyFilterConstraint.setTargetType(PropertyFilterTargetType.valueOf(node.get("targetType").asText()));
        }
        if (node.get("operator") != null) {
            propertyFilterConstraint.setOperator(ConstraintType.valueOf(node.get("operator").asText()));
        }
        if (node.get("valueType") != null) {
            propertyFilterConstraint.setValueType(FilterValueType.valueOf(node.get("valueType").asText()));
        }
        propertyFilterConstraint.setValue(deserializeValue(node.get("value")));

        return propertyFilterConstraint;
    }

    private Object deserializeValue(final JsonNode value) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.treeToValue(value, ToscaFunction.class);
        } catch (final Exception e) {
            LOGGER.debug(COULD_NOT_PARSE_CLASS, PropertyFilterConstraintDataDefinition.class.getName(), ToscaFunction.class.getName(), e);
        }
        try {
            return objectMapper.treeToValue(value, Map.class);
        } catch (final Exception e) {
            LOGGER.debug(COULD_NOT_PARSE_CLASS, PropertyFilterConstraintDataDefinition.class.getName(), Map.class.getName(), e);
        }
        try {
            return objectMapper.treeToValue(value, List.class);
        } catch (final Exception e) {
            LOGGER.debug(COULD_NOT_PARSE_CLASS, PropertyFilterConstraintDataDefinition.class.getName(), List.class.getName(), e);
        }
        try {
            return objectMapper.treeToValue(value, String.class);
        } catch (final Exception e) {
            LOGGER.debug(COULD_NOT_PARSE_CLASS, PropertyFilterConstraintDataDefinition.class.getName(), String.class.getName(), e);
        }

        return null;
    }

}
