/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.enrichment.impl.tosca;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaElementTypes;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.HIGH_AVAIL_MODE;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MANDATORY;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MAX_INSTANCES;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.MIN_INSTANCES;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.NFC_FUNCTION;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.NFC_NAMING_CODE;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.VFC_CODE;
import static org.openecomp.sdc.enrichment.impl.util.EnrichmentConstants.VM_TYPE_TAG;
import static org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType.NATIVE_NODE;
import static org.openecomp.sdc.tosca.datatypes.ToscaNodeType.MULTIDEPLOYMENTFLAVOR_NODE_TYPE;
import static org.openecomp.sdc.tosca.datatypes.ToscaNodeType.VFC_ABSTRACT_SUBSTITUTE;
import static org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType.NATIVE_DEPENDS_ON;
import static org.openecomp.sdc.tosca.services.ToscaConstants.SERVICE_TEMPLATE_FILTER_PROPERTY_NAME;
import static org.openecomp.sdc.translator.services.heattotosca.Constants.VNF_NODE_TEMPLATE_ID_SUFFIX;

public class AbstractSubstituteToscaEnricher {

    private ToscaAnalyzerService toscaAnalyzerService;
    private ComponentQuestionnaireData componentQuestionnaireData;


    public Map<String, List<ErrorMessage>> enrich(ToscaServiceModel toscaModel, String vspId, Version version) {
        componentQuestionnaireData = getComponentQuestionnaireData();
        toscaAnalyzerService = new ToscaAnalyzerServiceImpl();

        Map<String, Map<String, Object>> componentProperties =
                componentQuestionnaireData.getPropertiesfromCompQuestionnaire(vspId, version);

        final Map<String, List<String>> sourceToTargetDependencies = componentQuestionnaireData
                                                                             .populateDependencies(vspId, version,
                                                                                     componentQuestionnaireData
                                                                                      .getSourceToTargetComponent());
        Map<String, List<ErrorMessage>> errors = new HashMap<>();

        final ServiceTemplate serviceTemplate =
                toscaModel.getServiceTemplates().get(toscaModel.getEntryDefinitionServiceTemplate());

        if (serviceTemplate == null) {
            return errors;
        }

        final TopologyTemplate topologyTemplate = serviceTemplate.getTopology_template();
        if (topologyTemplate == null) {
            return errors;
        }

        final Map<String, NodeTemplate> nodeTemplates = serviceTemplate.getTopology_template().getNode_templates();
        if (nodeTemplates == null) {
            return errors;
        }

        final Map<String, List<String>> componentDisplayNameToNodeTemplateIds =
                populateAllNodeTemplateIdForComponent(nodeTemplates, serviceTemplate, toscaModel);

        nodeTemplates.keySet().forEach(nodeTemplateId -> {
            final NodeTemplate nodeTemplate =
                    toscaAnalyzerService.getNodeTemplateById(serviceTemplate, nodeTemplateId).orElse(null);

            if (nodeTemplate != null && toscaAnalyzerService.isTypeOf(nodeTemplate, VFC_ABSTRACT_SUBSTITUTE,
                    serviceTemplate, toscaModel)) {

                String componentDisplayName = getComponentDisplayName(nodeTemplateId, nodeTemplate);

                enrichProperties(nodeTemplate, componentDisplayName, componentProperties);

                enrichRequirements(sourceToTargetDependencies, componentDisplayName, nodeTemplate,
                        componentDisplayNameToNodeTemplateIds, serviceTemplate, toscaModel);
            }
        });
        return errors;
    }

    private void enrichProperties(NodeTemplate nodeTemplate, String componentDisplayName,
            Map<String, Map<String, Object>> componentProperties) {
        setProperty(nodeTemplate, VM_TYPE_TAG, componentDisplayName);

        if (componentProperties != null && componentProperties.containsKey(componentDisplayName)) {
            final String mandatory =
                    getValueFromQuestionnaireDetails(componentProperties, componentDisplayName, MANDATORY);

            boolean isServiceTemplateFilterNotExists = false;
            if (!StringUtils.isEmpty(mandatory)) {
                Map<String, Object> innerProps = (Map<String, Object>) nodeTemplate.getProperties()
                                                                   .get(SERVICE_TEMPLATE_FILTER_PROPERTY_NAME);

                if (innerProps == null) {
                    innerProps = new HashMap<>();
                    isServiceTemplateFilterNotExists = true;
                }
                Optional<Boolean> mandatoryValue = getValue(mandatory);
                if (mandatoryValue.isPresent()) {
                    innerProps.put(MANDATORY, mandatoryValue.get());
                }

                if (isServiceTemplateFilterNotExists) {
                    nodeTemplate.getProperties().put(SERVICE_TEMPLATE_FILTER_PROPERTY_NAME, innerProps);
                }
            }

            setProperty(nodeTemplate, HIGH_AVAIL_MODE,
                    getValueFromQuestionnaireDetails(componentProperties, componentDisplayName,
                            HIGH_AVAIL_MODE));

            setProperty(nodeTemplate, NFC_NAMING_CODE,
                    getValueFromQuestionnaireDetails(componentProperties, componentDisplayName,
                            NFC_NAMING_CODE));

            setProperty(nodeTemplate, VFC_CODE,
                    getValueFromQuestionnaireDetails(componentProperties, componentDisplayName, VFC_CODE));

            setProperty(nodeTemplate, NFC_FUNCTION,
                    getValueFromQuestionnaireDetails(componentProperties, componentDisplayName, NFC_FUNCTION));

            if (componentProperties.get(componentDisplayName).get(MIN_INSTANCES) != null) {
                nodeTemplate.getProperties().put(MIN_INSTANCES,
                        componentProperties.get(componentDisplayName).get(MIN_INSTANCES));
            }

            if (componentProperties.get(componentDisplayName).get(MAX_INSTANCES) != null) {
                nodeTemplate.getProperties().put(MAX_INSTANCES,
                        componentProperties.get(componentDisplayName).get(MAX_INSTANCES));
            }
        }
    }

    private Map<String, List<String>> populateAllNodeTemplateIdForComponent(Map<String, NodeTemplate> nodeTemplates,
                                                                                   ServiceTemplate serviceTemplate,
                                                                                   ToscaServiceModel toscaModel) {


        Map<String, List<String>> componentDisplayNameToNodeTempalteIds = new HashMap<>();

        //set dependency target
        nodeTemplates.keySet().forEach(nodeTemplateId -> {
            final NodeTemplate nodeTemplate =
                    toscaAnalyzerService.getNodeTemplateById(serviceTemplate, nodeTemplateId).orElse(null);

            if (nodeTemplate != null &&
                    toscaAnalyzerService.isTypeOf(nodeTemplate, VFC_ABSTRACT_SUBSTITUTE, serviceTemplate, toscaModel)) {

                String componentDisplayName = getComponentDisplayName(nodeTemplateId, nodeTemplate);

                if (componentDisplayNameToNodeTempalteIds.containsKey(componentDisplayName)) {
                    componentDisplayNameToNodeTempalteIds.get(componentDisplayName).add(nodeTemplateId);
                } else {
                    List<String> nodeTemplateIds = new ArrayList<>();
                    nodeTemplateIds.add(nodeTemplateId);
                    componentDisplayNameToNodeTempalteIds.put(componentDisplayName, nodeTemplateIds);
                }

            }
        });

        return componentDisplayNameToNodeTempalteIds;
    }

    private void enrichRequirements(Map<String, List<String>> sourceToTargetDependencies, String componentDisplayName,
                                           NodeTemplate nodeTemplate,
                                           Map<String, List<String>> componentDisplayNameToNodeTempalteIds,
                                           ServiceTemplate serviceTemplate, ToscaServiceModel toscaServiceModel) {
        final List<String> targets = sourceToTargetDependencies.get(componentDisplayName);
        if (CollectionUtils.isEmpty(targets)) {
            return;
        }

        for (String target : targets) {
            List<String> targetNodeTemplateIds = componentDisplayNameToNodeTempalteIds.get(target);
            if (CollectionUtils.isEmpty(targetNodeTemplateIds)) {
                continue;
            }
            for (String targetNodeTemplateId : targetNodeTemplateIds) {
                Optional<String> dependencyRequirementKey =
                        getDependencyRequirementKey(serviceTemplate, componentDisplayName, nodeTemplate,
                                toscaServiceModel);
                if (dependencyRequirementKey.isPresent()) {
                    RequirementAssignment requirementAssignment = new RequirementAssignment();
                    requirementAssignment.setCapability(NATIVE_NODE);
                    requirementAssignment.setRelationship(NATIVE_DEPENDS_ON);
                    requirementAssignment.setNode(targetNodeTemplateId);
                    DataModelUtil.addRequirementAssignment(nodeTemplate, dependencyRequirementKey.get(),
                            requirementAssignment);
                }
            }
        }
    }

    private Optional<String> getDependencyRequirementKey(ServiceTemplate serviceTemplate, String componentDisplayName,
                                                                NodeTemplate nodeTemplate,
                                                                ToscaServiceModel toscaServiceModel) {
        String nodeType = nodeTemplate.getType();
        NodeType flatNodeType = (NodeType) toscaAnalyzerService.getFlatEntity(ToscaElementTypes.NODE_TYPE, nodeType,
                serviceTemplate, toscaServiceModel).getFlatEntity();
        List<Map<String, RequirementDefinition>> flatNodeTypeRequirements = flatNodeType.getRequirements();
        if (Objects.isNull(flatNodeTypeRequirements)) {
            return Optional.empty();
        }
        for (Map<String, RequirementDefinition> requirementDefinitionMap : flatNodeTypeRequirements) {
            String requirementKey = requirementDefinitionMap.keySet().iterator().next();
            String expectedKey = ToscaConstants.DEPENDS_ON_REQUIREMENT_ID + "_" + componentDisplayName;
            if (requirementKey.equals(expectedKey)) {
                return Optional.of(requirementKey);
            }
        }
        return Optional.empty();
    }

    private String getComponentDisplayName(String nodeTemplateId, NodeTemplate nodeTemplate) {
        String componentDisplayName;
        final String type = nodeTemplate.getType();
        if (MULTIDEPLOYMENTFLAVOR_NODE_TYPE.equals(type)) {
            componentDisplayName = nodeTemplateId.substring(0, nodeTemplateId.lastIndexOf(VNF_NODE_TEMPLATE_ID_SUFFIX));
        } else {
            String vmType = DataModelUtil.getNamespaceSuffix(type);
            final String[] removedSuffix = vmType.split("_\\d+");
            componentDisplayName = removedSuffix[0];
        }
        return componentDisplayName;
    }

    private String getValueFromQuestionnaireDetails(Map<String, Map<String, Object>> componentTypetoParams,
                                                           String componentDisplayName, String propertyName) {
        return (String) componentTypetoParams.get(componentDisplayName).get(propertyName);
    }

    private void setProperty(NodeTemplate nodeTemplate, String key, String value) {
        if (!StringUtils.isEmpty(value)) {
            //YamlUtil throws IllegalStateException("duplicate key: " + key) if key is already present.
            // So first removing and then populating same key with new updated value
            nodeTemplate.getProperties().remove(key);
            nodeTemplate.getProperties().put(key, value);
        }
    }

    private Optional<Boolean> getValue(String value) {
        switch (value) {
            case "YES":
                return Optional.of(Boolean.TRUE);
            case "NO":
                return Optional.of(Boolean.FALSE);
            default:
                return Optional.empty();
        }
    }

    private ComponentQuestionnaireData getComponentQuestionnaireData() {
        if (componentQuestionnaireData == null) {
            componentQuestionnaireData = new ComponentQuestionnaireData();
        }
        return componentQuestionnaireData;
    }
}
