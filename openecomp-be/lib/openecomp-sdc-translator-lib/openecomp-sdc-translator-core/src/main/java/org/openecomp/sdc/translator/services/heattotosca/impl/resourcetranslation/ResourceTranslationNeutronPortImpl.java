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

import static org.openecomp.sdc.translator.services.heattotosca.Constants.SECURITY_GROUPS_PROPERTY_NAME;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_UNSUPPORTED_RESOURCE_REQUIREMENT_CONNECTION;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationEntityType;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.helper.ResourceTranslationNeutronPortHelper;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

public class ResourceTranslationNeutronPortImpl extends ResourceTranslationBase {

    @Override
    public void translate(TranslateTo translateTo) {
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setType(ToscaNodeType.NEUTRON_PORT);
        nodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
            .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(), translateTo.getResourceId(),
                translateTo.getResource().getProperties(), nodeTemplate.getProperties(), translateTo.getHeatFileName(),
                translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(), nodeTemplate, translateTo.getContext()));
        new ResourceTranslationNeutronPortHelper().setAdditionalProperties(nodeTemplate.getProperties());
        handleNetworkRequirement(translateTo, nodeTemplate);
        String resourceTranslatedId = handleSecurityRulesRequirement(translateTo);
        DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), resourceTranslatedId, nodeTemplate);
    }

    private String handleSecurityRulesRequirement(TranslateTo translateTo) {
        String resourceTranslatedId = translateTo.getTranslatedId();
        Map<String, Object> properties = translateTo.getResource().getProperties();
        Optional<Object> securityGroups = Optional.ofNullable(properties.get(SECURITY_GROUPS_PROPERTY_NAME));
        if (securityGroups.isPresent() && securityGroups.get() instanceof List) {
            List securityGroupsList = (List) securityGroups.get();
            securityGroupsList.forEach(resourceValue -> {
                Optional<AttachedResourceId> securityGroupResourceId = HeatToToscaUtil
                    .extractAttachedResourceId(translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(), translateTo.getContext(),
                        resourceValue);
                securityGroupResourceId
                    .ifPresent(attachedResourceId -> handleSecurityGroupResourceId(translateTo, resourceTranslatedId, attachedResourceId));
            });
        }
        return resourceTranslatedId;
    }

    private void handleSecurityGroupResourceId(TranslateTo translateTo, String resourceTranslatedId, AttachedResourceId securityGroupResourceId) {
        List<String> supportedSecurityGroupsTypes = Collections
            .singletonList(HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE.getHeatResource());
        if (securityGroupResourceId.isGetResource()) {
            handleGetResource(translateTo, resourceTranslatedId, securityGroupResourceId, supportedSecurityGroupsTypes);
        } else if (securityGroupResourceId.isGetParam()) {
            handleGetParam(translateTo, resourceTranslatedId, securityGroupResourceId, supportedSecurityGroupsTypes);
        }
    }

    private void handleGetParam(TranslateTo translateTo, String resourceTranslatedId, AttachedResourceId securityGroupResourceId,
                                List<String> supportedSecurityGroupsTypes) {
        if (!(securityGroupResourceId.getEntityId() instanceof String)) {
            return;
        }
        TranslatedHeatResource translatedSharedResourceId = translateTo.getContext().getHeatSharedResourcesByParam()
            .get(securityGroupResourceId.getEntityId());
        if (Objects.nonNull(translatedSharedResourceId) && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
            if (validateResourceTypeSupportedForReqCreation(translateTo, supportedSecurityGroupsTypes, translatedSharedResourceId.getHeatResource(),
                SECURITY_GROUPS_PROPERTY_NAME)) {
                return;
            }
            final NodeTemplate securityGroupNodeTemplate = DataModelUtil
                .getNodeTemplate(translateTo.getServiceTemplate(), translatedSharedResourceId.getTranslatedId());
            RequirementAssignment requirement = new RequirementAssignment();
            requirement.setCapability(ToscaCapabilityType.NATIVE_ATTACHMENT);
            requirement.setNode(resourceTranslatedId);
            requirement.setRelationship(ToscaRelationshipType.ATTACHES_TO);
            DataModelUtil.addRequirementAssignment(securityGroupNodeTemplate, ToscaConstants.PORT_REQUIREMENT_ID, requirement);
            ConsolidationDataUtil.updateNodesConnectedIn(translateTo, translatedSharedResourceId.getTranslatedId(), ConsolidationEntityType.PORT,
                translateTo.getResourceId(), ToscaConstants.PORT_REQUIREMENT_ID, requirement);
        }
    }

    private void handleGetResource(TranslateTo translateTo, String resourceTranslatedId, AttachedResourceId securityGroupResourceId,
                                   List<String> supportedSecurityGroupsTypes) {
        String resourceId = (String) securityGroupResourceId.getEntityId();
        Resource securityGroupResource = HeatToToscaUtil
            .getResource(translateTo.getHeatOrchestrationTemplate(), resourceId, translateTo.getHeatFileName());
        Optional<String> securityGroupTranslatedId = ResourceTranslationFactory.getInstance(securityGroupResource)
            .translateResource(translateTo.getHeatFileName(), translateTo.getServiceTemplate(), translateTo.getHeatOrchestrationTemplate(),
                securityGroupResource, resourceId, translateTo.getContext());
        if (!securityGroupTranslatedId.isPresent()) {
            return;
        }
        if (validateResourceTypeSupportedForReqCreation(translateTo, supportedSecurityGroupsTypes, securityGroupResource,
            SECURITY_GROUPS_PROPERTY_NAME)) {
            return;
        }
        final NodeTemplate securityGroupNodeTemplate = DataModelUtil
            .getNodeTemplate(translateTo.getServiceTemplate(), securityGroupTranslatedId.get());
        RequirementAssignment requirement = new RequirementAssignment();
        requirement.setCapability(ToscaCapabilityType.NATIVE_ATTACHMENT);
        requirement.setNode(resourceTranslatedId);
        requirement.setRelationship(ToscaRelationshipType.ATTACHES_TO);
        DataModelUtil.addRequirementAssignment(securityGroupNodeTemplate, ToscaConstants.PORT_REQUIREMENT_ID, requirement);
        ConsolidationDataUtil
            .updateNodesConnectedIn(translateTo, securityGroupTranslatedId.get(), ConsolidationEntityType.PORT, translateTo.getResourceId(),
                ToscaConstants.PORT_REQUIREMENT_ID, requirement);
    }

    private void handleNetworkRequirement(TranslateTo translateTo, NodeTemplate nodeTemplate) {
        Optional<AttachedResourceId> networkResourceId = HeatToToscaUtil.extractAttachedResourceId(translateTo, HeatConstants.NETWORK_PROPERTY_NAME);
        if (networkResourceId.isPresent()) {
            AttachedResourceId attachedResourceId = networkResourceId.get();
            addRequirementAssignmentForNetworkResource(translateTo, nodeTemplate, attachedResourceId);
        } else {
            networkResourceId = HeatToToscaUtil.extractAttachedResourceId(translateTo, HeatConstants.NETWORK_ID_PROPERTY_NAME);
            if (networkResourceId.isPresent()) {
                AttachedResourceId attachedResourceId = networkResourceId.get();
                addRequirementAssignmentForNetworkResource(translateTo, nodeTemplate, attachedResourceId);
            }
        }
    }

    private void addRequirementAssignmentForNetworkResource(TranslateTo translateTo, NodeTemplate nodeTemplate,
                                                            AttachedResourceId attachedResourceId) {
        if (attachedResourceId.isGetResource()) {
            addLinkRequirementForGetResource(translateTo, nodeTemplate, attachedResourceId);
        } else if (attachedResourceId.isGetParam() && attachedResourceId.getEntityId() instanceof String) {
            addLinkRequirementForGetParam(translateTo, nodeTemplate, attachedResourceId);
        }
    }

    private void addLinkRequirementForGetParam(TranslateTo translateTo, NodeTemplate nodeTemplate, AttachedResourceId attachedResourceId) {
        TranslatedHeatResource translatedSharedResourceId = translateTo.getContext().getHeatSharedResourcesByParam()
            .get(attachedResourceId.getEntityId());
        if (Objects.nonNull(translatedSharedResourceId) && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
            if (validateResourceTypeSupportedForReqCreation(translateTo, getSupportedNetworkResourceTypes(),
                translatedSharedResourceId.getHeatResource(), "network'\\'network_id")) {
                return;
            }
            RequirementAssignment requirementAssignment = HeatToToscaUtil
                .addLinkReqFromPortToNetwork(nodeTemplate, translatedSharedResourceId.getTranslatedId());
            ConsolidationDataUtil.updateNodesConnectedOut(translateTo, translatedSharedResourceId.getTranslatedId(), ConsolidationEntityType.PORT,
                ToscaConstants.LINK_REQUIREMENT_ID, requirementAssignment);
        }
    }

    private void addLinkRequirementForGetResource(TranslateTo translateTo, NodeTemplate nodeTemplate, AttachedResourceId attachedResourceId) {
        String networkTranslatedId;
        Resource networkHeatResource = translateTo.getHeatOrchestrationTemplate().getResources().get(attachedResourceId.getEntityId());
        if (validateResourceTypeSupportedForReqCreation(translateTo, getSupportedNetworkResourceTypes(), networkHeatResource,
            "network'\\'network_id")) {
            return;
        }
        networkTranslatedId = (String) attachedResourceId.getTranslatedId();
        RequirementAssignment requirementAssignment = HeatToToscaUtil.addLinkReqFromPortToNetwork(nodeTemplate, networkTranslatedId);
        ConsolidationDataUtil
            .updateNodesConnectedOut(translateTo, networkTranslatedId, ConsolidationEntityType.PORT, ToscaConstants.LINK_REQUIREMENT_ID,
                requirementAssignment);
    }

    private boolean validateResourceTypeSupportedForReqCreation(TranslateTo translateTo, List<String> supportedTypes, Resource heatResource,
                                                                final String propertyName) {
        if (isUnsupportedResourceType(heatResource, supportedTypes)) {
            String supportedResourceTypes = supportedTypes.toString();
            logger.warn(LOG_UNSUPPORTED_RESOURCE_REQUIREMENT_CONNECTION, propertyName, translateTo.getResourceId(), heatResource.getType(),
                supportedResourceTypes);
            return true;
        }
        return false;
    }

    private List<String> getSupportedNetworkResourceTypes() {
        return ImmutableList.of(HeatResourcesTypes.NEUTRON_NET_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource());
    }
}
