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
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.ToscaTopologyTemplateElements;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.errors.MissingMandatoryPropertyErrorBuilder;

import java.util.Optional;

public class ResourceTranslationContrailAttachPolicyImpl extends ResourceTranslationBase {
  protected static Logger logger =
      (Logger) LoggerFactory.getLogger(ResourceTranslationContrailAttachPolicyImpl.class);

  @Override
  protected void translate(TranslateTo translateTo) {
    String heatFileName = translateTo.getHeatFileName();
    String translatedNetworkResourceId = getTranslatedNetworkResourceId(translateTo);
    if (translatedNetworkResourceId == null) {
      return;
    }

    NodeTemplate policyNodeTemplate = getTranslatedPolicyNodeTemplate(translateTo, heatFileName);
    if (policyNodeTemplate != null) {
      DataModelUtil
          .addRequirementAssignment(policyNodeTemplate, ToscaConstants.NETWORK_REQUIREMENT_ID,
              createRequirementAssignment(translatedNetworkResourceId));
    }
  }

  @Override
  protected String generateTranslatedId(TranslateTo translateTo) {
    return extractAttachedResourceIdHandleMissing(translateTo, "network").getEntityId()
        .toString();
  }

  @Override
  protected Optional<ToscaTopologyTemplateElements> getTranslatedToscaTopologyElement(
      TranslateTo translateTo) {
    return Optional.empty();
  }

  private NodeTemplate getTranslatedPolicyNodeTemplate(TranslateTo translateTo,
                                                       String heatFileName) {
    AttachedResourceId attachedPolicyResourceId =
        extractAttachedResourceIdHandleMissing(translateTo, "policy");
    NodeTemplate policyNodeTemplate = new NodeTemplate();
    Optional<String> policyResourceId =
        HeatToToscaUtil.getContrailAttachedHeatResourceId(attachedPolicyResourceId);
    if (policyResourceId.isPresent()) {
      policyNodeTemplate = getPolicyNodeTemplate(translateTo, heatFileName, policyResourceId.get());
    } else {
      logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
          + translateTo.getResource().getType()
          + "' include 'policy' property without 'get_attr' of 'fq_name'/'get_resource' function,"
          + " therefore this resource will be ignored in TOSCA translation.");
    }
    return policyNodeTemplate;
  }

  private NodeTemplate getPolicyNodeTemplate(TranslateTo translateTo, String heatFileName,
                                             String policyResourceId) {
    Resource policyResource = HeatToToscaUtil
        .getResource(translateTo.getHeatOrchestrationTemplate(), policyResourceId, heatFileName);
    Optional<String> translatedPolicyResourceId =
        ResourceTranslationFactory.getInstance(policyResource)
            .translateResource(heatFileName, translateTo.getServiceTemplate(),
                translateTo.getHeatOrchestrationTemplate(), policyResource, policyResourceId,
                translateTo.getContext());
    if (!translatedPolicyResourceId.isPresent()) {
      logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
          + translateTo.getResource().getType()
          + "' include unsupported policy resource, therefore this resource will be ignored in "
          + "TOSCA translation. ");
      return null;
    }
    return DataModelUtil
        .getNodeTemplate(translateTo.getServiceTemplate(), translatedPolicyResourceId.get());
  }

  private String getTranslatedNetworkResourceId(TranslateTo translateTo) {
    AttachedResourceId attachedNetworkResourceId =
        extractAttachedResourceIdHandleMissing(translateTo, "network");

    String translatedNetworkResourceId = null;
    if (attachedNetworkResourceId.isGetResource()) {
      translatedNetworkResourceId = (String) attachedNetworkResourceId.getTranslatedId();
    } else {
      logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
          + translateTo.getResource().getType()
          + "' include 'network' property without 'get_resource' function, therefore this "
          + "resource will be ignored in TOSCA translation.");
    }
    return translatedNetworkResourceId;
  }

  private RequirementAssignment createRequirementAssignment(String translatedNetworkResourceId) {
    RequirementAssignment requirement = new RequirementAssignment();
    requirement.setCapability(ToscaCapabilityType.NATIVE_ATTACHMENT);
    requirement.setNode(translatedNetworkResourceId);
    requirement.setRelationship(ToscaRelationshipType.ATTACHES_TO);
    return requirement;
  }

  private AttachedResourceId extractAttachedResourceIdHandleMissing(
      TranslateTo translateTo, String propertyName) {
    Optional<AttachedResourceId> attachedResourceId =
        HeatToToscaUtil.extractAttachedResourceId(translateTo, propertyName);

    if (!attachedResourceId.isPresent()) {
      throw new CoreException(new MissingMandatoryPropertyErrorBuilder(propertyName).build());
    }
    return attachedResourceId.get();
  }
}
