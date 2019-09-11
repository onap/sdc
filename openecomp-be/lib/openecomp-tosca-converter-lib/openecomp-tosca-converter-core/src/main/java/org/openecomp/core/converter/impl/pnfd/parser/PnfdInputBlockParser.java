/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.core.converter.impl.pnfd.parser;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.openecomp.core.converter.impl.pnfd.PnfdQueryExecutor;
import org.openecomp.core.converter.pnfd.model.ConversionDefinition;
import org.openecomp.core.converter.pnfd.model.ConversionQuery;
import org.openecomp.core.converter.pnfd.model.Transformation;
import org.openecomp.core.converter.pnfd.strategy.PnfdConversionStrategy;
import org.openecomp.sdc.tosca.services.DataModelUtil;

public class PnfdInputBlockParser extends AbstractPnfdBlockParser {

    public PnfdInputBlockParser(final Transformation transformation) {
        super(transformation);
    }

    @Override
    protected Optional<Map<String, Object>> buildParsedBlock(final Map<String, Object> attributeQuery,
        final Map<String, Object> originalAttributeMap, final ConversionDefinition conversionDefinition) {
        //cannot query for more than one attribute
        if (attributeQuery.keySet().size() > 1) {
            return Optional.empty();
        }
        final String attribute = attributeQuery.keySet().iterator().next();
        final Map<String, Object> parsedInput = new HashMap<>();
        if (attributeQuery.get(attribute) == null) {
            final PnfdConversionStrategy pnfdConversionStrategy = conversionDefinition.getPnfdConversionStrategy();
            final Optional convertedAttribute = pnfdConversionStrategy.convert(originalAttributeMap.get(attribute));
            convertedAttribute.ifPresent(convertedAttribute1 -> parsedInput.put(conversionDefinition.getToAttributeName(), convertedAttribute1));
        } else {
            final Optional<Map<String, Object>> builtInput = buildParsedBlock((Map<String, Object>) attributeQuery.get(attribute),
                (Map<String, Object>) originalAttributeMap.get(attribute), conversionDefinition);
            builtInput.ifPresent(builtInput1 -> parsedInput.put(attribute, builtInput1));
        }

        return parsedInput.isEmpty() ? Optional.empty() : Optional.of(parsedInput);
    }

    @Override
    protected void write(final String blockName, final Map<String, Object> parsedBlockYamlObject) {
        if (!parsedBlockYamlObject.isEmpty()) {
            final ParameterDefinition parameterDefinition = ParameterDefinitionYamlParser.parse(parsedBlockYamlObject);
            DataModelUtil.addInputParameterToTopologyTemplate(templateTo, blockName, parameterDefinition);
        }
    }

    @Override
    protected Set<Map<String, Object>> findBlocksToParse() {
        final ConversionQuery conversionQuery = transformation.getConversionQuery();
        final Map<String, Object> inputsMap = templateFrom.getInputs();
        if (MapUtils.isEmpty(inputsMap)) {
            return Collections.emptySet();
        }

        return inputsMap.entrySet().stream()
            .filter(inputMapEntry -> PnfdQueryExecutor
                .find(conversionQuery, ImmutableMap.of(inputMapEntry.getKey(), inputMapEntry.getValue()))
            )
            .map(inputMapEntry -> {
                final Map<String, Object> map = new HashMap<>();
                map.put(inputMapEntry.getKey(), inputMapEntry.getValue());
                return map;
            }).collect(Collectors.toSet());
    }

}
