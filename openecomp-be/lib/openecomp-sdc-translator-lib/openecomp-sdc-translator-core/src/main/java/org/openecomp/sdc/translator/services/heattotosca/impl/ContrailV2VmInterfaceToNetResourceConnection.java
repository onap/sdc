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
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class ContrailV2VmInterfaceToNetResourceConnection extends PortToNetResourceConnection {

  public ContrailV2VmInterfaceToNetResourceConnection(
      ResourceTranslationBase resourceTranslationBase, TranslateTo translateTo,
      FileData nestedFileData, NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
    super(resourceTranslationBase, translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
  }

  @Override
  protected boolean isDesiredNodeTemplateType(NodeTemplate nodeTemplate) {
    return nodeTemplate.getType()
        .equals(ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE.getDisplayName());
  }

  @Override
  protected Optional<List<String>> getConnectorParamName(String heatResourceId,
                                    Resource heatResource,
                                    HeatOrchestrationTemplate nestedHeatOrchestrationTemplate) {
    List<String> networks = new ArrayList<>();
    Object virtualNetworkRefs = heatResource.getProperties().get("virtual_network_refs");
    if (Objects.isNull(virtualNetworkRefs) || !(virtualNetworkRefs instanceof List)
        || ((List) virtualNetworkRefs).size() == 0) {
      return Optional.empty();
    }
    if (((List) virtualNetworkRefs).size() > 1) {
      logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with nested heat file: '"
          + translateTo.getResource().getType()
          + "' has resource '" + heatResourceId + "' with type '"
          + HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource()
          + "' which include 'virtual_network_refs' property with more than one network values, "
          + "only the first network will be translated, all rest will be ignored in TOSCA "
          + "translation.");
    }
    Object virtualNetworkRef = ((List) virtualNetworkRefs).get(0);
    Optional<AttachedResourceId> network = HeatToToscaUtil
        .extractAttachedResourceId(nestedFileData.getFile(), nestedHeatOrchestrationTemplate,
            translateTo.getContext(), virtualNetworkRef);
    if (network.isPresent() && network.get().isGetParam()) {
      networks.add((String) network.get().getEntityId());
    }
    return Optional.of(networks);
  }

  @Override
  protected String getDesiredResourceType() {
    return HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource();
  }

  @Override
  protected void addRequirementToConnectResources(Map.Entry<String, RequirementDefinition> entry,
                                                  List<String> paramNames) {
    for (String paramName : paramNames) {
      Object paramValue = translateTo.getResource().getProperties().get(paramName);
      String contrailAttachedResourceId =
          HeatToToscaUtil.extractContrailGetResourceAttachedHeatResourceId(paramValue);
      Optional<String> node;
      if (contrailAttachedResourceId != null) { // contrail get resource
        node = ResourceTranslationBase.getResourceTranslatedId(translateTo.getHeatFileName(),
            translateTo.getHeatOrchestrationTemplate(), contrailAttachedResourceId,
            translateTo.getContext());
        if (node.isPresent()) {
          createRequirementAssignment(entry, node.get(), substitutionNodeTemplate);
        }
      } else {
        Optional<AttachedResourceId> attachedResourceId =
            HeatToToscaUtil.extractAttachedResourceId(translateTo, paramName);
        if (!attachedResourceId.isPresent()) {
          return;
        }
        AttachedResourceId resourceId = attachedResourceId.get();
        if (resourceId.isGetParam()) {
          TranslatedHeatResource shareResource =
              translateTo.getContext().getHeatSharedResourcesByParam()
                  .get(resourceId.getEntityId());
          if (Objects.nonNull(shareResource)
              && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
            createRequirementAssignment(entry, shareResource.getTranslatedId(),
                substitutionNodeTemplate);
          }
        }
      }
    }
  }

  @Override
  protected List<Predicate<RequirementDefinition>> getPredicatesListForConnectionPoints() {
    ArrayList<Predicate<RequirementDefinition>> predicates = new ArrayList<>();
    predicates.add(
        req -> req.getCapability().equals(ToscaCapabilityType.NETWORK_LINKABLE.getDisplayName())
            && req.getNode().equals(ToscaNodeType.ROOT.getDisplayName())
            && req.getRelationship().equals(ToscaRelationshipType.NETWORK_LINK_TO.getDisplayName())
    );
    return predicates;
  }
}
