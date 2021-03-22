/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.generator.core.utils;

import static org.openecomp.sdc.tosca.services.DataModelUtil.addSubstitutionNodeTypeRequirements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.onap.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.services.ToscaExtensionYamlUtil;
import org.openecomp.sdc.tosca.datatypes.ToscaElementTypes;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;

/**
 * The type Generator utils.
 */
public class GeneratorUtils {

    //TODO : Read from configuration
    private static final List<String> SUPPORTED_CAPABILITIES = Arrays.asList("host", "os", "endpoint", "scalable");
    private static final List<String> SUPPORTED_REQUIREMENTS = Collections.singletonList("link");
    private GeneratorUtils() {
        // prevent instantiation
    }

    /**
     * Add service template to tosca service model.
     *
     * @param toscaServiceModel the tosca service model
     * @param serviceTemplate   the service template
     */
    public static void addServiceTemplateToToscaServiceModel(ToscaServiceModel toscaServiceModel, ServiceTemplate serviceTemplate) {
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        Map<String, ServiceTemplate> serviceTemplates = toscaServiceModel.getServiceTemplates();
        if (!serviceTemplates.containsKey(serviceTemplateFileName)) {
            ToscaUtil.addServiceTemplateToMapWithKeyFileName(serviceTemplates, serviceTemplate);
        }
        toscaServiceModel.setServiceTemplates(serviceTemplates);
    }

    /**
     * Gets substitution node type exposed connection points.
     *
     * @param substitutionNodeType        the substitution node type
     * @param substitutionServiceTemplate the substitution service template
     * @param toscaServiceModel           the tosca service model
     * @return the substitution node type exposed connection points
     */
    public static Map<String, Map<String, List<String>>> getSubstitutionNodeTypeExposedConnectionPoints(NodeType substitutionNodeType,
                                                                                                        ServiceTemplate substitutionServiceTemplate,
                                                                                                        ToscaServiceModel toscaServiceModel) {
        Map<String, NodeTemplate> nodeTemplates = substitutionServiceTemplate.getTopology_template().getNode_templates();
        String nodeTemplateId;
        NodeTemplate nodeTemplate;
        String nodeType;
        Map<String, Map<String, List<String>>> substitutionMapping = new HashMap<>();
        if (nodeTemplates == null) {
            return substitutionMapping;
        }
        try {
            Map<String, List<String>> capabilitySubstitutionMapping = new HashMap<>();
            Map<String, List<String>> requirementSubstitutionMapping = new HashMap<>();
            substitutionMapping.put("capability", capabilitySubstitutionMapping);
            substitutionMapping.put("requirement", requirementSubstitutionMapping);
            List<Map<String, RequirementDefinition>> nodeTypeRequirementsDefinition;
            Map<String, RequirementAssignment> nodeTemplateRequirementsAssignment;
            List<Map<String, RequirementDefinition>> exposedRequirementsDefinition;
            Map<String, Map<String, RequirementAssignment>> fullFilledRequirementsDefinition = new HashMap<>();
            Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition = new HashMap<>();
            Map<String, CapabilityDefinition> exposedCapabilitiesDefinition;
            ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
            for (Map.Entry<String, NodeTemplate> entry : nodeTemplates.entrySet()) {
                nodeTemplateId = entry.getKey();
                nodeTemplate = entry.getValue();
                nodeType = nodeTemplate.getType();
                NodeType flatNodeType = (NodeType) toscaAnalyzerService
                    .getFlatEntity(ToscaElementTypes.NODE_TYPE, nodeType, substitutionServiceTemplate, toscaServiceModel).getFlatEntity();
                // get requirements
                nodeTypeRequirementsDefinition = getNodeTypeRequirements(flatNodeType, nodeTemplateId, substitutionServiceTemplate,
                    requirementSubstitutionMapping);
                nodeTemplateRequirementsAssignment = DataModelUtil.getNodeTemplateRequirements(nodeTemplate);
                fullFilledRequirementsDefinition.put(nodeTemplateId, nodeTemplateRequirementsAssignment);
                //set substitution node type requirements
                exposedRequirementsDefinition = toscaAnalyzerService
                    .calculateExposedRequirements(nodeTypeRequirementsDefinition, nodeTemplateRequirementsAssignment);
                //Filter unsupported requirements
                Iterator<Map<String, RequirementDefinition>> iterator = exposedRequirementsDefinition.iterator();
                while (iterator.hasNext()) {
                    Map<String, RequirementDefinition> requirementDefinitionMap = iterator.next();
                    for (Map.Entry<String, RequirementDefinition> requirementDefinitionEntry : requirementDefinitionMap.entrySet()) {
                        String requirementKey = requirementDefinitionEntry.getKey();
                        if (!SUPPORTED_REQUIREMENTS.contains(requirementKey)) {
                            iterator.remove();
                        }
                    }
                }
                addSubstitutionNodeTypeRequirements(substitutionNodeType, exposedRequirementsDefinition, nodeTemplateId);
                //get capabilities
                addNodeTypeCapabilitiesToSubMapping(nodeTypeCapabilitiesDefinition, capabilitySubstitutionMapping, nodeType, nodeTemplateId,
                    substitutionServiceTemplate, toscaServiceModel);
            }
            exposedCapabilitiesDefinition = toscaAnalyzerService
                .calculateExposedCapabilities(nodeTypeCapabilitiesDefinition, fullFilledRequirementsDefinition);
            //Filter unsupported capabilities
            Iterator<Map.Entry<String, CapabilityDefinition>> iterator = exposedCapabilitiesDefinition.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CapabilityDefinition> capabilityDefinitionEntry = iterator.next();
                //Expected Capability is of the format <capabilityId>_<componentName>
                String capabilityKey = capabilityDefinitionEntry.getKey().split("_")[0];
                if (!SUPPORTED_CAPABILITIES.contains(capabilityKey)) {
                    iterator.remove();
                }
            }
            DataModelUtil.setNodeTypeCapabilitiesDef(substitutionNodeType, exposedCapabilitiesDefinition);
        } catch (Exception ex) {
            return null;
        }
        return substitutionMapping;
    }

    /**
     * Gets node type requirements.
     *
     * @param flatNodeType                   the flat node type
     * @param templateName                   the template name
     * @param serviceTemplate                the service template
     * @param requirementSubstitutionMapping the requirement substitution mapping
     * @return the node type requirements
     */
    public static List<Map<String, RequirementDefinition>> getNodeTypeRequirements(NodeType flatNodeType, String templateName,
                                                                                   ServiceTemplate serviceTemplate,
                                                                                   Map<String, List<String>> requirementSubstitutionMapping) {
        List<Map<String, RequirementDefinition>> requirementList = new ArrayList<>();
        List<String> requirementMapping;
        if (flatNodeType.getRequirements() != null) {
            for (Map<String, RequirementDefinition> requirementMap : flatNodeType.getRequirements()) {
                for (Map.Entry<String, RequirementDefinition> requirementNodeEntry : requirementMap.entrySet()) {
                    ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
                    RequirementDefinition requirementNodeEntryValue = toscaExtensionYamlUtil
                        .yamlToObject(toscaExtensionYamlUtil.objectToYaml(requirementNodeEntry.getValue()), RequirementDefinition.class);
                    if (requirementNodeEntryValue.getOccurrences() == null) {
                        requirementNodeEntryValue.setOccurrences(new Object[]{1, 1});
                    }
                    Map<String, RequirementDefinition> requirementDef = new HashMap<>();
                    requirementDef.put(requirementNodeEntry.getKey(), requirementNodeEntryValue);
                    DataModelUtil.addRequirementToList(requirementList, requirementDef);
                    requirementMapping = new ArrayList<>();
                    requirementMapping.add(templateName);
                    requirementMapping.add(requirementNodeEntry.getKey());
                    requirementSubstitutionMapping.put(requirementNodeEntry.getKey() + "_" + templateName, requirementMapping);
                    if (requirementNodeEntryValue.getNode() == null) {
                        requirementNodeEntryValue.setOccurrences(new Object[]{1, 1});
                    }
                }
            }
        }
        return requirementList;
    }

    private static void addNodeTypeCapabilitiesToSubMapping(Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition,
                                                            Map<String, List<String>> capabilitySubstitutionMapping, String type, String templateName,
                                                            ServiceTemplate substitutionServiceTemplate, ToscaServiceModel toscaServiceModel) {
        ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
        NodeType flatNodeType = (NodeType) toscaAnalyzerService
            .getFlatEntity(ToscaElementTypes.NODE_TYPE, type, substitutionServiceTemplate, toscaServiceModel).getFlatEntity();
        String capabilityKey;
        List<String> capabilityMapping;
        if (flatNodeType.getCapabilities() != null) {
            for (Map.Entry<String, CapabilityDefinition> capabilityNodeEntry : flatNodeType.getCapabilities().entrySet()) {
                capabilityKey = capabilityNodeEntry.getKey() + "_" + templateName;
                nodeTypeCapabilitiesDefinition.put(capabilityKey, capabilityNodeEntry.getValue().clone());
                capabilityMapping = new ArrayList<>();
                capabilityMapping.add(templateName);
                capabilityMapping.add(capabilityNodeEntry.getKey());
                capabilitySubstitutionMapping.put(capabilityKey, capabilityMapping);
            }
        }
    }
}
