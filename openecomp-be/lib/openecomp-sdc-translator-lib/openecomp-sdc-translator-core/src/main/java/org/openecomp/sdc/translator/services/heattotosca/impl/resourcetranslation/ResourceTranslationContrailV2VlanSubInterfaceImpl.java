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

import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.helper.ContrailV2VirtualMachineInterfaceHelper;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_MULTIPLE_INTERFACE_VALUES;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_UNSUPPORTED_VLAN_RESOURCE_CONNECTION;


public class ResourceTranslationContrailV2VlanSubInterfaceImpl extends ResourceTranslationBase {
    protected static Logger logger = LoggerFactory.getLogger(ResourceTranslationContrailV2VlanSubInterfaceImpl.class);

    @Override
    protected void translate(TranslateTo translateTo) {
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setType(ToscaNodeType.CONTRAILV2_VLAN_SUB_INTERFACE);
        nodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
                .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(),
                        translateTo.getResourceId(), translateTo.getResource().getProperties(),
                        nodeTemplate.getProperties(), translateTo.getHeatFileName(),
                        translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
                        nodeTemplate, translateTo.getContext()));
        new ContrailV2VirtualMachineInterfaceHelper().connectVmiToNetwork(this, translateTo, nodeTemplate);
        connectSubInterfaceToInterface(translateTo, nodeTemplate);
        DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(),
                nodeTemplate);
    }

    //connection to shared interface is not supported
    private void connectSubInterfaceToInterface(TranslateTo translateTo,
                                                NodeTemplate vlanSubInterfaceNodeTemplate) {
        Object interfaceRefs = translateTo.getResource().getProperties().get(HeatConstants.VMI_REFS_PROPERTY_NAME);
        if (Objects.isNull(interfaceRefs) || !(interfaceRefs instanceof List)
                || ((List) interfaceRefs).isEmpty()) {
            return;
        }
        List<String> acceptableResourceTypes = Arrays
                .asList(HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource(),
                        HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource());
        if (((List) interfaceRefs).size() > 1) {
            logger.warn(LOG_MULTIPLE_INTERFACE_VALUES, translateTo.getResourceId(), translateTo.getResource().getType(),
                    HeatConstants.VMI_REFS_PROPERTY_NAME);
        }
        Object interfaceRef = ((List) interfaceRefs).get(0);
        Optional<String> interfaceResourceId =
                HeatToToscaUtil.extractContrailGetResourceAttachedHeatResourceId(interfaceRef);
        if (interfaceResourceId.isPresent()) { // get_resource
            Resource interfaceResource = HeatToToscaUtil.getResource(translateTo.getHeatOrchestrationTemplate(),
                    interfaceResourceId.get(), translateTo.getHeatFileName());

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
                logger.warn(LOG_UNSUPPORTED_VLAN_RESOURCE_CONNECTION, translateTo.getResourceId(), translateTo
                                .getResource().getType(), HeatConstants.VMI_REFS_PROPERTY_NAME,
                        getLogMessage(interfaceResource), interfaceResourceId.get(), interfaceResource.getType());
            }
        }
    }

    private String getLogMessage(Resource interfaceResource) {
        return (new ContrailV2VirtualMachineInterfaceHelper().isVlanSubInterfaceResource(interfaceResource))
                ? "Vlan Sub interface " : "";
    }
}
