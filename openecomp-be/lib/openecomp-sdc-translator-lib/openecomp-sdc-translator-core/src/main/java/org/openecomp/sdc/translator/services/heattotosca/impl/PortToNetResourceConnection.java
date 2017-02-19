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

import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class PortToNetResourceConnection extends NovaAndPortResourceConnectionHelper {

  public PortToNetResourceConnection(ResourceTranslationBase resourceTranslationBase,
                                     TranslateTo translateTo, FileData nestedFileData,
                                     NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
    super(resourceTranslationBase, translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
  }

  @Override
  protected boolean isDesiredNodeTemplateType(NodeTemplate nodeTemplate) {
    return nodeTemplate.getType().equals(ToscaNodeType.NEUTRON_PORT.getDisplayName());
  }

  @Override
  protected List<Predicate<RequirementDefinition>> getPredicatesListForConnectionPoints() {
    ArrayList<Predicate<RequirementDefinition>> predicates = new ArrayList<>();
    predicates.add(
        req -> req.getCapability().equals(ToscaCapabilityType.NETWORK_LINKABLE.getDisplayName())
            && req.getNode().equals(ToscaNodeType.ROOT.getDisplayName())
            && req.getRelationship().equals(
                ToscaRelationshipType.NETWORK_LINK_TO.getDisplayName()));
    return predicates;
  }

  @Override
  protected Optional<List<String>> getConnectorParamName(String heatResourceId,
                                                         Resource heatResource,
                                                         HeatOrchestrationTemplate
                                                         nestedHeatOrchestrationTemplate) {
    Optional<AttachedResourceId> network = HeatToToscaUtil
        .extractAttachedResourceId(nestedFileData.getFile(), nestedHeatOrchestrationTemplate,
            translateTo.getContext(), heatResource.getProperties().get("network"));
    if (network.isPresent() && network.get().isGetParam()) {
      return Optional.of(Collections.singletonList((String) network.get().getEntityId()));
    } else {
      network = HeatToToscaUtil
          .extractAttachedResourceId(nestedFileData.getFile(), nestedHeatOrchestrationTemplate,
              translateTo.getContext(), heatResource.getProperties().get("network_id"));
      if (network.isPresent() && network.get().isGetParam()) {
        return Optional.of(Collections.singletonList((String) network.get().getEntityId()));
      } else {
        return Optional.empty();
      }
    }
  }

  @Override
  protected String getDesiredResourceType() {
    return HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource();
  }

  @Override
  protected String getTranslatedResourceIdFromSubstitutionMapping(
      ServiceTemplate nestedServiceTemplate, Map.Entry<String, RequirementDefinition> entry) {
    List<String> substitutionMapping =
        nestedServiceTemplate.getTopology_template().getSubstitution_mappings().getRequirements()
            .get(entry.getKey());
    return substitutionMapping.get(0);
  }

  @Override
  protected void addRequirementToConnectResources(Map.Entry<String, RequirementDefinition> entry,
                                                  List<String> paramNames) {
    String paramName = paramNames.get(
        0); // port can connect to one network only and
    // we are expecting to have only one param(unlike security rules to port)
    Object paramValue = translateTo.getResource().getProperties().get(paramName);
    if (paramValue == null) {
      logger.warn(
          "Nested resource '" + translateTo.getResourceId() + "' is not including property '"
              + paramName + "' with value for the nested heat file, therefore, '" + entry.getKey()
              + "' TOSCA requirement will not be connected.");
      return;
    }
    List<String> supportedNetworkTypes =
        Arrays.asList(HeatResourcesTypes.NEUTRON_NET_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource());

    Optional<String> targetTranslatedNodeId =
        getConnectionTargetNodeUsingGetResourceFunc(entry, paramName, paramValue,
            supportedNetworkTypes);
    if (targetTranslatedNodeId.isPresent()) {
      createRequirementAssignment(entry, targetTranslatedNodeId.get(), substitutionNodeTemplate);
    } else {
      targetTranslatedNodeId =
          getConnectionTargetNodeUsingGetParam(entry, paramName, supportedNetworkTypes);
      if (targetTranslatedNodeId.isPresent()) {
        createRequirementAssignment(entry, targetTranslatedNodeId.get(), substitutionNodeTemplate);
      }
    }
  }

  private boolean validateResourceTypeSupportedForReqCreation(String sourceResourceId,
                                                              final String sourcePropertyName,
                                                              String sourceReqId,
                                                              Resource targetResource,
                                                              List<String> supportedTypes) {
    if (!resourceTranslationBase.isResourceTypeSupported(targetResource, supportedTypes)) {
      logger.warn("Nested resource '" + sourceResourceId + "' property '" + sourcePropertyName
          + "' is pointing to a resource with type '" + targetResource.getType()
          + "' which is not supported for requirement '" + sourceReqId
          + "' that connect port to network. \nSupported types are: '" + supportedTypes.toString()
          + "', therefore, this TOSCA requirement will not be connected.");
      return false;
    }
    return true;
  }

  private Optional<String> getConnectionTargetNodeUsingGetParam(
      Map.Entry<String, RequirementDefinition> requirementDefinitionEntry, String paramName,
      List<String> supportedTargetNodeTypes) {
    Optional<AttachedResourceId> attachedResourceId =
        HeatToToscaUtil.extractAttachedResourceId(translateTo, paramName);
    if (!attachedResourceId.isPresent()) {
      return Optional.empty();
    }
    AttachedResourceId resourceId = attachedResourceId.get();
    if (resourceId.isGetParam()) {
      TranslatedHeatResource shareResource =
          translateTo.getContext().getHeatSharedResourcesByParam().get(resourceId.getEntityId());
      if (Objects.nonNull(shareResource)
          && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
        if (validateResourceTypeSupportedForReqCreation(translateTo.getResourceId(), paramName,
            requirementDefinitionEntry.getKey(), shareResource.getHeatResource(),
            supportedTargetNodeTypes)) {
          return Optional.of(shareResource.getTranslatedId());
        }
      }
    }

    return Optional.empty();
  }

  private Optional<String> getConnectionTargetNodeUsingGetResourceFunc(
      Map.Entry<String, RequirementDefinition> requirementDefinitionEntry, String paramName,
      Object paramValue, List<String> supportedTargetNodeTypes) {
    String getResourceAttachedResourceId =
        HeatToToscaUtil.extractContrailGetResourceAttachedHeatResourceId(paramValue);
    if (getResourceAttachedResourceId != null) { // get resource
      Resource resource = translateTo.getHeatOrchestrationTemplate().getResources()
          .get(getResourceAttachedResourceId);
      if (validateResourceTypeSupportedForReqCreation(translateTo.getResourceId(), paramName,
          requirementDefinitionEntry.getKey(), resource, supportedTargetNodeTypes)) {
        return ResourceTranslationBase.getResourceTranslatedId(translateTo.getHeatFileName(),
            translateTo.getHeatOrchestrationTemplate(), getResourceAttachedResourceId,
            translateTo.getContext());
      }
    }

    return Optional.empty();
  }
}
