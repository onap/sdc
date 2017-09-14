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

import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.helper.ContrailV2VirtualMachineInterfaceHelper;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


public class ResourceTranslationContrailV2VlanSubInterfaceImpl extends ResourceTranslationBase {
  protected static Logger logger =
      (Logger) LoggerFactory.getLogger(ResourceTranslationContrailV2VlanSubInterfaceImpl.class);

  @Override
  protected void translate(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    NodeTemplate nodeTemplate = new NodeTemplate();
    nodeTemplate.setType(ToscaNodeType.CONTRAILV2_VLAN_SUB_INTERFACE);

    nodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(),translateTo.
            getResourceId(),translateTo.getResource().getProperties(),
            nodeTemplate.getProperties(), translateTo.getHeatFileName(),
            translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
            nodeTemplate, translateTo.getContext()));

    new ContrailV2VirtualMachineInterfaceHelper()
        .connectVmiToNetwork(this, translateTo, nodeTemplate);
    connectSubInterfaceToInterface(translateTo, nodeTemplate);
    DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(),
        nodeTemplate);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  //connection to shared interface is not supported
  private void connectSubInterfaceToInterface(TranslateTo translateTo,
                                              NodeTemplate vlanSubInterfaceNodeTemplate) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Object interfaceRefs =
        translateTo.getResource().getProperties().get(HeatConstants.VMI_REFS_PROPERTY_NAME);
    if (Objects.isNull(interfaceRefs) || !(interfaceRefs instanceof List)
        || ((List) interfaceRefs).size() == 0) {
      return;
    }
    List<String> acceptableResourceTypes = Arrays
        .asList(HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE
                .getHeatResource(),
            HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource());
    if (((List) interfaceRefs).size() > 1) {
      logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
          + translateTo.getResource().getType()
          + "' include '" + HeatConstants.VMI_REFS_PROPERTY_NAME
          + "' property with more than one interface values, only "
          + "the first interface will be connected, "
          + "all rest will be ignored in TOSCA translation.");
    }
    Object interfaceRef = ((List) interfaceRefs).get(0);

    Optional<String> interfaceResourceId =
        HeatToToscaUtil.extractContrailGetResourceAttachedHeatResourceId(interfaceRef);
    if (interfaceResourceId.isPresent()) { // get_resource
      Resource interfaceResource = HeatToToscaUtil
          .getResource(translateTo.getHeatOrchestrationTemplate(), interfaceResourceId.get(),
              translateTo.getHeatFileName());

      if (acceptableResourceTypes.contains(interfaceResource.getType())
          && !(new ContrailV2VirtualMachineInterfaceHelper()
          .isVlanSubInterfaceResource(interfaceResource))) {
        Optional<String> interfaceResourceTranslatedId =
            getResourceTranslatedId(translateTo.getHeatFileName(),
                translateTo.getHeatOrchestrationTemplate(), interfaceResourceId.get(),
                translateTo.getContext());
        interfaceResourceTranslatedId.ifPresent(interfaceResourceTranslatedIdVal -> HeatToToscaUtil
            .addBindingReqFromSubInterfaceToInterface(vlanSubInterfaceNodeTemplate,
                interfaceResourceTranslatedIdVal));
      } else {
        logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
            + translateTo.getResource().getType()
            + "' include '" + HeatConstants.VMI_REFS_PROPERTY_NAME
            + "' property which is connect to unsupported/incorrect "
            + (true == (new ContrailV2VirtualMachineInterfaceHelper()
            .isVlanSubInterfaceResource(interfaceResource)) ? "Vlan Sub interface " : "")
            + "resource '" + interfaceResourceId.get() + "' with type '"
            + interfaceResource.getType()
            + "', therefore, this connection will be ignored in TOSCA translation.");
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

}
