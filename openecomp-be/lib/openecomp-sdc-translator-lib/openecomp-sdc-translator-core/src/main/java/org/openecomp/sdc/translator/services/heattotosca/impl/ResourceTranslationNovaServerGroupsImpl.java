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

package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaGroupType;
import org.openecomp.sdc.tosca.datatypes.ToscaPolicyType;
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

public class ResourceTranslationNovaServerGroupsImpl extends ResourceTranslationBase {
  private static final String AFFINITY = "affinity";
  private static final String ANTI_AFFINITY = "anti-affinity";
  private static List<String> supportedPolicies = Arrays.asList(AFFINITY, ANTI_AFFINITY);

  @Override
  protected void translate(TranslateTo translateTo) {
    String resourceId = translateTo.getResourceId();
    List<String> toscaPolicyTypes = getToscaPolicies(translateTo.getResource(), resourceId);
    if (!CollectionUtils.isEmpty(toscaPolicyTypes)) {
      addGroupToTopology(translateTo, resourceId);
      addPoliciesToTopology(translateTo, resourceId, toscaPolicyTypes);
    }
  }

  private void addPoliciesToTopology(TranslateTo translateTo, String resourceId,
                                     List<String> toscaPolicyTypes) {
    logger.info("******** Start creating policies for resource '%s' ********", resourceId);
    for (int i = 0; i < toscaPolicyTypes.size(); i++) {
      String policy = toscaPolicyTypes.get(i);
      logger.info("******** Creating policy '%s' ********", policy);
      PolicyDefinition policyDefinition = new PolicyDefinition();
      policyDefinition.setType(policy);
      policyDefinition.setTargets(Arrays.asList(resourceId));
      policyDefinition.setProperties(TranslatorHeatToToscaPropertyConverter
          .getToscaPropertiesSimpleConversion(translateTo.getResource().getProperties(),
              policyDefinition.getProperties(), translateTo.getHeatFileName(),
              translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
              policyDefinition, translateTo.getContext()));
      policyDefinition.getProperties().put(
          policy.equals(ToscaPolicyType.PLACEMENT_ANTILOCATE.getDisplayName()) ? "container_type"
              : AFFINITY, "host");
      String policyId = resourceId + (toscaPolicyTypes.size() > 1 ? i : "");
      DataModelUtil
          .addPolicyDefinition(translateTo.getServiceTemplate(), policyId, policyDefinition);
      logger.info("******** Policy '%s' created ********", policy);
    }

    logger
        .info("******** All policies for resource '%s' created successfully ********", resourceId);
  }

  private void addGroupToTopology(TranslateTo translateTo, String resourceId) {
    logger.info("******** Start creating group for resource '%s' ********", resourceId);
    GroupDefinition group = new GroupDefinition();
    group.setMembers(new ArrayList<>());
    group.setType(ToscaGroupType.ROOT.getDisplayName());
    DataModelUtil
        .addGroupDefinitionToTopologyTemplate(translateTo.getServiceTemplate(), resourceId, group);
    logger.info("******** Creating group '%s' for resource '%s' ********", resourceId, resourceId);
  }

  private List<String> getToscaPolicies(Resource resource, String resourceId) {

    Map<String, Object> properties = resource.getProperties();
    if (Objects.isNull(properties) || Objects.isNull(properties.get("policies"))) {
      return Arrays.asList(ToscaPolicyType.PLACEMENT_ANTILOCATE.getDisplayName());
    }

    List policies = (List) properties.get("policies");
    List<String> retList = new ArrayList<>();
    policies.forEach(policy -> {
      if (!supportedPolicies.contains(policy)) {
        logger.warn("Resource '" + resourceId + "'(" + resource.getType()
            + ")  contains unsupported policy '" + policy.toString()
            + "'. This resource is been ignored during the translation");
      } else {
        retList.add(getToscaPolicyByHotPolicy((String) policy));
      }
    });
    return retList;
  }

  private String getToscaPolicyByHotPolicy(String name) {
    if (Objects.equals(name, AFFINITY)) {
      return ToscaPolicyType.PLACEMENT_COLOCATE.getDisplayName();
    } else {
      return ToscaPolicyType.PLACEMENT_ANTILOCATE.getDisplayName();
    }
  }
}
