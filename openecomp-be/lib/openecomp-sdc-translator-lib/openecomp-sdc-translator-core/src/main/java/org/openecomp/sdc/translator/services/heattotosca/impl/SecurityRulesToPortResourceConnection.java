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


import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;


class SecurityRulesToPortResourceConnection extends BaseResourceConnection<CapabilityDefinition> {
  SecurityRulesToPortResourceConnection(ResourceTranslationNestedImpl resourceTranslationNested,
                                        TranslateTo translateTo, FileData nestedFileData,
                                        NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
    super(resourceTranslationNested, translateTo, nestedFileData, substitutionNodeTemplate,
        nodeType);
  }

  @Override
  protected boolean isDesiredNodeTemplateType(NodeTemplate nodeTemplate) {
    return nodeTemplate.getType().equals(ToscaNodeType.NEUTRON_PORT.getDisplayName());
  }

  @Override
  protected List<Predicate<CapabilityDefinition>> getPredicatesListForConnectionPoints() {
    ArrayList<Predicate<CapabilityDefinition>> predicates = new ArrayList<>();
    predicates.add(cap -> cap.getType().equals(ToscaCapabilityType.ATTACHMENT.getDisplayName()));
    return predicates;
  }

  @Override
  protected Optional<List<String>> getConnectorParamName(
          String heatResourceId,Resource heatResource,
          HeatOrchestrationTemplate nestedHeatOrchestrationTemplate) {
    Object securityGroups =
        heatResource.getProperties().get(Constants.SECURITY_GROUPS_PROPERTY_NAME);
    List<String> paramsList = new ArrayList<>();
    if (securityGroups instanceof List) {
      ((List) securityGroups).forEach(group -> {
        Optional<AttachedResourceId> attachedResourceId = HeatToToscaUtil
            .extractAttachedResourceId(nestedFileData.getFile(), nestedHeatOrchestrationTemplate,
                translateTo.getContext(), group);
        if (attachedResourceId.isPresent()) {
          paramsList.add((String) attachedResourceId.get().getEntityId());
        }
      });
      return Optional.of(paramsList);
    }
    return Optional.empty();
  }

  @Override
  protected String getDesiredResourceType() {
    return HeatResourcesTypes.NEUTRON_PORT_RESOURCE_TYPE.getHeatResource();
  }

  @Override
  protected List<Map<String, CapabilityDefinition>> getAllConnectionPoints() {
    List<Map<String, CapabilityDefinition>> exposedRequirementsList = new ArrayList<>();
    List<Predicate<CapabilityDefinition>> predicates = getPredicatesListForConnectionPoints();
    Map<String, CapabilityDefinition> capabilities = this.nodeType.getCapabilities();
    if (capabilities == null) {
      return exposedRequirementsList;
    }
    capabilities.entrySet()
        .stream()
        .filter(entry -> predicates
            .stream()
            .anyMatch(p -> p.test(entry.getValue())))
        .forEach(entry -> {
          Map<String, CapabilityDefinition> exposedRequirementsMap = new HashMap<>();
          exposedRequirementsMap.put(entry.getKey(), entry.getValue());
          exposedRequirementsList.add(exposedRequirementsMap);
        });

    return exposedRequirementsList;
  }

  @Override
  void addRequirementToConnectResources(Map.Entry<String, CapabilityDefinition> entry,
                                        List<String> paramNames) {
    paramNames.forEach(p -> {
      Optional<AttachedResourceId> attachedResourceId =
          HeatToToscaUtil.extractAttachedResourceId(translateTo, p);
      String securityRulesNodeId;
      if (!attachedResourceId.isPresent()) {
        return;
      }
      Map.Entry<String, RequirementDefinition> requirementDefinition =
          createRequirementDefinition(entry.getKey());
      AttachedResourceId securityGroupAttachedId = attachedResourceId.get();
      if (securityGroupAttachedId.isGetResource()) {
        String securityGroupResourceId = (String) attachedResourceId.get().getEntityId();
        Resource securityGroupResource = HeatToToscaUtil
            .getResource(translateTo.getHeatOrchestrationTemplate(), securityGroupResourceId,
                translateTo.getHeatFileName());
        Optional<String> translatedSecurityRuleId =
            ResourceTranslationFactory.getInstance(securityGroupResource)
                .translateResource(translateTo.getHeatFileName(), translateTo.getServiceTemplate(),
                    translateTo.getHeatOrchestrationTemplate(), securityGroupResource,
                    securityGroupResourceId, translateTo.getContext());
        if (translatedSecurityRuleId.isPresent()) {
          NodeTemplate securityRuleNodeTemplate = DataModelUtil
              .getNodeTemplate(translateTo.getServiceTemplate(), translatedSecurityRuleId.get());
          createRequirementAssignment(requirementDefinition, translateTo.getTranslatedId(),
              securityRuleNodeTemplate);
        } else {
          logger.warn(
              securityGroupResource.getType() + "connection to " + securityGroupResource.getType()
                  + " is not supported/invalid, therefore this connection "
                  +   "will be ignored in the TOSCA translation");
        }
      } else if (securityGroupAttachedId.isGetParam()) {
        TranslatedHeatResource shareResource =
            translateTo.getContext().getHeatSharedResourcesByParam()
                .get(securityGroupAttachedId.getEntityId());
        if (Objects.nonNull(shareResource)
            && !HeatToToscaUtil.isHeatFileNested(translateTo, translateTo.getHeatFileName())) {
          NodeTemplate sharedNodeTemplate = DataModelUtil
              .getNodeTemplate(translateTo.getServiceTemplate(), shareResource.getTranslatedId());
          createRequirementAssignment(requirementDefinition, translateTo.getTranslatedId(),
              sharedNodeTemplate);
        }
      }
    });
  }

  @Override
  protected String getTranslatedResourceIdFromSubstitutionMapping(
      ServiceTemplate nestedServiceTemplate, Map.Entry<String, CapabilityDefinition> entry) {
    List<String> substitutionMapping =
        nestedServiceTemplate.getTopology_template().getSubstitution_mappings().getCapabilities()
            .get(entry.getKey());
    return substitutionMapping.get(0);
  }

  private Map.Entry<String, RequirementDefinition> createRequirementDefinition(String key) {
    RequirementDefinition definition = new RequirementDefinition();
    definition.setCapability(key);
    definition.setRelationship(ToscaRelationshipType.ATTACHES_TO.getDisplayName());
    return new Map.Entry<String, RequirementDefinition>() {
      @Override
      public String getKey() {
        return ToscaConstants.PORT_REQUIREMENT_ID;
      }

      @Override
      public RequirementDefinition getValue() {
        return definition;
      }

      @Override
      public RequirementDefinition setValue(RequirementDefinition value) {
        return null;
      }
    };
  }


}
