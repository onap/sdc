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

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.SubInterfaceConsolidationDataHandler;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.TranslationService;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ResourceTranslationNestedImpl extends ResourceTranslationBase {

    private static final String SUB_INTERFACE_COUNT = "count";
    protected static Logger log = LoggerFactory.getLogger(ResourceTranslationNestedImpl.class);

    @Override
    public void translate(TranslateTo translateTo) {
        TranslationContext context = translateTo.getContext();
        FileData nestedFileData = HeatToToscaUtil.getFileData(translateTo.getResource().getType(), context);
        if (nestedFileData == null) {
            log.warn("Nested File '{}' is not exist, therefore, the nested resource with the ID '{}' will be ignored "
                    + "in TOSCA translation", translateTo.getResource().getType(), translateTo.getResourceId());
            return;
        }
        String templateName = FileUtils.getFileWithoutExtention(translateTo.getResource().getType());
        String substitutionNodeTypeKey = HeatToToscaUtil.getNestedResourceTypePrefix(translateTo) + templateName;
        if (!context.getTranslatedServiceTemplates().containsKey(translateTo.getResource().getType())) {
            translateNestedHeat(translateTo, nestedFileData, templateName, substitutionNodeTypeKey);
        }
        ServiceTemplate substitutionServiceTemplate = context.getTranslatedServiceTemplates()
                .get(translateTo.getResource().getType());
        if (DataModelUtil.isNodeTemplateSectionMissingFromServiceTemplate(substitutionServiceTemplate)) {
            handleSubstitutionServiceTemplateWithoutNodeTemplates(templateName, translateTo);
            return;
        }
        NodeTemplate substitutionNodeTemplate = HeatToToscaUtil.createAbstractSubstitutionNodeTemplate(translateTo,
                templateName, substitutionNodeTypeKey);
        manageSubstitutionNodeTemplateConnectionPoint(translateTo, nestedFileData, substitutionNodeTemplate,
                substitutionNodeTypeKey);
        DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(),
                substitutionNodeTemplate);
        //Add nested node template id to consolidation data
        ConsolidationDataUtil.updateNestedNodeTemplateId(translateTo);
        //Gather consolidation data if the resource group represents a sub interface
        if (isResourceGroupSubInterface(substitutionNodeTypeKey)) {
            populateSubInterfaceTemplateConsolidationData(translateTo, substitutionNodeTemplate);
        }
    }

    private boolean isResourceGroupSubInterface(String substitutionNodeTypeKey) {
        return StringUtils.isNotBlank(substitutionNodeTypeKey)
                && substitutionNodeTypeKey.contains(ToscaNodeType.VLAN_SUB_INTERFACE_RESOURCE_TYPE_PREFIX);
    }

    private void translateNestedHeat(TranslateTo translateTo, FileData nestedFileData, String templateName,
                                     String substitutionNodeTypeKey) {
        TranslationContext context = translateTo.getContext();
        //substitution service template
        ServiceTemplate nestedSubstitutionServiceTemplate =
                createSubstitutionServiceTemplate(translateTo, nestedFileData, templateName);
        //global substitution service template
        ServiceTemplate globalSubstitutionServiceTemplate = HeatToToscaUtil
                .fetchGlobalSubstitutionServiceTemplate(translateTo.getServiceTemplate(), context);
        //substitution node type
        NodeType substitutionNodeType = new ToscaAnalyzerServiceImpl()
                .createInitSubstitutionNodeType(nestedSubstitutionServiceTemplate, ToscaNodeType.ABSTRACT_SUBSTITUTE);
        DataModelUtil.addNodeType(globalSubstitutionServiceTemplate, substitutionNodeTypeKey,
                substitutionNodeType);
        //substitution mapping
        HeatToToscaUtil.handleSubstitutionMapping(context, substitutionNodeTypeKey,
                nestedSubstitutionServiceTemplate, substitutionNodeType);
        //add new nested service template
        context.getTranslatedServiceTemplates().put(translateTo.getResource().getType(),
                nestedSubstitutionServiceTemplate);
    }

    private void populateSubInterfaceTemplateConsolidationData(TranslateTo translateTo,
                                                               NodeTemplate nodeTemplate) {

        SubInterfaceConsolidationDataHandler consolidationDataHandler =
                translateTo.getContext().getSubInterfaceConsolidationDataHandler();

        String translatedId = translateTo.getTranslatedId();
        Optional<String> subInterfaceNetworkRole =
                HeatToToscaUtil.getNetworkRoleFromSubInterfaceId(translateTo.getResource(), translateTo.getContext());
        subInterfaceNetworkRole.ifPresent(networkRole -> consolidationDataHandler.setNetworkRole(translateTo,
                translatedId, networkRole));

        consolidationDataHandler.setResourceGroupCount(translateTo, translatedId,
                getSubInterfaceCountFromResourceProperties(translateTo));

        if (CollectionUtils.isEmpty(nodeTemplate.getRequirements())) {
            return;
        }
        //Add connectivity to network in consolidation data based on resource group link requirements
        nodeTemplate.getRequirements().forEach((Map<String, RequirementAssignment> requirementMap) ->
                requirementMap.entrySet().stream()
                        .filter(requirementAssignmentEntry -> ToscaCapabilityType.NATIVE_NETWORK_LINKABLE
                                .equals(requirementAssignmentEntry.getValue().getCapability()))
                        .forEach(requirementAssignmentEntry ->
                                consolidationDataHandler.addNodesConnectedOut(translateTo,
                                        requirementAssignmentEntry.getValue().getNode(),
                                        requirementAssignmentEntry.getKey(),
                                        requirementAssignmentEntry.getValue())
                        )
        );
    }

    private Object getSubInterfaceCountFromResourceProperties(TranslateTo translateTo) {
        if (Objects.nonNull(translateTo.getHeatOrchestrationTemplate().getResources().get(translateTo
                .getResourceId()))) {
            Resource resource = translateTo.getHeatOrchestrationTemplate().getResources().get(translateTo
                    .getResourceId());
            if (HeatResourcesTypes.RESOURCE_GROUP_RESOURCE_TYPE.getHeatResource().equals(resource.getType())) {
                return resource.getProperties().get(SUB_INTERFACE_COUNT);
            } else if (HeatToToscaUtil.isYamlFile(resource.getType())) {
                return HeatConstants.DEFAULT_NESTED_HEAT_RESOURCE_COUNT;
            }
        }
        return null;
    }

    private void handleSubstitutionServiceTemplateWithoutNodeTemplates(String templateName,
                                                                       TranslateTo translateTo) {
        translateTo.getContext().addServiceTemplateWithoutNodeTemplates(templateName);
        translateTo.getContext()
                .addNestedNodeTemplateIdPointsToStWithoutNodeTemplates(translateTo.getTranslatedId());
        translateTo.getContext().getTranslatedServiceTemplates().remove(translateTo.getResource().getType());
    }

    private ServiceTemplate createSubstitutionServiceTemplate(TranslateTo translateTo,
                                                              FileData nestedFileData,
                                                              String templateName) {
        ServiceTemplate nestedSubstitutionServiceTemplate =
                HeatToToscaUtil.createInitSubstitutionServiceTemplate(templateName);
        translateTo.getContext().addNestedHeatFileName(ToscaUtil.getServiceTemplateFileName(templateName),
                        translateTo.getResource().getType());
        new TranslationService().translateHeatFile(nestedSubstitutionServiceTemplate, nestedFileData, translateTo
                .getContext());
        return nestedSubstitutionServiceTemplate;
    }


    private void manageSubstitutionNodeTemplateConnectionPoint(TranslateTo translateTo,
                                                               FileData nestedFileData,
                                                               NodeTemplate substitutionNodeTemplate,
                                                               String substitutionNodeTypeId) {
        ServiceTemplate globalSubstitutionTemplate =
                translateTo.getContext().getTranslatedServiceTemplates()
                        .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
        NodeType nodeType = globalSubstitutionTemplate.getNode_types().get(substitutionNodeTypeId);
        handlePortToNetConnections(translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
        handleSecurityRulesToPortConnections(translateTo, nestedFileData, substitutionNodeTemplate,
                nodeType);
        handleNovaToVolConnection(translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
        handleContrailV2VmInterfaceToNetworkConnection(translateTo, nestedFileData,
                substitutionNodeTemplate, nodeType);
        handleContrailPortToNetConnections(translateTo, nestedFileData, substitutionNodeTemplate,
                nodeType);
        handleVlanSubInterfaceToInterfaceConnections(translateTo, nestedFileData,
                substitutionNodeTemplate, nodeType);
    }

    private void handleVlanSubInterfaceToInterfaceConnections(TranslateTo translateTo,
                                                              FileData nestedFileData,
                                                              NodeTemplate substitutionNodeTemplate,
                                                              NodeType nodeType) {
        ContrailV2VlanToInterfaceResourceConnection linker =
                new ContrailV2VlanToInterfaceResourceConnection(this, translateTo, nestedFileData,
                        substitutionNodeTemplate, nodeType);
        linker.connect();
    }


    private void handleContrailV2VmInterfaceToNetworkConnection(TranslateTo translateTo,
                                                                FileData nestedFileData,
                                                                NodeTemplate substitutionNodeTemplate,
                                                                NodeType nodeType) {
        ContrailV2VmInterfaceToNetResourceConnection linker =
                new ContrailV2VmInterfaceToNetResourceConnection(this, translateTo, nestedFileData,
                        substitutionNodeTemplate, nodeType);
        linker.connect();
    }

    private void handleNovaToVolConnection(TranslateTo translateTo, FileData nestedFileData,
                                           NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
        NovaToVolResourceConnection linker =
                new NovaToVolResourceConnection(this, translateTo, nestedFileData, substitutionNodeTemplate,
                        nodeType);
        linker.connect();
    }

    private void handleSecurityRulesToPortConnections(TranslateTo translateTo,
                                                      FileData nestedFileData,
                                                      NodeTemplate substitutionNodeTemplate,
                                                      NodeType nodeType) {
        SecurityRulesToPortResourceConnection linker =
                new SecurityRulesToPortResourceConnection(this, translateTo, nestedFileData,
                        substitutionNodeTemplate, nodeType);
        linker.connect();
    }

    private void handlePortToNetConnections(TranslateTo translateTo, FileData nestedFileData,
                                            NodeTemplate substitutionNodeTemplate,
                                            NodeType nodeType) {
        PortToNetResourceConnection linker =
                new PortToNetResourceConnection(this, translateTo, nestedFileData, substitutionNodeTemplate,
                        nodeType);
        linker.connect();
    }

    private void handleContrailPortToNetConnections(TranslateTo translateTo, FileData nestedFileData,
                                                    NodeTemplate substitutionNodeTemplate,
                                                    NodeType nodeType) {
        ContrailPortToNetResourceConnection linker =
                new ContrailPortToNetResourceConnection(this, translateTo, nestedFileData,
                        substitutionNodeTemplate, nodeType);
        linker.connect();
    }

}
