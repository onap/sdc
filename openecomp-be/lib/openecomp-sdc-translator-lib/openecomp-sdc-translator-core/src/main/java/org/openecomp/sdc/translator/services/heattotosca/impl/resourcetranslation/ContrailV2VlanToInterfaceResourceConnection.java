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

import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_MULTIPLE_INTERFACE_VALUES_NESTED;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_UNSUPPORTED_VMI_VLAN_SUB_INTERFACE_REQUIREMENT_CONNECTION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.helper.ContrailV2VirtualMachineInterfaceHelper;

public class ContrailV2VlanToInterfaceResourceConnection extends ResourceConnectionUsingRequirementHelper {

    ContrailV2VlanToInterfaceResourceConnection(ResourceTranslationBase resourceTranslationBase, TranslateTo translateTo, FileData nestedFileData,
                                                NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
        super(resourceTranslationBase, translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
    }

    @Override
    protected boolean isDesiredNodeTemplateType(NodeTemplate nodeTemplate) {
        return nodeTemplate.getType().equals(ToscaNodeType.CONTRAILV2_VLAN_SUB_INTERFACE);
    }

    @Override
    protected List<Predicate<RequirementDefinition>> getPredicatesListForConnectionPoints() {
        ArrayList<Predicate<RequirementDefinition>> predicates = new ArrayList<>();
        predicates.add(req -> req.getCapability().equals(ToscaCapabilityType.NATIVE_NETWORK_BINDABLE) && (req.getNode() == null || req.getNode()
            .equals(ToscaNodeType.NETWORK_PORT)) && req.getRelationship().equals(ToscaRelationshipType.NATIVE_NETWORK_BINDS_TO));
        return predicates;
    }

    @Override
    protected Optional<List<String>> getConnectorPropertyParamName(String heatResourceId, Resource heatResource,
                                                                   HeatOrchestrationTemplate nestedHeatOrchestrationTemplate,
                                                                   String nestedHeatFileName) {
        List<String> interfaces = new ArrayList<>();
        Object interfaceRefs = heatResource.getProperties().get(HeatConstants.VMI_REFS_PROPERTY_NAME);
        if (Objects.isNull(interfaceRefs) || !(interfaceRefs instanceof List) || ((List) interfaceRefs).isEmpty()) {
            return Optional.empty();
        }
        if (((List) interfaceRefs).size() > 1) {
            logger.warn(LOG_MULTIPLE_INTERFACE_VALUES_NESTED, translateTo.getResourceId(), translateTo.getResource().getType(), heatResourceId,
                HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(), HeatConstants.VMI_REFS_PROPERTY_NAME);
        }
        Object interfaceRef = ((List) interfaceRefs).get(0);
        Optional<AttachedResourceId> attachedInterfaceResource = HeatToToscaUtil
            .extractAttachedResourceId(nestedFileData.getFile(), nestedHeatOrchestrationTemplate, translateTo.getContext(), interfaceRef);
        if (attachedInterfaceResource.isPresent() && attachedInterfaceResource.get().isGetParam() && attachedInterfaceResource.get()
            .getEntityId() instanceof String) {
            interfaces.add((String) attachedInterfaceResource.get().getEntityId());
        }
        return Optional.of(interfaces);
    }

    @Override
    protected String getDesiredResourceType() {
        return HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource();
    }

    @Override
    protected void addRequirementToConnectResources(Map.Entry<String, RequirementDefinition> requirementDefinitionEntry, List<String> paramNames) {
        if (paramNames == null || paramNames.isEmpty()) {
            return;
        }
        for (String paramName : paramNames) {
            Object paramValue = translateTo.getResource().getProperties().get(paramName);
            List<String> supportedInterfaceTypes = Arrays.asList(HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource(),
                HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource());
            addRequirementToConnectResource(requirementDefinitionEntry, paramName, paramValue, supportedInterfaceTypes);
        }
    }

    @Override
    boolean validateResourceTypeSupportedForReqCreation(String nestedResourceId, final String nestedPropertyName, String connectionPointId,
                                                        Resource connectedResource, List<String> supportedTypes) {
        if (resourceTranslationBase.isUnsupportedResourceType(connectedResource, supportedTypes) || (new ContrailV2VirtualMachineInterfaceHelper()
            .isVlanSubInterfaceResource(connectedResource))) {
            logger.warn(LOG_UNSUPPORTED_VMI_VLAN_SUB_INTERFACE_REQUIREMENT_CONNECTION, nestedResourceId, nestedPropertyName,
                getLogMessageSuffixForConnectedResource(connectedResource), connectedResource.getType(), connectionPointId,
                supportedTypes.toString());
            return false;
        }
        return true;
    }

    private String getLogMessageSuffixForConnectedResource(Resource connectedResource) {
        return new ContrailV2VirtualMachineInterfaceHelper().isVlanSubInterfaceResource(connectedResource) ? "Vlan Sub interface " : "";
    }
}
