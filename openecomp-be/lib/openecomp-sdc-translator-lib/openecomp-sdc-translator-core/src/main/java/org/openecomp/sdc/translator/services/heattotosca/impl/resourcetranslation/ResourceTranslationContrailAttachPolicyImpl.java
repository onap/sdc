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

import static org.openecomp.sdc.heat.services.HeatConstants.NETWORK_PROPERTY_NAME;
import static org.openecomp.sdc.tosca.services.DataModelUtil.createAttachmentRequirementAssignment;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_UNSUPPORTED_POLICY_NETWORK_PROPERTY;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_UNSUPPORTED_POLICY_PROPERTY_GET_ATTR;
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_UNSUPPORTED_POLICY_RESOURCE;

import java.util.Optional;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaTopologyTemplateElements;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.errors.MissingMandatoryPropertyErrorBuilder;

public class ResourceTranslationContrailAttachPolicyImpl extends ResourceTranslationBase {

    protected static Logger logger = LoggerFactory.getLogger(ResourceTranslationContrailAttachPolicyImpl.class);

    @Override
    protected void translate(TranslateTo translateTo) {
        String heatFileName = translateTo.getHeatFileName();
        String translatedNetworkResourceId = getTranslatedNetworkResourceId(translateTo);
        if (translatedNetworkResourceId == null) {
            return;
        }
        NodeTemplate policyNodeTemplate = getTranslatedPolicyNodeTemplate(translateTo, heatFileName);
        if (policyNodeTemplate != null) {
            DataModelUtil.addRequirementAssignment(policyNodeTemplate, ToscaConstants.NETWORK_REQUIREMENT_ID,
                createAttachmentRequirementAssignment(translatedNetworkResourceId));
        }
    }

    @Override
    protected String generateTranslatedId(TranslateTo translateTo) {
        return extractAttachedResourceIdHandleMissing(translateTo, NETWORK_PROPERTY_NAME).getEntityId().toString();
    }

    @Override
    protected Optional<ToscaTopologyTemplateElements> getTranslatedToscaTopologyElement(TranslateTo translateTo) {
        return Optional.empty();
    }

    private NodeTemplate getTranslatedPolicyNodeTemplate(TranslateTo translateTo, String heatFileName) {
        AttachedResourceId attachedPolicyResourceId = extractAttachedResourceIdHandleMissing(translateTo, "policy");
        NodeTemplate policyNodeTemplate = new NodeTemplate();
        Optional<String> policyResourceId = HeatToToscaUtil.getContrailAttachedHeatResourceId(attachedPolicyResourceId);
        if (policyResourceId.isPresent()) {
            policyNodeTemplate = getPolicyNodeTemplate(translateTo, heatFileName, policyResourceId.get());
        } else {
            logger.warn(LOG_UNSUPPORTED_POLICY_PROPERTY_GET_ATTR, translateTo.getResourceId(), translateTo.getResource().getType());
        }
        return policyNodeTemplate;
    }

    private NodeTemplate getPolicyNodeTemplate(TranslateTo translateTo, String heatFileName, String policyResourceId) {
        Resource policyResource = HeatToToscaUtil.getResource(translateTo.getHeatOrchestrationTemplate(), policyResourceId, heatFileName);
        Optional<String> translatedPolicyResourceId = ResourceTranslationFactory.getInstance(policyResource)
            .translateResource(heatFileName, translateTo.getServiceTemplate(), translateTo.getHeatOrchestrationTemplate(), policyResource,
                policyResourceId, translateTo.getContext());
        if (!translatedPolicyResourceId.isPresent()) {
            logger.warn(LOG_UNSUPPORTED_POLICY_RESOURCE, translateTo.getResourceId(), translateTo.getResource().getType());
            return null;
        }
        return DataModelUtil.getNodeTemplate(translateTo.getServiceTemplate(), translatedPolicyResourceId.get());
    }

    private String getTranslatedNetworkResourceId(TranslateTo translateTo) {
        AttachedResourceId attachedNetworkResourceId = extractAttachedResourceIdHandleMissing(translateTo, NETWORK_PROPERTY_NAME);
        String translatedNetworkResourceId = null;
        if (attachedNetworkResourceId.isGetResource()) {
            translatedNetworkResourceId = (String) attachedNetworkResourceId.getTranslatedId();
        } else {
            logger.warn(LOG_UNSUPPORTED_POLICY_NETWORK_PROPERTY, translateTo.getResourceId(), translateTo.getResource().getType());
        }
        return translatedNetworkResourceId;
    }

    private AttachedResourceId extractAttachedResourceIdHandleMissing(TranslateTo translateTo, String propertyName) {
        Optional<AttachedResourceId> attachedResourceId = HeatToToscaUtil.extractAttachedResourceId(translateTo, propertyName);
        if (!attachedResourceId.isPresent()) {
            throw new CoreException(new MissingMandatoryPropertyErrorBuilder(propertyName).build());
        }
        return attachedResourceId.get();
    }
}
