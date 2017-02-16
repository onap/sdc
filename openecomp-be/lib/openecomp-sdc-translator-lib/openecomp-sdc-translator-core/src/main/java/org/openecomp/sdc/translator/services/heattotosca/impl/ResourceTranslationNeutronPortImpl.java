/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class ResourceTranslationNeutronPortImpl extends ResourceTranslationBase {

  @Override
  public void translate(TranslateTo translateTo) {

    NodeTemplate nodeTemplate = new NodeTemplate();
    nodeTemplate.setType(ToscaNodeType.NEUTRON_PORT.getDisplayName());

    nodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(translateTo.getResource().getProperties(),
            nodeTemplate.getProperties(), translateTo.getHeatFileName(),
            translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
            nodeTemplate, translateTo.getContext()));
    handleNetworkRequirement(translateTo, nodeTemplate);
    String resourceTranslatedId = handleSecurityRulesRequirement(translateTo);
    DataModelUtil
        .addNodeTemplate(translateTo.getServiceTemplate(), resourceTranslatedId, nodeTemplate);
  }

  private String handleSecurityRulesRequirement(TranslateTo translateTo) {
    String resourceTranslatedId = translateTo.getTranslatedId();
    Map<String, Object> properties = translateTo.getResource().getProperties();
    Optional<Object> securityGroups =
        Optional.ofNullable(properties.get(Constants.SECURITY_GROUPS_PROPERTY_NAME));
    if (securityGroups.isPresent() && securityGroups.get() instanceof List) {
      List securityGroupsList = (List) securityGroups.get();
      securityGroupsList.forEach(resourceValue -> {
        Optional<AttachedResourceId> securityGroupResourceId = HeatToToscaUtil
            .extractAttachedResourceId(translateTo.getHeatFileName(),
                translateTo.getHeatOrchestrationTemplate(), translateTo.getContext(),
                resourceValue);
        if (securityGroupResourceId.isPresent()) {
          handleSecurityGroupResourceId(translateTo, resourceTranslatedId,
              securityGroupResourceId.get());
        }
      });
    }
    return resourceTranslatedId;
  }

  private void handleSecurityGroupResourceId(TranslateTo translateTo, String resourceTranslatedId,
                                             AttachedResourceId securityGroupResourceId) {
    List<String> supportedSecurityGroupsTypes = Collections
        .singletonList(HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE.getHeatResource());
    if (securityGroupResourceId.isGetResource()) {
      handleGetResource(translateTo, resourceTranslatedId, securityGroupResourceId,
          supportedSecurityGroupsTypes);
    } else if (securityGroupResourceId.isGetParam()) {
      handleGetParam(translateTo, resourceTranslatedId, securityGroupResourceId,
          supportedSecurityGroupsTypes);
    }
  }

  private void handleGetParam(TranslateTo translateTo, String resourceTranslatedId,
                              AttachedResourceId securityGroupResourceId,
                              List<String> supportedSecurityGroupsTypes) {
    TranslatedHeatResource translatedSharedResourceId =
        translateTo.getContext().getHeatSharedResourcesByParam()
            .get(securityGroupResourceId.getEntityId());
    if (Objects.nonNull(translatedSharedResourceId)
        && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
      if (validateResourceTypeSupportedForReqCreation(translateTo, supportedSecurityGroupsTypes,
          translatedSharedResourceId.getHeatResource(), "security_groups")) {
        return;
      }
      final NodeTemplate securityGroupNodeTemplate = DataModelUtil
          .getNodeTemplate(translateTo.getServiceTemplate(),
              translatedSharedResourceId.getTranslatedId());
      RequirementAssignment requirement = new RequirementAssignment();
      requirement.setCapability(ToscaCapabilityType.ATTACHMENT.getDisplayName());
      requirement.setNode(resourceTranslatedId);
      requirement.setRelationship(ToscaRelationshipType.ATTACHES_TO.getDisplayName());
      DataModelUtil
          .addRequirementAssignment(securityGroupNodeTemplate, ToscaConstants.PORT_REQUIREMENT_ID,
              requirement);
    }
  }

  private void handleGetResource(TranslateTo translateTo, String resourceTranslatedId,
                                 AttachedResourceId securityGroupResourceId,
                                 List<String> supportedSecurityGroupsTypes) {
    String resourceId = (String) securityGroupResourceId.getEntityId();
    Resource securityGroupResource = HeatToToscaUtil
        .getResource(translateTo.getHeatOrchestrationTemplate(), resourceId,
            translateTo.getHeatFileName());
    Optional<String> securityGroupTranslatedId =
        ResourceTranslationFactory.getInstance(securityGroupResource)
            .translateResource(translateTo.getHeatFileName(), translateTo.getServiceTemplate(),
                translateTo.getHeatOrchestrationTemplate(), securityGroupResource, resourceId,
                translateTo.getContext());
    if (securityGroupTranslatedId.isPresent()) {
      if (validateResourceTypeSupportedForReqCreation(translateTo, supportedSecurityGroupsTypes,
          securityGroupResource, "security_groups")) {
        return;
      }
      final NodeTemplate securityGroupNodeTemplate = DataModelUtil
          .getNodeTemplate(translateTo.getServiceTemplate(), securityGroupTranslatedId.get());
      RequirementAssignment requirement = new RequirementAssignment();
      requirement.setCapability(ToscaCapabilityType.ATTACHMENT.getDisplayName());
      requirement.setNode(resourceTranslatedId);
      requirement.setRelationship(ToscaRelationshipType.ATTACHES_TO.getDisplayName());
      DataModelUtil
          .addRequirementAssignment(securityGroupNodeTemplate, ToscaConstants.PORT_REQUIREMENT_ID,
              requirement);
    }
  }

  private void handleNetworkRequirement(TranslateTo translateTo, NodeTemplate nodeTemplate) {
    Optional<AttachedResourceId> networkResourceId =
        HeatToToscaUtil.extractAttachedResourceId(translateTo, "network");
    if (networkResourceId.isPresent()) {
      AttachedResourceId attachedResourceId = networkResourceId.get();
      addRequirementAssignmentForNetworkResource(translateTo, nodeTemplate, attachedResourceId);
    } else {
      networkResourceId = HeatToToscaUtil.extractAttachedResourceId(translateTo, "network_id");
      if (networkResourceId.isPresent()) {
        AttachedResourceId attachedResourceId = networkResourceId.get();
        addRequirementAssignmentForNetworkResource(translateTo, nodeTemplate, attachedResourceId);
      }
    }
  }

  private void addRequirementAssignmentForNetworkResource(TranslateTo translateTo,
                                                          NodeTemplate nodeTemplate,
                                                          AttachedResourceId attachedResourceId) {
    String networkTranslatedId;
    List<String> supportedNetworkTypes =
        Arrays.asList(HeatResourcesTypes.NEUTRON_NET_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource());
    if (attachedResourceId.isGetResource()) {
      Resource networkHeatResource = translateTo.getHeatOrchestrationTemplate().getResources()
          .get(attachedResourceId.getEntityId());
      if (validateResourceTypeSupportedForReqCreation(translateTo, supportedNetworkTypes,
          networkHeatResource, "network'\\'network_id")) {
        return;
      }
      networkTranslatedId = (String) attachedResourceId.getTranslatedId();
      addRequirementAssignment(nodeTemplate, networkTranslatedId);
    } else if (attachedResourceId.isGetParam()) {
      TranslatedHeatResource translatedSharedResourceId =
          translateTo.getContext().getHeatSharedResourcesByParam()
              .get(attachedResourceId.getEntityId());
      if (Objects.nonNull(translatedSharedResourceId)
          && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
        if (validateResourceTypeSupportedForReqCreation(translateTo, supportedNetworkTypes,
            translatedSharedResourceId.getHeatResource(), "network'\\'network_id")) {
          return;
        }
        addRequirementAssignment(nodeTemplate, translatedSharedResourceId.getTranslatedId());
      }
    }
  }

  private void addRequirementAssignment(NodeTemplate nodeTemplate, String translatedId) {
    RequirementAssignment requirement = new RequirementAssignment();
    requirement.setCapability(ToscaCapabilityType.NETWORK_LINKABLE.getDisplayName());
    requirement.setRelationship(ToscaRelationshipType.NETWORK_LINK_TO.getDisplayName());
    requirement.setNode(translatedId);
    DataModelUtil
        .addRequirementAssignment(nodeTemplate, ToscaConstants.LINK_REQUIREMENT_ID, requirement);
  }


  private boolean validateResourceTypeSupportedForReqCreation(TranslateTo translateTo,
                                                              List<String> supportedTypes,
                                                              Resource heatResource,
                                                              final String propertyName) {
    if (!isResourceTypeValidForRequirement(heatResource, supportedTypes)) {
      logger.warn(
          "'" + propertyName + "' property of port resource('" + translateTo.getResourceId()
              + "') is pointing to a resource of type '" + heatResource.getType() + "' "
              + "which is not supported for this requirement. "
              + "Supported types are: " + supportedTypes.toString());
      return true;
    }
    return false;
  }

  private boolean isResourceTypeValidForRequirement(Resource networkHeatResource,
                                                    List<String> supportedNetworkTypes) {
    return Objects.nonNull(networkHeatResource)
        && supportedNetworkTypes.contains(networkHeatResource.getType());
  }

}
