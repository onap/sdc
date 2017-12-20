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

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaGroupType;
import org.openecomp.sdc.tosca.datatypes.ToscaPolicyType;
import org.openecomp.sdc.tosca.datatypes.ToscaTopologyTemplateElements;
import org.openecomp.sdc.tosca.datatypes.model.GroupDefinition;
import org.openecomp.sdc.tosca.datatypes.model.PolicyDefinition;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class ResourceTranslationNovaServerGroupsImpl extends ResourceTranslationBase {
  private static final String AFFINITY = "affinity";
  private static final String ANTI_AFFINITY = "anti-affinity";
  private static List<String> supportedPolicies = Arrays.asList(AFFINITY, ANTI_AFFINITY);

  @Override
  protected String generateTranslatedId(TranslateTo translateTo) {
    return isEssentialRequirementsValid(translateTo) ? getTranslatedGroupId(
        translateTo.getResourceId()) : null;
  }

  @Override
  protected boolean isEssentialRequirementsValid(TranslateTo translateTo) {
    return validatePolicyType(translateTo);
  }

  @Override
  protected Optional<ToscaTopologyTemplateElements> getTranslatedToscaTopologyElement(
      TranslateTo translateTo) {
    if (isEssentialRequirementsValid(translateTo)) {
      return Optional.of(ToscaTopologyTemplateElements.GROUP);
    } else {
      return Optional.empty();
    }
  }

  private boolean validatePolicyType(TranslateTo translateTo) {
    Map<String, Object> properties = translateTo.getResource().getProperties();
    if (Objects.isNull(properties) || Objects.isNull(properties.get("policies"))) {
      return true;
    }

    Object policies = properties.get("policies");
    if (!(policies instanceof List)) {
      return false;
    }

    for (Object policy : (List) policies) {
      if (!isValidPolicyType(policy, translateTo.getResourceId(), translateTo.getResource())) {
        return false;
      }
    }
    return true;
  }

  private boolean isValidPolicyType(Object policy, String resourceId, Resource resource) {
    if (!(policy instanceof String)) {
      return false;
    }

    if ((policy instanceof String) && !supportedPolicies.contains(policy)) {
      logger.warn("Resource '" + resourceId + "'(" + resource.getType()
          + ")  contains unsupported policy '" + policy.toString()
          + "'. This resource is been ignored during the translation");
      return false;
    }

    return true;
  }

  @Override
  protected void translate(TranslateTo translateTo) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    String resourceId = translateTo.getResourceId();
    List<String> toscaPolicyTypes = getToscaPolicies(translateTo.getResource(), resourceId);
    if (!CollectionUtils.isEmpty(toscaPolicyTypes)) {
      String translatedGroupId = addGroupToTopology(translateTo, resourceId);
      addPoliciesToTopology(translateTo, translatedGroupId, toscaPolicyTypes);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void addPoliciesToTopology(TranslateTo translateTo, String policyTargetEntityId,
                                     List<String> toscaPolicyTypes) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    logger.info("******** Start creating policies for resource '%s' ********",
        translateTo.getResourceId());
    for (int i = 0; i < toscaPolicyTypes.size(); i++) {
      String policy = toscaPolicyTypes.get(i);
      logger.info("******** Creating policy '%s' ********", policy);
      PolicyDefinition policyDefinition = new PolicyDefinition();
      policyDefinition.setType(policy);
      policyDefinition.setTargets(Arrays.asList(policyTargetEntityId));
      policyDefinition.setProperties(TranslatorHeatToToscaPropertyConverter
          .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(),
              translateTo.getResourceId(),translateTo.getResource().getProperties(),
              policyDefinition.getProperties(), translateTo.getHeatFileName(),
              translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
              policyDefinition, translateTo.getContext()));
      policyDefinition.getProperties().put(
          policy.equals(ToscaPolicyType.PLACEMENT_ANTILOCATE) ? "container_type"
              : AFFINITY, "host");
      String policyId = getTranslatedPolicyId(translateTo, toscaPolicyTypes, i);
      DataModelUtil
          .addPolicyDefinition(translateTo.getServiceTemplate(), policyId, policyDefinition);
      logger.info("******** Policy '%s' created ********", policy);
    }

    logger
        .info("******** All policies for resource '%s' created successfully ********",
            translateTo.getResourceId());

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private String getTranslatedPolicyId(TranslateTo translateTo, List<String> toscaPolicyTypes,
                                       int policyIndex) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return translateTo.getResourceId() + (toscaPolicyTypes.size() > 1 ? policyIndex : "")
        + "_policy";
  }

  private String addGroupToTopology(TranslateTo translateTo, String resourceId) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    logger.info("******** Start creating group for resource '%s' ********", resourceId);
    GroupDefinition group = new GroupDefinition();
    group.setMembers(new ArrayList<>());
    group.setType(ToscaGroupType.NATIVE_ROOT);
    String translatedGroupId = getTranslatedGroupId(resourceId);
    DataModelUtil
        .addGroupDefinitionToTopologyTemplate(translateTo.getServiceTemplate(),
            translatedGroupId, group);
    logger.info("******** Creating group '%s' for resource '%s' ********", resourceId, resourceId);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return translatedGroupId;
  }

  private String getTranslatedGroupId(String resourceId) {
    return resourceId + "_group";
  }

  private List<String> getToscaPolicies(Resource resource, String resourceId) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Map<String, Object> properties = resource.getProperties();
    if (Objects.isNull(properties) || Objects.isNull(properties.get("policies"))) {
      return Arrays.asList(ToscaPolicyType.PLACEMENT_ANTILOCATE);
    }

    List<Object> policies = (List) properties.get("policies");
    List<String> retList = new ArrayList<>();
    policies.forEach(policy -> {
      if (isValidPolicyType(policy, resourceId, resource)) {
        retList.add(getToscaPolicyByHotPolicy(policy));
      }
    });

    mdcDataDebugMessage.debugExitMessage(null, null);
    return retList;
  }

  private String getToscaPolicyByHotPolicy(Object policy) {
    if (Objects.equals(policy, AFFINITY)) {
      return ToscaPolicyType.PLACEMENT_COLOCATE;
    } else {
      return ToscaPolicyType.PLACEMENT_ANTILOCATE;
    }
  }

}
