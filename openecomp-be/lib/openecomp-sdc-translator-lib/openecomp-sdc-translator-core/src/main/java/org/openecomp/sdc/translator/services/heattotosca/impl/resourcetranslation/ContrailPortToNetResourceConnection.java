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

import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_UNSUPPORTED_CONTRAIL_PORT_NETWORK_REQUIREMENT_CONNECTION;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

public class ContrailPortToNetResourceConnection extends ResourceConnectionUsingRequirementHelper {

    ContrailPortToNetResourceConnection(ResourceTranslationBase resourceTranslationBase, TranslateTo translateTo, FileData nestedFileData,
                                        NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
        super(resourceTranslationBase, translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
    }

    @Override
    protected boolean isDesiredNodeTemplateType(NodeTemplate nodeTemplate) {
        ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
        ToscaServiceModel toscaServiceModel = HeatToToscaUtil.getToscaServiceModel(translateTo.getContext());
        return toscaAnalyzerService.isTypeOf(nodeTemplate, ToscaNodeType.CONTRAIL_ABSTRACT_SUBSTITUTE,
            translateTo.getContext().getTranslatedServiceTemplates().get(translateTo.getResource().getType()), toscaServiceModel);
    }

    @Override
    protected List<Predicate<RequirementDefinition>> getPredicatesListForConnectionPoints() {
        ArrayList<Predicate<RequirementDefinition>> predicates = new ArrayList<>();
        predicates.add(req -> req.getCapability().equals(ToscaCapabilityType.NATIVE_NETWORK_LINKABLE) && (req.getNode() == null || req.getNode()
            .equals(ToscaNodeType.NATIVE_ROOT)) && req.getRelationship().equals(ToscaRelationshipType.NATIVE_NETWORK_LINK_TO));
        return predicates;
    }

    @Override
    protected Optional<List<String>> getConnectorPropertyParamName(String heatResourceId, Resource heatResource,
                                                                   HeatOrchestrationTemplate nestedHeatOrchestrationTemplate,
                                                                   String nestedHeatFileName) {
        Object interfaceListProperty = heatResource.getProperties().get(HeatConstants.INTERFACE_LIST_PROPERTY_NAME);
        if (interfaceListProperty == null) {
            return Optional.empty();
        }
        List<String> paramsList = new ArrayList<>();
        if (interfaceListProperty instanceof List) {
            return getConnectorPropertyParamNameFromList(nestedHeatOrchestrationTemplate, nestedHeatFileName, (List) interfaceListProperty,
                paramsList);
        } else if (interfaceListProperty instanceof Map) {
            return getConnectorPropertyParamNameFromMap(nestedHeatOrchestrationTemplate, nestedHeatFileName, (Map) interfaceListProperty, paramsList);
        }
        return Optional.empty();
    }

    private Optional<List<String>> getConnectorPropertyParamNameFromList(HeatOrchestrationTemplate nestedHeatOrchestrationTemplate,
                                                                         String nestedHeatFileName, List interfaceListProperty,
                                                                         List<String> paramsList) {
        for (Object interfaceEntry : interfaceListProperty) {
            if (interfaceEntry instanceof Map) {
                Optional<AttachedResourceId> attachedVirtualNetwork = HeatToToscaUtil
                    .extractAttachedResourceId(nestedHeatFileName, nestedHeatOrchestrationTemplate, translateTo.getContext(),
                        ((Map) interfaceEntry).get(HeatConstants.VIRTUAL_NETWORK_PROPERTY_NAME));
                if (attachedVirtualNetwork.isPresent() && attachedVirtualNetwork.get().isGetParam() && attachedVirtualNetwork.get()
                    .getEntityId() instanceof String) {
                    paramsList.add((String) attachedVirtualNetwork.get().getEntityId());
                }
            }
        }
        return Optional.of(paramsList);
    }

    private Optional<List<String>> getConnectorPropertyParamNameFromMap(HeatOrchestrationTemplate nestedHeatOrchestrationTemplate,
                                                                        String nestedHeatFileName, Map interfaceListProperty,
                                                                        List<String> paramsList) {
        Optional<AttachedResourceId> attachedVirtualNetwork = HeatToToscaUtil
            .extractAttachedResourceId(nestedHeatFileName, nestedHeatOrchestrationTemplate, translateTo.getContext(),
                interfaceListProperty.get(HeatConstants.VIRTUAL_NETWORK_PROPERTY_NAME));
        if (attachedVirtualNetwork.isPresent() && attachedVirtualNetwork.get().isGetParam() && attachedVirtualNetwork.get()
            .getEntityId() instanceof String) {
            paramsList.add((String) attachedVirtualNetwork.get().getEntityId());
            return Optional.of(paramsList);
        }
        return Optional.empty();
    }

    @Override
    protected String getDesiredResourceType() {
        return HeatResourcesTypes.CONTRAIL_SERVICE_INSTANCE.getHeatResource();
    }

    @Override
    protected void addRequirementToConnectResources(Map.Entry<String, RequirementDefinition> requirementDefinitionEntry, List<String> paramNames) {
        if (paramNames == null || paramNames.isEmpty()) {
            return;
        }
        Integer index = Integer.valueOf(requirementDefinitionEntry.getKey().substring("link_port_".length()).substring(0, 1));
        String paramName = paramNames.get(
            index); // port can connect to one network only and we are

        // expecting to get only one param(unlike security rules to port)
        Object paramValue = translateTo.getResource().getProperties().get(paramName);
        List<String> supportedNetworkTypes = ImmutableList.of(HeatResourcesTypes.NEUTRON_NET_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource());
        addRequirementToConnectResource(requirementDefinitionEntry, paramName, paramValue, supportedNetworkTypes);
    }

    @Override
    boolean validateResourceTypeSupportedForReqCreation(String nestedResourceId, final String nestedPropertyName, String connectionPointId,
                                                        Resource connectedResource, List<String> supportedTypes) {
        if (resourceTranslationBase.isUnsupportedResourceType(connectedResource, supportedTypes)) {
            logger
                .warn(LOG_UNSUPPORTED_CONTRAIL_PORT_NETWORK_REQUIREMENT_CONNECTION, nestedResourceId, nestedPropertyName, connectedResource.getType(),
                    connectionPointId, supportedTypes.toString());
            return false;
        }
        return true;
    }
}
