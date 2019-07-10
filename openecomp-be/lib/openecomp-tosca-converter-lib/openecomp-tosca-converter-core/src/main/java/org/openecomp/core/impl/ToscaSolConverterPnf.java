/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019 Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.core.impl;

import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.CONSTRAINTS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DEFAULT_VALUE;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DESCRIPTION;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.ENTRY_SCHEMA;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.GET_INPUT;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.PROPERTIES;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.REQUIRED;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.TYPE;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.onap.sdc.tosca.datatypes.model.Constraint;
import org.onap.sdc.tosca.datatypes.model.EntrySchema;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.sdc.tosca.services.DataModelUtil;

public class ToscaSolConverterPnf extends AbstractToscaSolConverter {

    private static final String PNF_EXT_CP_TYPE = "tosca.nodes.nfv.PnfExtCp";
    private static final String EXT_CP_TYPE = "org.openecomp.resource.cp.v2.extCP";
    private static final String LAYER_PROTOCOLS = "layer_protocols";
    private static final String IP_V4 = "ipv4";
    private static final String IP_V6 = "ipv6";
    private static final String IP_VERSION = "ip_version";
    private static final String ASSIGNMENT_METHOD = "assingment_method";
    private static final String DHCP = "dhcp";
    private static final String IP_REQUIREMENTS = "ip_requirements";

    /**
     * For PNF the node templates are converted ETSI node types to ecomp node types All other data i.e. inputs,
     * substitution mappings and outputs are simply dropped at this stage. The equivalent ecomp data will be added when
     * the vsp is imported into the catalog.
     *
     * @param serviceTemplate - the service template
     * @param readerService - the reader service
     */
    @Override
    public void convertTopologyTemplate(final ServiceTemplate serviceTemplate,
        final ServiceTemplateReaderService readerService) {
        convertNodeTemplatesToEcompTypes(serviceTemplate, readerService);
        addEmptyNodeTemplatesIfNoneDefined(serviceTemplate);
        addInputsToEcompTypes(serviceTemplate, readerService);
    }

    private void addInputsToEcompTypes(final ServiceTemplate serviceTemplate,
        final ServiceTemplateReaderService readerService) {
        final Map<String, Object> inputs = readerService.getInputs();
        inputs.entrySet().stream().forEach(input -> {
            final String parameterDefinitionId = input.getKey();
            final Map value = (Map) input.getValue();

            final Object required = value.get(REQUIRED);
            final Object constraints = value.get(CONSTRAINTS);
            final Object entrySchema = value.get(ENTRY_SCHEMA);
            final ParameterDefinition parameterDefinition =
                DataModelUtil.createParameterDefinition(
                    (String) value.get(TYPE),
                    (String) value.get(DESCRIPTION),
                    required instanceof Boolean ? (Boolean) required : false,
                    constraints instanceof List ? (List<Constraint>) constraints : null,
                    entrySchema instanceof EntrySchema ? (EntrySchema) entrySchema : null,
                    (String) value.get(DEFAULT_VALUE));

            DataModelUtil
                .addInputParameterToTopologyTemplate(serviceTemplate, parameterDefinitionId, parameterDefinition);
        });
    }

    /**
     * PNF only has nfv.PNF and nfv.PnfExtCp types defined in ETSI SOL001 v2.5.1. - The PNF is mapped to the outer
     * Abstract PNF container in ecomp model and hence nfv.PNF is dropped here. - nfv.PnfExtCp is mapped to ecomp
     * v2.extCp type.
     *
     * @param serviceTemplate - the service template
     * @param readerService - the reader service
     */
    private void convertNodeTemplatesToEcompTypes(final ServiceTemplate serviceTemplate,
        final ServiceTemplateReaderService readerService) {
        final Map<String, Object> nodeTemplates = readerService.getNodeTemplates();
        if (MapUtils.isEmpty(nodeTemplates)) {
            return;
        }

        nodeTemplates.entrySet().stream()
            .filter(nodeTemplateEntry ->
                PNF_EXT_CP_TYPE.equals(((Map<String, Object>) nodeTemplateEntry.getValue()).get(TYPE)))
            .forEach(nodeTemplateEntry ->
                DataModelUtil.addNodeTemplate(serviceTemplate, nodeTemplateEntry.getKey(),
                    convertToEcompConnectionPointNodeType((Map<String, Object>) nodeTemplateEntry.getValue())));
    }

    /**
     * Converts from the ETSI PnfExtCp node type to ecomp v2.extCP node type The following properties are mapped -
     * layer_protocols is mapped to ip_requirements if it contains the values ipv4 and/or ipv6. All other data e.g.
     * remaining properties, requirements, capabilities are not mapped over to ecomp equivalent
     *
     * @param pnfExtCp - the ETSI PnfExtCp map
     * @return ecomp v2.extCP node type
     */
    private NodeTemplate convertToEcompConnectionPointNodeType(final Map<String, Object> pnfExtCp) {
        final NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setType(EXT_CP_TYPE);
        final Map<String, Object> properties = (Map<String, Object>) pnfExtCp.get(PROPERTIES);
        properties.entrySet().stream()
            .filter(stringObjectEntry -> LAYER_PROTOCOLS.equals(stringObjectEntry.getKey()))
            .forEach(stringObjectEntry -> {
                final Object propertyValue = stringObjectEntry.getValue();
                if (propertyValue instanceof List) {
                    final List<Map<String, Object>> ipRequirements =
                        convertToIpRequirementsProperty((List<String>) propertyValue);
                    if (!ipRequirements.isEmpty()) {
                        final Map<String, Object> convertedProperties = new HashMap<>();
                        convertedProperties.put(IP_REQUIREMENTS, ipRequirements);
                        nodeTemplate.setProperties(convertedProperties);
                    }
                } else if (propertyValue instanceof AbstractMap &&
                    ((AbstractMap) propertyValue).containsKey(GET_INPUT)) {
                    final Set entrySet = ((AbstractMap) propertyValue).entrySet();
                    final Map<String, Object> convertedProperties = new HashMap<>();
                    convertedProperties.put(IP_REQUIREMENTS, entrySet);
                    nodeTemplate.setProperties(convertedProperties);
                }
            });
        return nodeTemplate;
    }

    private List<Map<String, Object>> convertToIpRequirementsProperty(final List<String> layerProtocols) {
        return layerProtocols.stream()
            .filter(layerProtocol -> IP_V4.equals(layerProtocol) || IP_V6.equals(layerProtocol))
            .map(this::createIpPropertyElement)
            .collect(Collectors.toList());
    }

    private Map<String, Object> createIpPropertyElement(final String ipVersion) {
        final int version = IP_V4.equals(ipVersion) ? 4 : 6;
        final Map<String, Object> result = new HashMap<>();
        result.put(IP_VERSION, version);
        result.put(ASSIGNMENT_METHOD, DHCP);
        return result;
    }

    private void addEmptyNodeTemplatesIfNoneDefined(final ServiceTemplate serviceTemplate) {
        TopologyTemplate topologyTemplate = serviceTemplate.getTopology_template();
        if (Objects.isNull(topologyTemplate)) {
            topologyTemplate = new TopologyTemplate();
            serviceTemplate.setTopology_template(topologyTemplate);
        }
        if (topologyTemplate.getNode_templates() == null) {
            topologyTemplate.setNode_templates(new HashMap<>());
        }
    }

}