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

import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DERIVED_FROM;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.core.converter.pnfd.model.ConversionDefinition;
import org.openecomp.core.converter.pnfd.model.Transformation;
import org.openecomp.core.converter.pnfd.model.TransformationPropertyType;
import org.openecomp.core.converter.pnfd.strategy.PnfdConversionStrategy;
import org.openecomp.sdc.tosca.services.DataModelUtil;

public class PnfdNodeTypeBlockParser extends AbstractPnfdBlockParser {

    public PnfdNodeTypeBlockParser(final Transformation transformation) {
        super(transformation);
    }

    @Override
    protected Set<Map<String, Object>> findBlocksToParse() {
        final Map<String, Object> customNodeTypeMap = fetchCustomNodeType();
        if (customNodeTypeMap.isEmpty()) {
            return Collections.emptySet();
        }

        final String nodeNamePrefix =
            transformation.getPropertyValue(TransformationPropertyType.NODE_NAME_PREFIX, String.class)
                .orElse("");

        return customNodeTypeMap.entrySet().parallelStream()
            .map(nodeEntry -> {
                final Map<String, Object> map = new HashMap<>();
                map.put(nodeNamePrefix.concat(nodeEntry.getKey()), nodeEntry.getValue());
                return map;
            }).collect(Collectors.toSet());
    }

    @Override
    protected Optional<Map<String, Object>> buildParsedBlock(final Map<String, Object> attributeQuery,
                                                             final Map<String, Object> fromNodeTemplateAttributeMap,
                                                             final ConversionDefinition conversionDefinition) {
        //cannot query for more than one attribute
        if (attributeQuery.keySet().size() > 1) {
            return Optional.empty();
        }
        final String attribute = attributeQuery.keySet().iterator().next();
        final Object queryValue = attributeQuery.get(attribute);
        final Object attributeValueToConvert = fromNodeTemplateAttributeMap.get(attribute);
        final Map<String, Object> parsedNodeTemplate = new HashMap<>();
        if (queryValue == null) {
            PnfdConversionStrategy pnfdConversionStrategy = conversionDefinition.getPnfdConversionStrategy();
            final Optional convertedAttribute = pnfdConversionStrategy
                .convert(DERIVED_FROM.getElementName()
                    .equalsIgnoreCase(attribute) ? attributeValueToBeConverted : attributeValueToConvert);
            if (convertedAttribute.isPresent()) {
                parsedNodeTemplate.put(conversionDefinition.getToAttributeName(), convertedAttribute.get());
            }
        }
        return parsedNodeTemplate.isEmpty() ? Optional.empty() : Optional.of(parsedNodeTemplate);
    }

    @Override
    protected void write(final String blockName, final Map<String, Object> parsedBlockYamlObject) {
        if (!parsedBlockYamlObject.isEmpty()) {
            final NodeType nodeTypeYamlParser = NodeTypeYamlParser.parse(parsedBlockYamlObject);
            DataModelUtil.addNodeType(templateTo, blockName, nodeTypeYamlParser);
        }
    }

}
