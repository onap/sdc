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

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.heat.datatypes.HeatBoolean;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ParameterDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.errors.MissingMandatoryPropertyErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ResourceTranslationNeutronSubnetImpl extends ResourceTranslationBase {
  protected static Logger logger =
      (Logger) LoggerFactory.getLogger(ResourceTranslationNeutronSubnetImpl.class);

  @Override
  public void translate(TranslateTo translateTo) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Optional<AttachedResourceId> subnetNetwork = getAttachedNetworkResource(translateTo);

    if (!subnetNetwork.isPresent() || !subnetNetwork.get().isGetResource()) {
      return;
    }

    Resource networkResource = HeatToToscaUtil
        .getResource(translateTo.getHeatOrchestrationTemplate(),
            (String) subnetNetwork.get().getEntityId(), translateTo.getHeatFileName());
    Optional<String> translatedNetworkId = ResourceTranslationFactory.getInstance(networkResource)
        .translateResource(translateTo.getHeatFileName(), translateTo.getServiceTemplate(),
            translateTo.getHeatOrchestrationTemplate(), networkResource,
            (String) subnetNetwork.get().getEntityId(), translateTo.getContext());
    if (translatedNetworkId.isPresent()) {
      NodeTemplate networkNodeTemplate = DataModelUtil
          .getNodeTemplate(translateTo.getServiceTemplate(), translatedNetworkId.get());

      Map<String, Map<String, Object>> subNetMap =
          (Map<String, Map<String, Object>>) networkNodeTemplate.getProperties().get("subnets");
      if (subNetMap == null) {
        subNetMap = new HashMap<>();
        networkNodeTemplate.getProperties().put("subnets", subNetMap);
        TranslatorHeatToToscaPropertyConverter
            .setSimpleProperty(translateTo.getServiceTemplate(),translateTo.getTranslatedId(),
                translateTo.getResource().getProperties(),
                translateTo.getHeatFileName(), translateTo.getResource().getType(),
                translateTo.getHeatOrchestrationTemplate(), translateTo.getContext(),
                networkNodeTemplate.getProperties(), "enable_dhcp",
                ToscaConstants.DHCP_ENABLED_PROPERTY_NAME, networkNodeTemplate); //dhcp_enabled
        TranslatorHeatToToscaPropertyConverter
            .setSimpleProperty(translateTo.getServiceTemplate(),translateTo.getTranslatedId(),
                translateTo.getResource().getProperties(),
                translateTo.getHeatFileName(), translateTo.getResource().getType(),
                translateTo.getHeatOrchestrationTemplate(), translateTo.getContext(),
                networkNodeTemplate.getProperties(), "ip_version", null, networkNodeTemplate);
        handleDhcpProperty(translateTo, networkNodeTemplate);
      }

      Map<String, Object> properties;
      properties = TranslatorHeatToToscaPropertyConverter
          .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(),translateTo.
                  getResourceId(),translateTo.getResource().getProperties(), null,
              translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(),
              translateTo.getResource().getType(), networkNodeTemplate, translateTo.getContext());

      subNetMap.put(translateTo.getResourceId(), properties);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  @Override
  protected String generateTranslatedId(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Optional<AttachedResourceId> subnetNetwork = getAttachedNetworkResource(translateTo);

    if (!subnetNetwork.isPresent() || !subnetNetwork.get().isGetResource()) {
      logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
          + translateTo.getResource().getType()
          + "' include 'network_id/'network'' property without 'get_resource' function, therefore"
          + " this resource will be ignored in TOSCA translation.");

      mdcDataDebugMessage.debugExitMessage(null, null);
      return null;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return (String) subnetNetwork.get().getTranslatedId();
  }

  private void handleDhcpProperty(TranslateTo translateTo, NodeTemplate networkNodeTemplate) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Object dhcpEnabled =
        networkNodeTemplate.getProperties().get(ToscaConstants.DHCP_ENABLED_PROPERTY_NAME);
    if (dhcpEnabled instanceof Map) {
      Object dhcpEnabledParameterName =
          ((Map) dhcpEnabled).get(ToscaFunctions.GET_INPUT.getDisplayName());
      if (dhcpEnabledParameterName != null) {
        ParameterDefinition dhcpParameterDefinition = null;
        if (translateTo.getServiceTemplate().getTopology_template().getInputs() != null) {
          dhcpParameterDefinition =
              translateTo.getServiceTemplate().getTopology_template().getInputs()
                  .get(dhcpEnabledParameterName);
        }
        if (dhcpParameterDefinition == null) {
          logger.warn("Missing input parameter " + dhcpEnabledParameterName);
        } else {
          Object defaultVal = dhcpParameterDefinition.get_default();
          if (defaultVal != null) {
            try {
              Boolean booleanValue = HeatBoolean.eval(defaultVal);
              dhcpParameterDefinition.set_default(booleanValue);
            } catch (CoreException coreException) {
              logger.debug("",coreException);
              //if value is not valid value for boolean set with dhcp_enabled default value = true
              dhcpParameterDefinition.set_default(true);
              logger.warn("Parameter '" + dhcpEnabledParameterName + "' used for "
                  + ToscaConstants.DHCP_ENABLED_PROPERTY_NAME
                  + " boolean property, but it's value is not a valid boolean value, therefore "
                  + ToscaConstants.DHCP_ENABLED_PROPERTY_NAME
                  + " property will be set with default value of 'true'.");
            }
            dhcpParameterDefinition.setType(PropertyType.BOOLEAN.getDisplayName());
          }
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private Optional<AttachedResourceId> getAttachedNetworkResource(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Optional<AttachedResourceId> subnetNetwork = Optional.empty();
    Optional<AttachedResourceId> attachedNetworkId =
        HeatToToscaUtil.extractAttachedResourceId(translateTo, "network_id");
    if (!attachedNetworkId.isPresent()) {
      Optional<AttachedResourceId> attachedNetwork =
          HeatToToscaUtil.extractAttachedResourceId(translateTo, "network");
      if (!attachedNetwork.isPresent()) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.GET_RESOURCE, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(),
            LoggerErrorDescription.MISSING_MANDATORY_PROPERTY);
        throw new CoreException(
            new MissingMandatoryPropertyErrorBuilder("network_id/'network'").build());
      } else {
        subnetNetwork = attachedNetwork;
      }
    } else {
      subnetNetwork = attachedNetworkId;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return subnetNetwork;
  }
}


