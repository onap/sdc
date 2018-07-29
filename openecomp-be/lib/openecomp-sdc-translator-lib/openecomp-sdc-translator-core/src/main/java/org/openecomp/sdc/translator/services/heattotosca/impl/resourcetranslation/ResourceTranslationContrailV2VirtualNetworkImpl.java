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

import org.apache.commons.collections.CollectionUtils;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.heat.datatypes.model.ResourceReferenceFunctions;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.ReferenceType;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.util.*;

import static org.openecomp.sdc.tosca.services.DataModelUtil.createAttachmentRequirementAssignment;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.*;

public class ResourceTranslationContrailV2VirtualNetworkImpl extends ResourceTranslationBase {

    private static final String FQ_NAME = "fq_name";
    protected static Logger logger = LoggerFactory.getLogger(ResourceTranslationContrailV2VirtualNetworkImpl.class);

    @Override
    public void translate(TranslateTo translateTo) {
        NodeTemplate nodeTemplate = new NodeTemplate();
        nodeTemplate.setType(ToscaNodeType.CONTRAILV2_VIRTUAL_NETWORK);
        nodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
                .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(),
                        translateTo.getResourceId(), translateTo.getResource().getProperties(),
                        nodeTemplate.getProperties(), translateTo.getHeatFileName(),
                        translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
                        nodeTemplate, translateTo.getContext()));
        DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(),
                nodeTemplate);
        linkToPolicyNodeTemplate(translateTo);
    }

    private void linkToPolicyNodeTemplate(TranslateTo translateTo) {
        List<AttachedResourceId> networkPolicyIdList = extractNetworkPolicyIdList(translateTo);
        if (CollectionUtils.isEmpty(networkPolicyIdList)) {
            return;
        }
        for (AttachedResourceId attachedResourceId : networkPolicyIdList) {
            NodeTemplate policyNodeTemplate = DataModelUtil.getNodeTemplate(translateTo.getServiceTemplate(),
                            (String) attachedResourceId.getTranslatedId());
            DataModelUtil.addRequirementAssignment(policyNodeTemplate, ToscaConstants.NETWORK_REQUIREMENT_ID,
                    createAttachmentRequirementAssignment(translateTo.getTranslatedId()));
        }
    }

    private List<AttachedResourceId> extractNetworkPolicyIdList(TranslateTo translateTo) {
        Object propertyValue = translateTo.getResource().getProperties().get("network_policy_refs");
        if (propertyValue != null) {
            return extractNetworkPolicyId(propertyValue, translateTo);
        }
        return Collections.emptyList();
    }

    private List<AttachedResourceId> extractNetworkPolicyId(Object propertyValue,
                                                            TranslateTo translateTo) {
        List<AttachedResourceId> attachedResourceIdList = new ArrayList<>();
        if (propertyValue instanceof List) {
            for (Object value : (List) propertyValue) {
                attachedResourceIdList.addAll(extractNetworkPolicyId(value, translateTo));
            }
        } else {
            AttachedResourceId resourceId = parseNetworkPolicyId(propertyValue, translateTo);
            if (resourceId != null) {
                attachedResourceIdList.add(resourceId);
            }
        }
        return attachedResourceIdList;
    }

    private AttachedResourceId parseNetworkPolicyId(Object propertyValue, TranslateTo translateTo) {
        Optional<String> translatedPolicyResourceId;
        String policyResourceId = extractResourceId(propertyValue, translateTo);
        if (policyResourceId == null) {
            return null;
        }

        Resource policyResource = HeatToToscaUtil.getResource(translateTo.getHeatOrchestrationTemplate(),
                policyResourceId, translateTo.getHeatFileName());
        if (!policyResource.getType().equals(HeatResourcesTypes.CONTRAIL_V2_NETWORK_RULE_RESOURCE_TYPE
                .getHeatResource())) {
            return null;
        }
        translatedPolicyResourceId = ResourceTranslationFactory.getInstance(policyResource)
                .translateResource(translateTo.getHeatFileName(), translateTo.getServiceTemplate(),
                        translateTo.getHeatOrchestrationTemplate(), policyResource, policyResourceId,
                        translateTo.getContext());
        if (!translatedPolicyResourceId.isPresent()) {
            logger.warn(LOG_INVALID_NETWORK_POLICY_REFS_RESOURCE,
                    translateTo.getResourceId(), translateTo.getResource().getType());
            return null;
        }
        return new AttachedResourceId(translatedPolicyResourceId.get(), policyResourceId, ReferenceType.GET_ATTR);
    }

    private String extractResourceId(Object propertyValue, TranslateTo translateTo) {
        if (propertyValue instanceof Map) {
            return extractResourceIdFromMapProperty((Map) propertyValue, translateTo);
        } else if (propertyValue instanceof List) {
            String resourceId = extractResourceIdFromListProperty((List) propertyValue, translateTo);
            if (resourceId != null) {
                return resourceId;
            }
        }
        logger.warn(LOG_INVALID_PROPERTY_VALUE_FORMAT, translateTo.getResourceId(),
                translateTo.getResource().getType());
        return null;
    }

    private String extractResourceIdFromMapProperty(Map propertyValue, TranslateTo translateTo) {
        Object value;
        String resourceId = null;
        if (propertyValue.containsKey(ResourceReferenceFunctions.GET_ATTR.getFunction())) {
            value = propertyValue.get(ResourceReferenceFunctions.GET_ATTR.getFunction());
            if (value instanceof List && extractResourceIdFromGetAttrList(translateTo, (List) value)) {
                resourceId = (String) ((List) value).get(0);
            }
        } else if (propertyValue.containsKey(ResourceReferenceFunctions.GET_RESOURCE.getFunction())) {
            resourceId = extractResourceIdFromGetResource(propertyValue, translateTo);
        } else {
            resourceId = extractResourceIdFromPropertyValues(propertyValue, translateTo);
        }
        return resourceId;
    }

    private boolean extractResourceIdFromGetAttrList(TranslateTo translateTo, List<Object> value) {
        if (value.size() == 2 && FQ_NAME.equals(value.get(1))) {
            if (value.get(0) instanceof String) {
                return true;
            } else {
                logger.warn(LOG_INVALID_PROPERTY_FORMAT_GET_ATTR_FQ_NAME, translateTo.getResourceId(),
                        translateTo.getResource().getType());
            }
        }
        return false;
    }

    private String extractResourceIdFromPropertyValues(Map propertyValue, TranslateTo translateTo) {
        Collection<Object> valCollection = propertyValue.values();
        for (Object entryValue : valCollection) {
            String ret = extractResourceId(entryValue, translateTo);
            if (ret != null) {
                return ret;
            }
        }
        return null;
    }

    private String extractResourceIdFromListProperty(List<Object> propertyValue, TranslateTo translateTo) {
        for (Object prop : propertyValue) {
            String resourceId = extractResourceId(prop, translateTo);
            if (resourceId != null) {
                return resourceId;
            }
        }
        return null;
    }

    private String extractResourceIdFromGetResource(Map propertyValue, TranslateTo translateTo) {
        Object value;
        value = propertyValue.get(ResourceReferenceFunctions.GET_RESOURCE.getFunction());
        if (value instanceof String) {
            return (String) value;
        } else {
            logger.warn(LOG_INVALID_PROPERTY_FORMAT_GET_RESOURCE, translateTo.getResourceId(),
                    translateTo.getResource().getType());
        }
        return null;
    }
}
