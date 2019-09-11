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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.MapUtils;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.converter.impl.pnfd.PnfdQueryExecutor;
import org.openecomp.core.converter.pnfd.model.ConversionDefinition;
import org.openecomp.core.converter.pnfd.model.ConversionQuery;
import org.openecomp.core.converter.pnfd.model.PnfTransformationToken;
import org.openecomp.core.converter.pnfd.model.Transformation;
import org.openecomp.core.converter.pnfd.parser.PnfdBlockParser;

public abstract class AbstractPnfdBlockParser implements PnfdBlockParser {

    protected final Transformation transformation;
    protected ServiceTemplateReaderService templateFrom;
    protected ServiceTemplate templateTo;
    protected Object attributeValueToBeConverted = "";

    public AbstractPnfdBlockParser(final Transformation transformation) {
        this.transformation = transformation;
    }

    /**
     * Parses a PNFD block based on the {@link Transformation} provided during the {@link PnfdBlockParser}
     * instantiation.
     *
     * @param templateFrom the original PNFD template
     * @param templateTo the resulting PNFD template
     */
    public void parse(final ServiceTemplateReaderService templateFrom, final ServiceTemplate templateTo) {
        this.templateFrom = templateFrom;
        this.templateTo = templateTo;
        final Set<Map<String, Object>> blockToParseSet = findBlocksToParse();
        if (!blockToParseSet.isEmpty()) {
            blockToParseSet.forEach(this::parse);
        }
    }

    /**
     * Applies all specified conversions in {@link Transformation#getConversionDefinitionList()} for the given
     * blockYamlObject.
     *
     * @param blockYamlObject the block content as a YAML object
     */
    protected void parse(final Map<String, Object> blockYamlObject) {
        if (MapUtils.isEmpty(blockYamlObject)) {
            return;
        }
        final List<ConversionDefinition> conversionDefinitionList = transformation.getConversionDefinitionList();
        final Map<String, Object> parsedBlockYamlObject = new HashMap<>();
        final String blockName = blockYamlObject.keySet().iterator().next();
        conversionDefinitionList.stream()
            .filter(conversionDefinition -> conversionDefinition.getConversionQuery().isValidAttributeQuery())
            .forEach(conversionDefinition -> {
                final Map<String, Object> query =
                    (Map<String, Object>) conversionDefinition.getConversionQuery().getQuery();
                final Map<String, Object> blockAttributeMap = (Map<String, Object>) blockYamlObject.get(blockName);
                final Optional<Map<String, Object>> parsedBlockAttributeMap = buildParsedBlock(query, blockAttributeMap
                    , conversionDefinition);
                parsedBlockAttributeMap.ifPresent(convertedNodeTemplateAttributeMap1 ->
                    mergeYamlObjects(parsedBlockYamlObject, convertedNodeTemplateAttributeMap1)
                );
            });

        write(blockName, parsedBlockYamlObject);
    }

    /**
     * Writes the block in the resulting {@link ServiceTemplate} {@link #templateTo}.
     *
     * @param blockName the name of the block
     * @param parsedBlockYamlObject the block content as a YAML object
     */
    protected abstract void write(final String blockName, final Map<String, Object> parsedBlockYamlObject);

    /**
     * Uses the provided attribute query to find a attribute in the original YAML object and apply the provided
     * conversion.
     *
     * @param attributeQuery the attribute query
     * @param fromNodeTemplateAttributeMap the original YAML object
     * @param conversionDefinition the conversion
     * @return the rebuilt original YAML object with the converted attribute
     */
    protected abstract Optional<Map<String, Object>> buildParsedBlock(final Map<String, Object> attributeQuery,
                                                                      final Map<String, Object> fromNodeTemplateAttributeMap,
                                                                      final ConversionDefinition conversionDefinition);

    /**
     * Merges two YAML objects.
     *
     * @param originalMap original YAML object
     * @param toBeMergedMap YAML object to be merged
     * @return the new YAML object representing the merge result.
     */
    protected Map<String, Object> mergeYamlObjects(final Map<String, Object> originalMap,
        final Map<String, Object> toBeMergedMap) {
        toBeMergedMap.forEach(
            (key, value) -> originalMap.merge(key, value,
                (toBeMergedValue, originalValue) -> {
                    if (originalValue instanceof Map) {
                        return mergeYamlObjects((Map) originalValue, (Map) toBeMergedValue);
                    }
                    return originalValue;
                })
        );

        return originalMap;
    }

    /**
     * Executes the provided {@link #transformation getConversionQuery} YAML query to find the blocks to be parsed in
     * {@link #templateFrom}.
     *
     * @return The YAML blocks found
     */
    protected abstract Set<Map<String, Object>> findBlocksToParse();

    /**
     * Checks if the YAML object is a TOSCA get_input call
     *
     * @param yamlObject the YAML object
     * @return {@code true} if the YAML object is a TOSCA get_input call, {@code false} otherwise
     */
    protected boolean isGetInputFunction(final Object yamlObject) {
        if (yamlObject instanceof Map) {
            final Map<String, Object> yamlMap = (Map<String, Object>) yamlObject;
            return yamlMap.containsKey(PnfTransformationToken.GET_INPUT.getName());
        }

        return false;
    }

    /**
     * Extracts the value from an YAML Object.
     * @param yamlObject
     * @return The Object value from the yamlObject parameter.
     */
    protected String extractObjectValue(final Object yamlObject) {
        if (yamlObject instanceof Map) {
            final Map<String, Object> yamlMap = (Map<String, Object>) yamlObject;
            return (String) yamlMap.values().stream().findFirst().orElse(null);
        }

        return null;
    }

    /**
     * Gets the stored input names called with TOSCA get_input function and its transformation configured in {@link
     * ConversionDefinition#getToGetInput()}
     */
    public Optional<Map<String, String>> getInputAndTransformationNameMap() {
        return Optional.empty();
    }

    /**
     * Finds all the derived node types from the provided node types.
     *
     * @param rootNodeTypeMap a map with the root node types to find the derived ones
     * @param derivedNodeTypeMap a map that will be filled with the derived node types
     */
    private void findAllDerivedNodeType(final Map<String, Object> rootNodeTypeMap,
                                          final Map<String, Object> derivedNodeTypeMap) {
        templateFrom.getNodeTypes().entrySet().stream()
            .filter(nodeEntry -> rootNodeTypeMap.containsKey(extractObjectValue(nodeEntry.getValue())))
            .forEach(nodeEntry -> {
                if (!derivedNodeTypeMap.containsKey(nodeEntry.getKey())) {
                    derivedNodeTypeMap.put(nodeEntry.getKey(), nodeEntry.getValue());
                    final ImmutableMap<String, Object> newRootNodeTypeMap = ImmutableMap
                        .of(nodeEntry.getKey(), nodeEntry.getValue());
                    findAllDerivedNodeType(newRootNodeTypeMap, derivedNodeTypeMap);
                }
            });
    }

    /**
     * Fetches all Custom NodeTypes based on the query result.
     * @return a map with all custom Node Types that matches with the query result.
     */
    protected Map<String, Object> fetchCustomNodeType() {
        final Map<String, Object> nodeTypesMap = templateFrom.getNodeTypes();
        if (MapUtils.isEmpty(nodeTypesMap)) {
            return Collections.emptyMap();
        }
        final ConversionQuery conversionQuery = transformation.getConversionQuery();
        final Map<String, Object> customNodeTypesMap = new HashMap<>();
        nodeTypesMap.entrySet().stream()
            .filter(nodeEntry -> PnfdQueryExecutor.find(conversionQuery, nodeEntry.getValue()))
            .forEach(customNode -> {
                attributeValueToBeConverted = extractObjectValue(customNode.getValue());
                customNodeTypesMap.put(customNode.getKey(), customNode.getValue());
            });
        final Map<String, Object> childNodeTypeMap = new HashMap<>();
        findAllDerivedNodeType(customNodeTypesMap, childNodeTypeMap);
        customNodeTypesMap.putAll(childNodeTypeMap);
        return customNodeTypesMap;
    }

}
