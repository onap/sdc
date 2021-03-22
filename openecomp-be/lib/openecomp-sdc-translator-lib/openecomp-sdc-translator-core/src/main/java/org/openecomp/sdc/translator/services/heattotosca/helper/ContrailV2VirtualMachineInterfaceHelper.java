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
package org.openecomp.sdc.translator.services.heattotosca.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.heat.services.HeatConstants;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationEntityType;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationBase;

public class ContrailV2VirtualMachineInterfaceHelper {

    static Logger logger = (Logger) LoggerFactory.getLogger(ContrailV2VirtualMachineInterfaceHelper.class);

    /**
     * Connect Virtual Machine Interface node template to network node template in TOSCA.
     *
     * @param resourceTranslationImpl resource translation implemetation
     * @param translateTo             translated ro object
     * @param vmiNodeTemplate         Virtual Machine Interface node template
     */
    public void connectVmiToNetwork(ResourceTranslationBase resourceTranslationImpl, TranslateTo translateTo, NodeTemplate vmiNodeTemplate) {
        Object virtualNetworkRefs = translateTo.getResource().getProperties().get(HeatConstants.VIRTUAL_NETWORK_REFS_PROPERTY_NAME);
        if (Objects.isNull(virtualNetworkRefs) || !(virtualNetworkRefs instanceof List) || ((List) virtualNetworkRefs).size() == 0) {
            return;
        }
        List<String> acceptableResourceTypes = Arrays.asList(HeatResourcesTypes.NEUTRON_NET_RESOURCE_TYPE.getHeatResource(),
            HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_NETWORK_RESOURCE_TYPE.getHeatResource());
        if (((List) virtualNetworkRefs).size() > 1) {
            logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '" + translateTo.getResource().getType() + "' include '"
                + HeatConstants.VIRTUAL_NETWORK_REFS_PROPERTY_NAME + "' property with more than one network values, only "
                + "the first network will be connected, " + "all rest will be ignored in TOSCA translation.");
        }
        Object virtualNetworkRef = ((List) virtualNetworkRefs).get(0);
        Optional<String> networkResourceId = HeatToToscaUtil.extractContrailGetResourceAttachedHeatResourceId(virtualNetworkRef);
        if (networkResourceId.isPresent()) { // get_resource
            Resource networkResource = HeatToToscaUtil
                .getResource(translateTo.getHeatOrchestrationTemplate(), networkResourceId.get(), translateTo.getHeatFileName());
            if (acceptableResourceTypes.contains(networkResource.getType())) {
                Optional<String> resourceTranslatedId = resourceTranslationImpl
                    .getResourceTranslatedId(translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(), networkResourceId.get(),
                        translateTo.getContext());
                if (resourceTranslatedId.isPresent()) {
                    RequirementAssignment requirementAssignment = HeatToToscaUtil
                        .addLinkReqFromPortToNetwork(vmiNodeTemplate, resourceTranslatedId.get());
                    if (ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE.equals(vmiNodeTemplate.getType())) {
                        ConsolidationDataUtil.updateNodesConnectedOut(translateTo, resourceTranslatedId.get(), ConsolidationEntityType.PORT,
                            ToscaConstants.LINK_REQUIREMENT_ID, requirementAssignment);
                    }
                }
            } else {
                logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '" + translateTo.getResource().getType() + "' include '"
                    + HeatConstants.VIRTUAL_NETWORK_REFS_PROPERTY_NAME + "' property which is connect to "
                    + "unsupported/incorrect resource with type '" + networkResource.getType()
                    + "', therefore, this connection will be ignored in TOSCA translation.");
            }
        } else {
            Optional<AttachedResourceId> attachedResourceId = HeatToToscaUtil
                .extractAttachedResourceId(translateTo.getHeatFileName(), translateTo.getHeatOrchestrationTemplate(), translateTo.getContext(),
                    virtualNetworkRef);
            if (attachedResourceId.isPresent() && attachedResourceId.get().isGetParam() && attachedResourceId.get().getEntityId() instanceof String) {
                TranslatedHeatResource translatedSharedResourceId = translateTo.getContext().getHeatSharedResourcesByParam()
                    .get(attachedResourceId.get().getEntityId());
                if (Objects.nonNull(translatedSharedResourceId) && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
                    RequirementAssignment requirementAssignment = HeatToToscaUtil
                        .addLinkReqFromPortToNetwork(vmiNodeTemplate, translatedSharedResourceId.getTranslatedId());
                    if (ToscaNodeType.CONTRAILV2_VIRTUAL_MACHINE_INTERFACE.equals(vmiNodeTemplate.getType())) {
                        ConsolidationDataUtil
                            .updateNodesConnectedOut(translateTo, translatedSharedResourceId.getTranslatedId(), ConsolidationEntityType.PORT,
                                ToscaConstants.LINK_REQUIREMENT_ID, requirementAssignment);
                    }
                }
            }
        }
    }

    /**
     * Check if the input heat resource is Vlan sub interface resource.
     *
     * @param resource heat resource to be checked
     * @return true - if input resource is valn sub interface resource flase - otherwise.
     */
    public boolean isVlanSubInterfaceResource(Resource resource) {
        if (resource.getType().equals(HeatResourcesTypes.CONTRAIL_V2_VIRTUAL_MACHINE_INTERFACE_RESOURCE_TYPE.getHeatResource())
            && getVlanTagPropertyValue(resource).isPresent()) {
            return true;
        }
        return false;
    }

    public boolean isVlanSubInterfaceConnectedToPortIndirectly(TranslateTo translateTo) {
        Resource resource = translateTo.getResource();
        TranslationContext context = translateTo.getContext();
        Set<String> nestedHeatsFiles = context.getNestedHeatsFiles();
        Map<String, Object> properties = resource.getProperties();
        if (MapUtils.isNotEmpty(properties) && properties.containsKey(HeatConstants.VMI_REFS_PROPERTY_NAME)) {
            Map<String, Object> portReference = getPortReference(properties);
            return CollectionUtils.isNotEmpty(nestedHeatsFiles) && nestedHeatsFiles.contains(translateTo.getHeatFileName()) && portReference
                .containsKey(ResourceReferenceFunctions.GET_PARAM.getFunction());
        }
        return false;
    }

    private Map<String, Object> getPortReference(Map<String, Object> properties) {
        Object portReferenceObj = properties.get(HeatConstants.VMI_REFS_PROPERTY_NAME);
        List<Object> portReference = portReferenceObj instanceof List ? (List<Object>) portReferenceObj : new ArrayList<>();
        return CollectionUtils.isEmpty(portReference) ? new HashMap<>() : (Map<String, Object>) portReference.get(0);
    }

    private Optional<Object> getVlanTagPropertyValue(Resource resource) {
        Object vmiProperties = resource.getProperties().get(HeatConstants.VMI_PROPERTIES_PROPERTY_NAME);
        if (vmiProperties != null && vmiProperties instanceof Map) {
            return Optional.ofNullable(((Map) vmiProperties).get(HeatConstants.VMI_SUB_INTERFACE_VLAN_TAG_PROPERTY_NAME));
        }
        return Optional.empty();
    }
}
