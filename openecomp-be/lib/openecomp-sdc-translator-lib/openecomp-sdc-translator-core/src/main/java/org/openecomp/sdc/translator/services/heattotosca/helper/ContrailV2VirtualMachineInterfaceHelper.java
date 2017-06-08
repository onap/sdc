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

package org.openecomp.sdc.translator.services.heattotosca.helper;

import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationEntityType;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationBase;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ContrailV2VirtualMachineInterfaceHelper {
  static Logger logger =
      (Logger) LoggerFactory.getLogger(ContrailV2VirtualMachineInterfaceHelper.class);
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  /**
   * Connect Virtual Machine Interface node template to network node template in TOSCA.
   *
   * @param resourceTranslationImpl resource translation implemetation
   * @param translateTo             translated ro object
   * @param vmiNodeTemplate         Virtual Machine Interface node template
   */
  public void connectVmiToNetwork(ResourceTranslationBase resourceTranslationImpl,
                                  TranslateTo translateTo, NodeTemplate vmiNodeTemplate) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Object virtualNetworkRefs =
        translateTo.getResource().getProperties()
            .get(HeatConstants.VIRTUAL_NETWORK_REFS_PROPERTY_NAME);
    if (Objects.isNull(virtualNetworkRefs) || !(virtualNetworkRefs instanceof List)
        || ((List) virtualNetworkRefs).size() == 0) {
      return;
    }
    List<String> acceptableResourceTypes = Arrays
        .asList(HeatResourcesTypes.NEUTRON_NET_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource());

    if (((List) virtualNetworkRefs).size() > 1) {
      logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
          + translateTo.getResource().getType()
          + "' include '"
          + HeatConstants.VIRTUAL_NETWORK_REFS_PROPERTY_NAME
          + "' property with more than one network values, only "
          + "the first network will be connected, "
          + "all rest will be ignored in TOSCA translation.");
    }
    Object virtualNetworkRef = ((List) virtualNetworkRefs).get(0);

    Optional<String> networkResourceId =
        HeatToToscaUtil.extractContrailGetResourceAttachedHeatResourceId(virtualNetworkRef);
    if (networkResourceId.isPresent()) { // get_resource
      Resource networkResource = HeatToToscaUtil
          .getResource(translateTo.getHeatOrchestrationTemplate(), networkResourceId.get(),
              translateTo.getHeatFileName());
      if (acceptableResourceTypes.contains(networkResource.getType())) {
        Optional<String> resourceTranslatedId =
            resourceTranslationImpl.getResourceTranslatedId(translateTo.getHeatFileName(),
                translateTo.getHeatOrchestrationTemplate(), networkResourceId.get(),
                translateTo.getContext());

        if (resourceTranslatedId.isPresent()) {
          RequirementAssignment requirementAssignment = HeatToToscaUtil.addLinkReqFromPortToNetwork(
              vmiNodeTemplate, resourceTranslatedId.get());

          if (ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE
              .equals(vmiNodeTemplate.getType())) {
            ConsolidationDataUtil
                .updateNodesConnectedOut(translateTo, resourceTranslatedId.get(),
                    ConsolidationEntityType.PORT,
                    ToscaConstants.LINK_REQUIREMENT_ID, requirementAssignment);
          }
        }
      } else {
        logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
            + translateTo.getResource().getType()
            + "' include '" + HeatConstants.VIRTUAL_NETWORK_REFS_PROPERTY_NAME
            + "' property which is connect to "
            + "unsupported/incorrect resource with type '"
            + networkResource.getType()
            + "', therefore, this connection will be ignored in TOSCA translation.");
      }
    } else {
      Optional<AttachedResourceId> attachedResourceId = HeatToToscaUtil
          .extractAttachedResourceId(translateTo.getHeatFileName(),
              translateTo.getHeatOrchestrationTemplate(), translateTo.getContext(),
              virtualNetworkRef);
      if (attachedResourceId.isPresent() && attachedResourceId.get().isGetParam()
          && attachedResourceId.get().getEntityId() instanceof String) {
        TranslatedHeatResource translatedSharedResourceId =
            translateTo.getContext().getHeatSharedResourcesByParam()
                .get(attachedResourceId.get().getEntityId());
        if (Objects.nonNull(translatedSharedResourceId)
            && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
          RequirementAssignment requirementAssignment = HeatToToscaUtil.addLinkReqFromPortToNetwork(
              vmiNodeTemplate, translatedSharedResourceId.getTranslatedId());

          if (ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE
              .equals(vmiNodeTemplate.getType())) {
            ConsolidationDataUtil.updateNodesConnectedOut(translateTo, translatedSharedResourceId
                    .getTranslatedId(), ConsolidationEntityType.PORT,
                ToscaConstants.LINK_REQUIREMENT_ID,
                requirementAssignment);
          }
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  /**
   * Check if the input heat resource is Vlan sub interface resource
   *
   * @param resource heat resource to be checked
   * @return true - if input resource is valn sub interface resource flase - otherwise.
   */
  public boolean isVlanSubInterfaceResource(Resource resource) {

    if (resource.getType().equals(HeatResourcesTypes
        .CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource())
        && getVlanTagPropertyValue(resource).isPresent()) {
      return true;
    }

    return false;
  }

  private Optional<Object> getVlanTagPropertyValue(Resource resource) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Object vmiProperties = resource.getProperties()
        .get(HeatConstants.VMI_PROPERTIES_PROPERTY_NAME);
    if (vmiProperties != null && vmiProperties instanceof Map) {
      mdcDataDebugMessage.debugExitMessage(null, null);
      return Optional.ofNullable(((Map) vmiProperties)
          .get(HeatConstants.VMI_SUB_INTERFACE_VLAN_TAG_PROPERTY_NAME));
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.empty();
  }


}
