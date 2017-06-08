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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class PortToNetResourceConnection extends ResourceConnectionUsingRequirementHelper {

  public PortToNetResourceConnection(ResourceTranslationBase resourceTranslationBase,
                                     TranslateTo translateTo, FileData nestedFileData,
                                     NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
    super(resourceTranslationBase, translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
  }

  @Override
  protected boolean isDesiredNodeTemplateType(NodeTemplate nodeTemplate) {
    return nodeTemplate.getType().equals(ToscaNodeType.NEUTRON_PORT);
  }

  @Override
  protected List<Predicate<RequirementDefinition>> getPredicatesListForConnectionPoints() {
    ArrayList<Predicate<RequirementDefinition>> predicates = new ArrayList<>();
    predicates.add(
        req -> req.getCapability().equals(ToscaCapabilityType.NATIVE_NETWORK_LINKABLE)
            && (req.getNode() == null || req.getNode().equals(ToscaNodeType.NATIVE_ROOT))
            && req.getRelationship().equals(
            ToscaRelationshipType.NATIVE_NETWORK_LINK_TO));
    return predicates;
  }

  @Override
  protected Optional<List<String>> getConnectorPropertyParamName(String heatResourceId,
                                                                 Resource heatResource,
                                                                 HeatOrchestrationTemplate
                                                                nestedHeatOrchestrationTemplate,
                                                                 String nestedHeatFileName) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Optional<AttachedResourceId> network = HeatToToscaUtil
        .extractAttachedResourceId(nestedHeatFileName, nestedHeatOrchestrationTemplate,
            translateTo.getContext(), heatResource.getProperties().get("network"));
    if (network.isPresent() && network.get().isGetParam()
        && network.get().getEntityId() instanceof String) {
      return Optional.of(Collections.singletonList((String) network.get().getEntityId()));
    } else {
      network = HeatToToscaUtil
          .extractAttachedResourceId(nestedHeatFileName, nestedHeatOrchestrationTemplate,
              translateTo.getContext(), heatResource.getProperties().get("network_id"));
      if (network.isPresent()
          && network.get().isGetParam()
          && network.get().getEntityId() instanceof String) {
        mdcDataDebugMessage.debugExitMessage(null, null);
        return Optional.of(Collections.singletonList((String) network.get().getEntityId()));
      } else {
        mdcDataDebugMessage.debugExitMessage(null, null);
        return Optional.empty();
      }
    }
  }

  @Override
  protected String getDesiredResourceType() {
    return HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource();
  }

  @Override
  protected void addRequirementToConnectResources(
      Map.Entry<String, RequirementDefinition> requirementDefinitionEntry,
      List<String> paramNames) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (paramNames == null || paramNames.isEmpty()) {
      return;
    }
    String paramName = paramNames.get(
        0); // port can connect to one network only and we are
    // expecting to have only one param(unlike security rules to port)
    Object paramValue = translateTo.getResource().getProperties().get(paramName);
    List<String> supportedNetworkTypes =
        Arrays.asList(HeatResourcesTypes.NEUTRON_NET_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource());

    addRequirementToConnectResource(requirementDefinitionEntry, paramName, paramValue,
        supportedNetworkTypes);

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
          + "' that connect port to network. Supported types are: '" + supportedTypes.toString()
          + "', therefore, this TOSCA requirement will not be connected.");

      mdcDataDebugMessage.debugExitMessage(null, null);
      return false;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return true;
  }


}
