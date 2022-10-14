/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.onap.sdc.tosca.datatypes.model.PropertyType;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.HeatBoolean;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.errors.MissingMandatoryPropertyErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

public class ResourceTranslationNeutronSubnetImpl extends ResourceTranslationBase {

    private static final Logger logger = LoggerFactory.getLogger(ResourceTranslationNeutronSubnetImpl.class);
    private static final String IP_VERSION_PROPERTY_NAME = "ip_version";

    @Override
    public void translate(TranslateTo translateTo) {
        Optional<AttachedResourceId> subnetNetwork = getAttachedNetworkResource(translateTo);
        if (!subnetNetwork.get().isGetResource()) {
            return;
        }
        Resource networkResource = HeatToToscaUtil
            .getResource(translateTo.getHeatOrchestrationTemplate(), (String) subnetNetwork.get().getEntityId(), translateTo.getHeatFileName());
        Optional<String> translatedNetworkId = ResourceTranslationFactory.getInstance(networkResource)
            .translateResource(translateTo.getHeatFileName(), translateTo.getServiceTemplate(), translateTo.getHeatOrchestrationTemplate(),
                networkResource, (String) subnetNetwork.get().getEntityId(), translateTo.getContext());
        if (!translatedNetworkId.isPresent()) {
            return;
        }
        NodeTemplate networkNodeTemplate = DataModelUtil.getNodeTemplate(translateTo.getServiceTemplate(), translatedNetworkId.get());
        Map<String, Map<String, Object>> subNetMap = (Map<String, Map<String, Object>>) networkNodeTemplate.getProperties()
            .get(HeatConstants.SUBNETS_PROPERTY_NAME);
        if (subNetMap == null) {
            subNetMap = addSubnetProperties(translateTo, networkNodeTemplate);
        }
        Map<String, Object> properties = TranslatorHeatToToscaPropertyConverter
            .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(), translateTo.getResourceId(),
                translateTo.getResource().getProperties(), null, translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(),
                translateTo.getResource().getType(), networkNodeTemplate, translateTo.getContext());
        subNetMap.put(translateTo.getResourceId(), properties);
    }

    private Map<String, Map<String, Object>> addSubnetProperties(TranslateTo translateTo, NodeTemplate networkNodeTemplate) {
        Map<String, Map<String, Object>> subNetMap = new HashMap<>();
        networkNodeTemplate.getProperties().put(HeatConstants.SUBNETS_PROPERTY_NAME, subNetMap);
        TranslatorHeatToToscaPropertyConverter
            .setSimpleProperty(translateTo.getServiceTemplate(), translateTo.getTranslatedId(), translateTo.getResource().getProperties(),
                translateTo.getHeatFileName(), translateTo.getResource().getType(), translateTo.getHeatOrchestrationTemplate(),
                translateTo.getContext(), networkNodeTemplate.getProperties(), HeatConstants.ENABLE_DHCP_PROPERTY_NAME,
                ToscaConstants.DHCP_ENABLED_PROPERTY_NAME, networkNodeTemplate); //dhcp_enabled
        TranslatorHeatToToscaPropertyConverter
            .setSimpleProperty(translateTo.getServiceTemplate(), translateTo.getTranslatedId(), translateTo.getResource().getProperties(),
                translateTo.getHeatFileName(), translateTo.getResource().getType(), translateTo.getHeatOrchestrationTemplate(),
                translateTo.getContext(), networkNodeTemplate.getProperties(), IP_VERSION_PROPERTY_NAME, null, networkNodeTemplate);
        handleDhcpProperty(translateTo, networkNodeTemplate);
        return subNetMap;
    }

    @Override
    protected String generateTranslatedId(TranslateTo translateTo) {
        Optional<AttachedResourceId> subnetNetwork = getAttachedNetworkResource(translateTo);
        if (!subnetNetwork.get().isGetResource()) {
            logger.warn("Heat resource: '{}' with type: '{}' include 'network_id/'network'' property without "
                    + "'get_resource' function, therefore this resource will be ignored in TOSCA translation.", translateTo.getResourceId(),
                translateTo.getResource().getType());
            return null;
        }
        return (String) subnetNetwork.get().getTranslatedId();
    }

    private void handleDhcpProperty(TranslateTo translateTo, NodeTemplate networkNodeTemplate) {
        Object dhcpEnabled = networkNodeTemplate.getProperties().get(ToscaConstants.DHCP_ENABLED_PROPERTY_NAME);
        if (!(dhcpEnabled instanceof Map)) {
            return;
        }
        Object dhcpEnabledParameterName = ((Map) dhcpEnabled).get(ToscaFunctions.GET_INPUT.getFunctionName());
        if (dhcpEnabledParameterName == null) {
            return;
        }
        ParameterDefinition dhcpParameterDefinition = null;
        if (translateTo.getServiceTemplate().getTopology_template().getInputs() != null) {
            dhcpParameterDefinition = translateTo.getServiceTemplate().getTopology_template().getInputs().get(dhcpEnabledParameterName);
        }
        if (dhcpParameterDefinition == null) {
            logger.warn("Missing input parameter : {} ", dhcpEnabledParameterName);
        } else {
            Object defaultVal = dhcpParameterDefinition.get_default();
            if (defaultVal == null) {
                return;
            }
            try {
                Boolean booleanValue = HeatBoolean.eval(defaultVal);
                dhcpParameterDefinition.set_default(booleanValue);
            } catch (CoreException coreException) {
                dhcpParameterDefinition.set_default(true);
                logger.warn("Parameter '{}' used for {} boolean property, but it's value is not a valid boolean "
                        + "value, therefore {} property will be set with default value of 'true'.", dhcpEnabledParameterName,
                    ToscaConstants.DHCP_ENABLED_PROPERTY_NAME, ToscaConstants.DHCP_ENABLED_PROPERTY_NAME, coreException);
            }
            dhcpParameterDefinition.setType(PropertyType.BOOLEAN.getDisplayName());
        }
    }

    private Optional<AttachedResourceId> getAttachedNetworkResource(TranslateTo translateTo) {
        Optional<AttachedResourceId> attachedNetworkId = HeatToToscaUtil
            .extractAttachedResourceId(translateTo, HeatConstants.NETWORK_ID_PROPERTY_NAME);
        if (attachedNetworkId.isPresent()) {
            return attachedNetworkId;
        }
        Optional<AttachedResourceId> attachedNetwork = HeatToToscaUtil.extractAttachedResourceId(translateTo, HeatConstants.NETWORK_PROPERTY_NAME);
        if (attachedNetwork.isPresent()) {
            return attachedNetwork;
        }
        throw new CoreException(
            new MissingMandatoryPropertyErrorBuilder(HeatConstants.NETWORK_ID_PROPERTY_NAME + "/" + HeatConstants.NETWORK_PROPERTY_NAME).build());
    }
}
