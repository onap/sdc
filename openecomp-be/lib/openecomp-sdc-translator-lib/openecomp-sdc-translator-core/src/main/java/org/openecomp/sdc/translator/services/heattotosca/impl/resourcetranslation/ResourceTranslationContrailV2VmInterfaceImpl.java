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

import java.util.List;
import java.util.Map;


public class ResourceTranslationContrailV2VmInterfaceImpl extends ResourceTranslationBase {
  protected static Logger logger =
      (Logger) LoggerFactory.getLogger(ResourceTranslationContrailV2VmInterfaceImpl.class);

  @Override
  protected String generateTranslatedId(TranslateTo translateTo) {
    if (new ContrailV2VirtualMachineInterfaceHelper().isVlanSubInterfaceResource(translateTo
        .getResource())) {
      return null;
    } else {
      return super.generateTranslatedId(translateTo);
    }
  }

  @Override
  protected void translate(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (new ContrailV2VirtualMachineInterfaceHelper().isVlanSubInterfaceResource(translateTo
        .getResource())) {
      translateVlanSubInterfaceResource(translateTo);
    } else {
      translateVirtualMachineInterfaceResource(translateTo);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }


  private void translateVirtualMachineInterfaceResource(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    NodeTemplate nodeTemplate = new NodeTemplate();
    nodeTemplate.setType(ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE);
    nodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(),translateTo.
                getResourceId(),translateTo.getResource().getProperties(),
            nodeTemplate.getProperties(), translateTo.getHeatFileName(),
            translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
            nodeTemplate, translateTo.getContext()));
    String toscaVmiRefsPropertyName =
        HeatToToscaUtil.getToscaPropertyName(translateTo, HeatConstants.VMI_REFS_PROPERTY_NAME);
    if (nodeTemplate.getProperties().containsKey(toscaVmiRefsPropertyName)) {
      nodeTemplate.getProperties().remove(toscaVmiRefsPropertyName);
    }

    handleVmiMacAddressesInProperties(translateTo, nodeTemplate);

    new ContrailV2VirtualMachineInterfaceHelper()
        .connectVmiToNetwork(this, translateTo, nodeTemplate);
    DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(),
        nodeTemplate);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void handleVmiMacAddressesInProperties(TranslateTo translateTo,
                                                 NodeTemplate nodeTemplate) {
    String toscaVmiMacAddressesName =
        HeatToToscaUtil.getToscaPropertyName(translateTo, HeatConstants.VMI_MAC_ADDRESSES);
    String toscaVmiMacAddressesMacAddressesName =
        HeatToToscaUtil.getToscaPropertyName(translateTo, HeatConstants.VMI_MAC_ADDRESSES_MAC_ADDRESSES);

    if(nodeTemplate.getProperties().containsKey(toscaVmiMacAddressesName)){
      Object macAddressesValue = nodeTemplate.getProperties().get(toscaVmiMacAddressesName);
      if(macAddressesValue instanceof Map && ((Map<String, Object>)macAddressesValue).containsKey
          (toscaVmiMacAddressesMacAddressesName)){
        updateMacAddressesMacAddressesInProperties(nodeTemplate, toscaVmiMacAddressesName,
            toscaVmiMacAddressesMacAddressesName,
            (Map<String, Object>) macAddressesValue);
      }
    }
  }

  private void updateMacAddressesMacAddressesInProperties(NodeTemplate nodeTemplate,
                                                          String toscaVmiMacAddressesName,
                                                          String toscaVmiMacAddressesMacAddressesName,
                                                          Map<String, Object> macAddressesValue) {
    Object macAddressesMacAddressesValue =
        macAddressesValue.get(toscaVmiMacAddressesMacAddressesName);
    if(macAddressesMacAddressesValue instanceof List){
      nodeTemplate.getProperties().put(toscaVmiMacAddressesName, macAddressesMacAddressesValue);
    }else{
      nodeTemplate.getProperties().remove(toscaVmiMacAddressesName);
    }
  }

  private void translateVlanSubInterfaceResource(TranslateTo translateTo) {

    mdcDataDebugMessage.debugEntryMessage(null, null);

    new ResourceTranslationContrailV2VlanSubInterfaceImpl().translate(translateTo);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

}
