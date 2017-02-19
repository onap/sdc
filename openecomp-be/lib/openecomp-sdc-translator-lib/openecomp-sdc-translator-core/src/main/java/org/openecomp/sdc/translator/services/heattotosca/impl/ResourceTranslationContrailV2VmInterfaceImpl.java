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
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class ResourceTranslationContrailV2VmInterfaceImpl extends ResourceTranslationBase {
  protected static Logger logger =
      LoggerFactory.getLogger(ResourceTranslationContrailV2VmInterfaceImpl.class);

  @Override
  protected void translate(TranslateTo translateTo) {
    NodeTemplate nodeTemplate = new NodeTemplate();
    nodeTemplate.setType(ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE.getDisplayName());

    nodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(translateTo.getResource().getProperties(),
            nodeTemplate.getProperties(), translateTo.getHeatFileName(),
            translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
            nodeTemplate, translateTo.getContext()));
    handleNetworkRequirement(translateTo, nodeTemplate);
    Optional<String> resourceTranslatedId = getResourceTranslatedId(translateTo.getHeatFileName(),
        translateTo.getHeatOrchestrationTemplate(), translateTo.getResourceId(),
        translateTo.getContext());
    if (resourceTranslatedId.isPresent()) {
      DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), resourceTranslatedId.get(),
          nodeTemplate);
    }
  }

  private void handleNetworkRequirement(TranslateTo translateTo, NodeTemplate nodeTemplate) {
    Object virtualNetworkRefs =
        translateTo.getResource().getProperties().get("virtual_network_refs");
    if (Objects.isNull(virtualNetworkRefs) || !(virtualNetworkRefs instanceof List)
        || ((List) virtualNetworkRefs).size() == 0) {
      return;
    }
    List<String> acceptableResourceTypes = Arrays
        .asList(HeatResourcesTypes.NEUTRON_NET_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource());
    List virtualNetworkRefList = (List) virtualNetworkRefs;
    if (virtualNetworkRefList.size() > 1) {
      logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
          + translateTo.getResource().getType()
          + "' include 'virtual_network_refs' property with more than one network values,"
              + " only the first network will be translated, "
              + "all rest will be ignored in TOSCA translation.");
    }
    Object virtualNetworkRef = virtualNetworkRefList.get(0);
    String networkResourceId =
        HeatToToscaUtil.extractContrailGetResourceAttachedHeatResourceId(virtualNetworkRef);
    if (Objects.nonNull(networkResourceId)) { // get_resource
      Resource networkResource = HeatToToscaUtil
          .getResource(translateTo.getHeatOrchestrationTemplate(), networkResourceId,
              translateTo.getHeatFileName());
      if (acceptableResourceTypes.contains(networkResource.getType())) {
        Optional<String> resourceTranslatedId =
            getResourceTranslatedId(translateTo.getHeatFileName(),
                translateTo.getHeatOrchestrationTemplate(), networkResourceId,
                translateTo.getContext());
        if (resourceTranslatedId.isPresent()) {
          addLinkReqFromPortToNetwork(nodeTemplate, resourceTranslatedId.get());
        }
      } else {
        logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
            + translateTo.getResource().getType()
            + "' include 'virtual_network_refs' property which is connect"
            +   " to unsupported/incorrect resource with type '"
            + networkResource.getType()
            + "', therefore, this connection will be ignored in TOSCA translation.");
      }
    } else {
      Optional<AttachedResourceId> attachedResourceId = HeatToToscaUtil
          .extractAttachedResourceId(translateTo.getHeatFileName(),
              translateTo.getHeatOrchestrationTemplate(), translateTo.getContext(),
              virtualNetworkRef);
      if (attachedResourceId.isPresent() && attachedResourceId.get().isGetParam()) {
        TranslatedHeatResource translatedSharedResourceId =
            translateTo.getContext().getHeatSharedResourcesByParam()
                .get(attachedResourceId.get().getEntityId());
        if (Objects.nonNull(translatedSharedResourceId)
            && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
          addLinkReqFromPortToNetwork(nodeTemplate, translatedSharedResourceId.getTranslatedId());
        }
      }
    }


  }

}
