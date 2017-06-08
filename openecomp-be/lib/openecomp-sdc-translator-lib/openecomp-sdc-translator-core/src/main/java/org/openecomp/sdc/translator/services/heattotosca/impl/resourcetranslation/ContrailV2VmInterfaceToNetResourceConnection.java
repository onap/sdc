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

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import org.openecomp.sdc.common.utils.CommonUtil;
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
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class ContrailV2VmInterfaceToNetResourceConnection
    extends ResourceConnectionUsingRequirementHelper {

  public ContrailV2VmInterfaceToNetResourceConnection(
      ResourceTranslationBase resourceTranslationBase, TranslateTo translateTo,
      FileData nestedFileData, NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
    super(resourceTranslationBase, translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
  }

  @Override
  protected boolean isDesiredNodeTemplateType(NodeTemplate nodeTemplate) {
    return (nodeTemplate.getType()
        .equals(ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE)
        || nodeTemplate.getType()
        .equals(ToscaNodeType.CONTRAILV2_VLAN_SUB_INTERFACE));
  }

  @Override
  protected List<Predicate<RequirementDefinition>> getPredicatesListForConnectionPoints() {
    ArrayList<Predicate<RequirementDefinition>> predicates = new ArrayList<>();
    predicates.add(
        req -> req.getCapability().equals(ToscaCapabilityType.NATIVE_NETWORK_LINKABLE)
            && (req.getNode() == null || req.getNode().equals(ToscaNodeType.NATIVE_ROOT))
            && req.getRelationship()
            .equals(ToscaRelationshipType.NATIVE_NETWORK_LINK_TO));
    return predicates;
  }

  @Override
  protected Optional<List<String>> getConnectorPropertyParamName(String heatResourceId,
                                                                 Resource heatResource,
                                                                 HeatOrchestrationTemplate
                                                               nestedHeatOrchestrationTemplate,
                                                                 String nestedHeatFileName) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

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
    if (network.isPresent() && network.get().isGetParam()
        && network.get().getEntityId() instanceof String) {
      networks.add((String) network.get().getEntityId());
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.of(networks);
  }

  @Override
  protected String getDesiredResourceType() {
    return HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource();
  }

  @Override
  protected void addRequirementToConnectResources(
      Map.Entry<String, RequirementDefinition> requirementDefinitionEntry,
      List<String> paramNames) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (paramNames == null || paramNames.isEmpty()) {
      return;
    }
    for (String paramName : paramNames) {
      Object paramValue = translateTo.getResource().getProperties().get(paramName);
      List<String> supportedNetworkTypes =
          Arrays.asList(HeatResourcesTypes.NEUTRON_NET_RESOURCE_TYPE.getHeatResource(),
              HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource());

      addRequirementToConnectResource(requirementDefinitionEntry, paramName, paramValue,
          supportedNetworkTypes);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  @Override
  boolean validateResourceTypeSupportedForReqCreation(String nestedResourceId,
                                                      final String nestedPropertyName,
                                                      String connectionPointId,
                                                      Resource connectedResource,
                                                      List<String> supportedTypes) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (!resourceTranslationBase.isResourceTypeSupported(connectedResource, supportedTypes)) {
      logger.warn("Nested resource '" + nestedResourceId + "' property '" + nestedPropertyName
          + "' is pointing to a resource with type '" + connectedResource.getType()
          + "' which is not supported for requirement '" + connectionPointId
          + "' that connect virtual machine interface to network. Supported types are: '"
          + supportedTypes.toString()
          + "', therefore, this TOSCA requirement will not be connected.");

      mdcDataDebugMessage.debugExitMessage(null, null);
      return false;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return true;
  }
}
