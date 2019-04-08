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

import org.apache.commons.collections.MapUtils;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.sdc.tosca.services.DataModelUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ToscaSolConverterPnf extends AbstractToscaSolConverter {

    private static final String PNF_EXT_CP_TYPE = "tosca.nodes.nfv.PnfExtCp";
    private static final String EXT_CP_TYPE = "org.openecomp.resource.cp.v2.extCP";
    private static final String LAYER_PROTOCOLS = "layer_protocols";
    private static final String IP_V4 = "ipv4";
    private static final String IP_V6 = "ipv6";
    private static final String IP_VERSION = "ip_version";
    private static final String ASSIGNMENT_METHOD = "assingment_method";
    private static final String DHCP = "dhcp";

    /**
     * For PNF the node templates are converted ETSI node types to ecomp node types
     * All other data i.e. inputs, substitution mappings and outputs are simply dropped at this stage. The equivalent
     * ecomp data will be added when the vsp is imported into the catalog.
     * @param serviceTemplate
     * @param readerService
     */
    @Override
    public void convertTopologyTemplate(ServiceTemplate serviceTemplate, ServiceTemplateReaderService readerService) {
        convertNodeTemplatesToEcompTypes(serviceTemplate, readerService);
        addEmptyNodeTemplatesIfNoneDefined(serviceTemplate);
    }

    /**
     * PNF only has nfv.PNF and nfv.PnfExtCp types defined in ETSI SOL001 v2.5.1.
     * - The PNF is mapped to the outer Abstract PNF container in ecomp model and hence nfv.PNF is dropped here.
     * - nfv.PnfExtCp is mapped to ecomp v2.extCp type.
     *
     * @param serviceTemplate
     * @param readerService
     */
    private void convertNodeTemplatesToEcompTypes(ServiceTemplate serviceTemplate,
                                                  ServiceTemplateReaderService readerService) {
        Map<String, Object> nodeTemplates = readerService.getNodeTemplates();
        if (MapUtils.isEmpty(nodeTemplates)) {
            return;
        }

        for (Map.Entry<String, Object> nodeTemplateEntry : nodeTemplates.entrySet()) {
            Map<String, Object> inputNodeTemplate = (Map<String, Object>) nodeTemplateEntry.getValue();
            if (PNF_EXT_CP_TYPE.equals((String) inputNodeTemplate.get("type"))) {
                NodeTemplate nodeTemplate = convertToEcompConnectionPointNodeType(inputNodeTemplate);
                DataModelUtil.addNodeTemplate(serviceTemplate, nodeTemplateEntry.getKey(), nodeTemplate);
            }
        }
        addEmptyNodeTemplatesIfNoneDefined(serviceTemplate);
    }


    /**
     * Converts from the ETSI PnfExtCp node type to ecomp v2.extCP node type
     * The following properties are mapped
     * - layer_protocols is mapped to ip_requirements if it contains the values ipv4 and/or ipv6
     * <p>
     * All other data e.g. remaining properties, requirements, capabilities are
     * not mapped over to ecomp equivalent
     *
     * @param pnfExtCp
     * @return ecomp v2.extCP node type
     */
    private NodeTemplate convertToEcompConnectionPointNodeType(Map<String, Object> pnfExtCp) {
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setType(EXT_CP_TYPE);
        Map<String, Object> properties = (Map<String, Object>) pnfExtCp.get("properties");
        for (Map.Entry<String, Object> property : properties.entrySet()) {
            final String propertyName = property.getKey();
            if (LAYER_PROTOCOLS.equals(propertyName)) {
                List<Map<String, Object>> ipRequirements = convertToIpRequirementsProperty((List<String>) property.getValue());
                if (isNotEmpty(ipRequirements)) {
                    Map<String, Object> convertedProperties = new HashMap<>();
                    convertedProperties.put("ip_requirements", ipRequirements);
                    nodeTemplate.setProperties(convertedProperties);
                }
            }
        }
        return nodeTemplate;
    }

    private List<Map<String, Object>> convertToIpRequirementsProperty(List<String> layerProtocols) {
        return layerProtocols.stream()
                .filter(layerProtocol -> IP_V4.equals(layerProtocol) || IP_V6.equals(layerProtocol))
                .map(this::createIpPropertyElement)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createIpPropertyElement(String ipVersion) {
        final int version = ipVersion.equals(IP_V4) ? 4 : 6;
        Map<String, Object> result = new HashMap<>();
        result.put(IP_VERSION, version);
        result.put(ASSIGNMENT_METHOD, DHCP);
        return result;
    }

    private boolean isNotEmpty(List<?> list) {
        return !list.isEmpty();
    }

    private void addEmptyNodeTemplatesIfNoneDefined(ServiceTemplate serviceTemplate) {
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