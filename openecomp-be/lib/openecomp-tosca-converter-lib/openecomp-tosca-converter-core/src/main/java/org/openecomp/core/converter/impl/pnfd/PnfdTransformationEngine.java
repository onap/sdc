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

package org.openecomp.core.converter.impl.pnfd;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.converter.impl.pnfd.factory.PnfdBlockParserFactory;
import org.openecomp.core.converter.impl.pnfd.parser.ConversionQueryYamlParser;
import org.openecomp.core.converter.pnfd.model.Transformation;
import org.openecomp.core.converter.pnfd.model.TransformationBlock;
import org.openecomp.core.converter.pnfd.model.TransformationDescription;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * Engine that manages the PNF Descriptor transformation process.
 */
public class PnfdTransformationEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(PnfdTransformationEngine.class);

    private final ServiceTemplate templateTo;
    private final ServiceTemplateReaderService templateFrom;
    private final PnfdTransformationDescriptorReader pnfdTransformationDescriptorReader =
        new PnfdTransformationDescriptorReader();
    private TransformationDescription transformationDescription;
    private Map<TransformationBlock, List<Transformation>> transformationGroupByBlockMap;
    private final String descriptorResourcePath;

    public PnfdTransformationEngine(final ServiceTemplateReaderService templateFrom, final ServiceTemplate templateTo) {
        this(templateFrom, templateTo, "pnfdTransformationTemplate/model-driven-conversion.yaml");
    }

    //used for tests purposes
    PnfdTransformationEngine(final ServiceTemplateReaderService templateFrom, final ServiceTemplate templateTo,
            final String descriptorResourcePath) {
        this.templateFrom = templateFrom;
        this.templateTo = templateTo;
        this.descriptorResourcePath = descriptorResourcePath;
    }

    /**
     * Runs the transformation process.
     */
    public void transform() {
        readDefinition();
        initializeTopologyTemplate();
        executeTransformations();
    }

    /**
     * Initializes the topology template and its node template set.
     */
    private void initializeTopologyTemplate() {
        TopologyTemplate topologyTemplate = templateTo.getTopology_template();
        if (topologyTemplate == null) {
            topologyTemplate = new TopologyTemplate();
            templateTo.setTopology_template(topologyTemplate);
        }
        if (topologyTemplate.getNode_templates() == null) {
            topologyTemplate.setNode_templates(new HashMap<>());
        }
    }

    /**
     * Reads the transformation description yaml file.
     */
    private void readDefinition() {
        transformationDescription = pnfdTransformationDescriptorReader.parse(getDefinitionFileInputStream());
    }

    /**
     * Execute all transformations specified in the descriptor.
     */
    private void executeTransformations() {
        final Set<Transformation> transformationSet = transformationDescription.getTransformationSet();
        if (CollectionUtils.isEmpty(transformationSet)) {
            return;
        }
        transformationGroupByBlockMap = transformationSet.stream()
            .filter(Transformation::isValid)
            .collect(Collectors.groupingBy(Transformation::getBlock));
        final Map<String, String> inputsToConvertMap = executeNodeTemplateTransformations();
        executeGetInputFunctionTransformations(inputsToConvertMap);
    }

    /**
     * Parses all topology_template node_template.
     * @return
     *  A map containing any input that was called with a get_input TOSCA function and its getInputFunction
     *  transformation name
     */
    private Map<String, String> executeNodeTemplateTransformations() {
        final List<Transformation> transformationList = transformationGroupByBlockMap
            .get(TransformationBlock.NODE_TEMPLATE);
        if (CollectionUtils.isEmpty(transformationList)) {
            return Collections.emptyMap();
        }

        final Map<String, String> inputsToConvertMap = new HashMap<>();
        transformationList.forEach(transformation ->
            PnfdBlockParserFactory.getInstance().get(transformation).ifPresent(pnfParser -> {
                pnfParser.parse(templateFrom, templateTo);
                if (pnfParser.getInputAndTransformationNameMap().isPresent()) {
                    inputsToConvertMap.putAll(pnfParser.getInputAndTransformationNameMap().get());
                }
            }));
        return inputsToConvertMap;
    }

    /**
     * Parses all topology_template inputs called with a get_input TOSCA function.
     * @param inputsToConvertMap    A map containing the topology_template input name and its conversion definition name
     */
    private void executeGetInputFunctionTransformations(final Map<String, String> inputsToConvertMap) {
        final List<Transformation> transformationListOfGetInputFunction = transformationGroupByBlockMap
            .get(TransformationBlock.GET_INPUT_FUNCTION);

        if(MapUtils.isEmpty(inputsToConvertMap) || CollectionUtils.isEmpty(transformationListOfGetInputFunction)) {
            return;
        }

        final Map<String, List<Transformation>> transformationByName = transformationListOfGetInputFunction.stream()
            .collect(Collectors.groupingBy(Transformation::getName));

        inputsToConvertMap.forEach((inputName, transformationName) -> {
            final List<Transformation> transformationList = transformationByName.get(transformationName);
            if (!CollectionUtils.isEmpty(transformationList)) {
                final Transformation transformation = transformationList.stream()
                    .findFirst().orElse(null);
                if (transformation != null) {
                    final Map<String, Object> conversionQueryMap = new HashMap<>();
                    conversionQueryMap.put(inputName, null);
                    transformation.setConversionQuery(ConversionQueryYamlParser.parse(conversionQueryMap));
                    PnfdBlockParserFactory.getInstance().get(transformation)
                        .ifPresent(pnfParser -> pnfParser.parse(templateFrom, templateTo));
                }
            }
        });
    }

    /**
     * Gets the transformation definition yaml file path.
     * @return The transformation definition yaml path.
     */
    private InputStream getDefinitionFileInputStream() {
        final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(descriptorResourcePath);
        if (resourceAsStream  == null) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(String.format("Could not find resource '%s'", descriptorResourcePath));
            }
            return null;
        }
        return resourceAsStream;
    }
}
