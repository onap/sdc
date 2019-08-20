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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.core.converter.pnfd.parser.AbstractPnfdBlockParser;
import org.openecomp.core.converter.impl.pnfd.PnfdQueryExecutor;
import org.openecomp.core.converter.pnfd.model.ConversionDefinition;
import org.openecomp.core.converter.pnfd.model.ConversionQuery;
import org.openecomp.core.converter.pnfd.model.Transformation;
import org.openecomp.core.converter.impl.pnfd.strategy.CopyConversionStrategy;
import org.openecomp.core.converter.pnfd.strategy.PnfdConversionStrategy;
import org.openecomp.sdc.tosca.services.DataModelUtil;

public class PnfdNodeTemplateBlockParser extends AbstractPnfdBlockParser {

    private Map<String, String> inputNameToConvertMap = new HashMap<>();

    public PnfdNodeTemplateBlockParser(final Transformation transformation) {
        super(transformation);
    }

    public Optional<Map<String, Object>> buildParsedBlock(final Map<String, Object> attributeQuery,
        final Map<String, Object> fromNodeTemplateAttributeMap,
        final ConversionDefinition conversionDefinition) {
        //cannot query for more than one attribute
        if (attributeQuery.keySet().size() > 1) {
            return Optional.empty();
        }
        final String attribute = attributeQuery.keySet().iterator().next();
        final Object queryValue = attributeQuery.get(attribute);
        final Object attributeValueToConvert = fromNodeTemplateAttributeMap.get(attribute);
        if (queryValue == null) {
            PnfdConversionStrategy pnfdConversionStrategy = conversionDefinition.getPnfdConversionStrategy();
            if (isGetInputFunction(attributeValueToConvert)) {
                inputNameToConvertMap.put(extractGetInputFunctionValue(attributeValueToConvert)
                    , conversionDefinition.getToGetInput()
                );
                pnfdConversionStrategy = new CopyConversionStrategy();
            }
            final Map<String, Object> parsedNodeTemplate = new HashMap<>();
            final Optional convertedAttribute = pnfdConversionStrategy.convert(attributeValueToConvert);
            if (convertedAttribute.isPresent()) {
                parsedNodeTemplate.put(conversionDefinition.getToAttributeName(), convertedAttribute.get());
            }

            return parsedNodeTemplate.isEmpty() ? Optional.empty() : Optional.of(parsedNodeTemplate);
        } else {
            if (!(queryValue instanceof Map) || !(attributeValueToConvert instanceof Map)) {
                return Optional.empty();
            }
            final Map<String, Object> parsedNodeTemplate = new HashMap<>();
            final Optional<Map<String, Object>> builtNodeTemplate = buildParsedBlock(
                (Map<String, Object>) queryValue,
                (Map<String, Object>) attributeValueToConvert, conversionDefinition);
            builtNodeTemplate.ifPresent(builtNodeTemplate1 -> parsedNodeTemplate.put(attribute, builtNodeTemplate1));

            return parsedNodeTemplate.isEmpty() ? Optional.empty() : Optional.of(parsedNodeTemplate);
        }
    }

    protected Set<Map<String, Object>> findBlocksToParse() {
        final ConversionQuery conversionQuery = transformation.getConversionQuery();
        final Map<String, Object> nodeTemplateMap = templateFrom.getNodeTemplates();
        if (MapUtils.isEmpty(nodeTemplateMap)) {
            return Collections.emptySet();
        }

        return nodeTemplateMap.entrySet().stream()
            .filter(mapEntry -> PnfdQueryExecutor.find(conversionQuery, mapEntry.getValue()))
            .map(stringObjectEntry -> {
                final Map<String, Object> map = new HashMap<>();
                map.put(stringObjectEntry.getKey(), stringObjectEntry.getValue());
                return map;
            }).collect(Collectors.toSet());
    }

    @Override
    public void write(final String nodeTemplateName, final Map<String, Object> parsedNodeTemplateMap) {
        if (!parsedNodeTemplateMap.isEmpty()) {
            final NodeTemplate parsedNodeTemplate = NodeTemplateYamlParser.parse(parsedNodeTemplateMap);
            DataModelUtil.addNodeTemplate(templateTo, nodeTemplateName, parsedNodeTemplate);
        }
    }

    @Override
    public Optional<Map<String, String>> getInputAndTransformationNameMap() {
        return Optional.of(inputNameToConvertMap);
    }
}
