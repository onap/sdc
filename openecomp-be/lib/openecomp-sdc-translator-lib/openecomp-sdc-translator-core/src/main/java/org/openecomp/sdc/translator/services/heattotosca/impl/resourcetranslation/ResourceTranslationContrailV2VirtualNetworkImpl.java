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

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.RequirementAssignment;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.ReferenceType;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResourceTranslationContrailV2VirtualNetworkImpl extends ResourceTranslationBase {

  protected static Logger logger =
      (Logger) LoggerFactory.getLogger(ResourceTranslationContrailV2VirtualNetworkImpl.class);

  @Override
  public void translate(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    NodeTemplate nodeTemplate = new NodeTemplate();
    nodeTemplate.setType(ToscaNodeType.CONTRAILV2_VIRTUAL_NETWORK);
    nodeTemplate.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(),translateTo.
            getResourceId(),translateTo.getResource().getProperties(),
            nodeTemplate.getProperties(), translateTo.getHeatFileName(),
            translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
            nodeTemplate, translateTo.getContext()));
    DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(),
            nodeTemplate);
    linkToPolicyNodeTemplate(translateTo);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void linkToPolicyNodeTemplate(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    List<AttachedResourceId> networkPolicyIdList = extractNetworkPolicyIdList(translateTo);
    if (CollectionUtils.isEmpty(networkPolicyIdList)) {
      return;
    }
    for (AttachedResourceId attachedResourceId : networkPolicyIdList) {
      NodeTemplate policyNodeTemplate = DataModelUtil
          .getNodeTemplate(translateTo.getServiceTemplate(),
              (String) attachedResourceId.getTranslatedId());
      DataModelUtil
          .addRequirementAssignment(policyNodeTemplate, ToscaConstants.NETWORK_REQUIREMENT_ID,
              createRequirementAssignment(translateTo.getTranslatedId()));
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private List<AttachedResourceId> extractNetworkPolicyIdList(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Object propertyValue = translateTo.getResource().getProperties().get("network_policy_refs");
    if (propertyValue != null) {
      mdcDataDebugMessage.debugExitMessage(null, null);
      return extractNetworkPolicyId(propertyValue, translateTo);
    } else {
      mdcDataDebugMessage.debugExitMessage(null, null);
      return null;
    }
  }

  private List<AttachedResourceId> extractNetworkPolicyId(Object propertyValue,
                                                          TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    List<AttachedResourceId> attachedResourceIdList = new ArrayList<>();

    if (propertyValue instanceof List) {
      for (Object value : (List) propertyValue) {
        attachedResourceIdList.addAll(extractNetworkPolicyId(value, translateTo));
      }
    } else {
      AttachedResourceId resourceId = parsNetworkPolicyId(propertyValue, translateTo);
      if (resourceId != null) {
        attachedResourceIdList.add(resourceId);
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return attachedResourceIdList;
  }

  private AttachedResourceId parsNetworkPolicyId(Object propertyValue, TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Optional<String> translatedPolicyResourceId;
    String policyResourceId = extractResourceId(propertyValue, translateTo);
    if (policyResourceId == null) {
      mdcDataDebugMessage.debugExitMessage(null, null);
      return null;
    }

    Resource policyResource = HeatToToscaUtil
        .getResource(translateTo.getHeatOrchestrationTemplate(), policyResourceId,
            translateTo.getHeatFileName());
    if (!policyResource.getType()
        .equals(HeatResourcesTypes.CONTRAIL_V2_NETWORK_RULE_RESOURCE_TYPE.getHeatResource())) {
      mdcDataDebugMessage.debugExitMessage(null, null);
      return null;
    }
    translatedPolicyResourceId = ResourceTranslationFactory.getInstance(policyResource)
        .translateResource(translateTo.getHeatFileName(), translateTo.getServiceTemplate(),
            translateTo.getHeatOrchestrationTemplate(), policyResource, policyResourceId,
            translateTo.getContext());
    if (!translatedPolicyResourceId.isPresent()) {
      logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
          + translateTo.getResource().getType()
          + "' property network_policy_refs is referenced to an unsupported resource the "
          + "connection will be ignored in TOSCA translation.");
      mdcDataDebugMessage.debugExitMessage(null, null);
      return null;
    }
    AttachedResourceId attachedResourceId =
        new AttachedResourceId(translatedPolicyResourceId.get(), policyResourceId,
            ReferenceType.GET_ATTR);
    mdcDataDebugMessage.debugExitMessage(null, null);
    return attachedResourceId;
  }

  private String extractResourceId(Object propertyValue, TranslateTo translateTo) {

    Object value;
    if (propertyValue instanceof Map) {
      if (((Map) propertyValue).containsKey("get_attr")) {
        value = ((Map) propertyValue).get("get_attr");
        if (value instanceof List) {
          if (((List) value).size() == 2 && ((List) value).get(1).equals("fq_name")) {
            if (((List) value).get(0) instanceof String) {
              return (String) ((List) value).get(0);
            } else {
              logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
                  + translateTo.getResource().getType()
                  + "' has property with invalid format of 'get_attr' function with 'fq_name' "
                  + "value, therefore this property will be ignored in TOSCA translation.");
            }
          }
        }
      } else if (((Map) propertyValue).containsKey("get_resource")) {
        value = ((Map) propertyValue).get("get_resource");
        if (value instanceof String) {
          return (String) value;
        } else {
          logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
              + translateTo.getResource().getType()
              + "' has property invalid format of 'get_resource' function, therefore this property"
              + " will be ignored in TOSCA translation.");
        }
      } else {
        Collection<Object> valCollection = ((Map) propertyValue).values();
        for (Object entryValue : valCollection) {
          String ret = extractResourceId(entryValue, translateTo);
          if (ret != null) {
            return ret;
          }

        }
      }
    } else if (propertyValue instanceof List) {
      for (Object prop : (List) propertyValue) {
        String ret = extractResourceId(prop, translateTo);
        if (ret != null) {
          return ret;
        }
      }
    }
    logger.warn("Heat resource: '" + translateTo.getResourceId() + "' with type: '"
        + translateTo.getResource().getType()
        + "' invalid format of property value, therefore this resource will be ignored in TOSCA "
        + "translation.");
    return null;
  }

  private RequirementAssignment createRequirementAssignment(String translatedNetworkResourceId) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    RequirementAssignment requirement = new RequirementAssignment();
    requirement.setCapability(ToscaCapabilityType.NATIVE_ATTACHMENT);
    requirement.setNode(translatedNetworkResourceId);
    requirement.setRelationship(ToscaRelationshipType.ATTACHES_TO);
    mdcDataDebugMessage.debugExitMessage(null, null);
    return requirement;
  }


}
