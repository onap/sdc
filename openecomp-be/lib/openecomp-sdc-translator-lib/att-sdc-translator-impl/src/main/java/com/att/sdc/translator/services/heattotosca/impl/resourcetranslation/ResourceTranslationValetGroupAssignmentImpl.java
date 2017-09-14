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

package com.att.sdc.translator.services.heattotosca.impl.resourcetranslation;

import com.att.sdc.tosca.datatypes.AttToscaPolicyType;
import com.google.common.base.Strings;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaGroupType;
import org.openecomp.sdc.tosca.datatypes.ToscaTopologyTemplateElements;
import org.openecomp.sdc.tosca.datatypes.model.GroupDefinition;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.PolicyDefinition;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.ResourceTranslationBase;
import org.openecomp.sdc.translator.services.heattotosca.mapping.TranslatorHeatToToscaPropertyConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil.getResource;

public class ResourceTranslationValetGroupAssignmentImpl extends ResourceTranslationBase {
  private static final String AFFINITY = "affinity";
  private static final String EXCLUSIVITY = "exclusivity";
  private static final String DIVERSITY = "diversity";
  private static List<String> supportedPolicies = Arrays.asList(AFFINITY, EXCLUSIVITY, DIVERSITY);

  private boolean validateGroupType(TranslateTo translateTo) {
    Map<String, Object> properties = translateTo.getResource().getProperties();
    if (properties == null) {
      return false;
    }

    Object groupType = properties.get("group_type");
    if (Objects.isNull(groupType)) {
      return false;
    }
    return isGroupTypeValid(groupType);
  }

  @Override
  protected void translate(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    String resourceId = translateTo.getResourceId();
    Optional<String> toscaPolicyType = getToscaPolicies(translateTo.getResource(), resourceId);
    if (toscaPolicyType.isPresent()) {
      List<String> members = getAttValetGroupAssignmentMembers(translateTo);
      addGroupToTopology(translateTo, resourceId, members);
      addPoliciesToTopology(translateTo, toscaPolicyType.get());
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  @Override
  protected String generateTranslatedId(TranslateTo translateTo) {
    return isEssentialRequirementsValid(translateTo) ? getValetGroupAssignmentTranslatedGroupId(
        translateTo) : null;
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

  @Override
  protected boolean isEssentialRequirementsValid(TranslateTo translateTo) {
    return validateGroupType(translateTo);
  }

  private void addPoliciesToTopology(TranslateTo translateTo, String policyType) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    ResourceTranslationBase.logger.info("******** Creating policy '%s' ********", policyType);
    PolicyDefinition policyDefinition = new PolicyDefinition();
    policyDefinition.setType(policyType);
    policyDefinition
        .setTargets(
            Collections.singletonList(getValetGroupAssignmentTranslatedGroupId(translateTo)));
    policyDefinition.setProperties(TranslatorHeatToToscaPropertyConverter
        .getToscaPropertiesSimpleConversion(translateTo.getServiceTemplate(),
            translateTo.getResourceId(),translateTo.getResource().getProperties(),
            policyDefinition.getProperties(), translateTo.getHeatFileName(),
            translateTo.getHeatOrchestrationTemplate(), translateTo.getResource().getType(),
            policyDefinition, translateTo.getContext()));

    DataModelUtil.addPolicyDefinition(translateTo.getServiceTemplate(),
        getTranslatedPolicyId(translateTo), policyDefinition);
    ResourceTranslationBase.logger.info("******** Policy '%s' created ********", policyType);
    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private String getTranslatedPolicyId(TranslateTo translateTo) {
    return translateTo.getResourceId() + "_policy";
  }

  private void addGroupToTopology(TranslateTo translateTo, String resourceId,
                                  List<String> members) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    ResourceTranslationBase.logger
        .info("******** Start creating group for resource '%s' ********", resourceId);
    GroupDefinition group = new GroupDefinition();
    group.setMembers(members);
    group.setType(ToscaGroupType.NATIVE_ROOT);
    String groupId = getValetGroupAssignmentTranslatedGroupId(translateTo);
    DataModelUtil
        .addGroupDefinitionToTopologyTemplate(translateTo.getServiceTemplate(), groupId, group);
    ResourceTranslationBase.logger
        .info("******** Creating group '%s' for resource '%s' ********", groupId, resourceId);
    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private List<String> getAttValetGroupAssignmentMembers(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Map<String, Object> properties = translateTo.getResource().getProperties();
    List<String> members = new ArrayList<>();
    String refResourceId;
    Optional<String> refAttachedResourceId;

    if (MapUtils.isNotEmpty(properties)) {
      Object resources = properties.get("resources");

      //if null warning no resource
      if (resources instanceof List) {
        for (Object member : ((List) resources)) {
          if (member instanceof Map) {
            refResourceId = (String) ((Map) member).get("get_resource");

            refAttachedResourceId =
                ResourceTranslationBase.getResourceTranslatedId(translateTo.getHeatFileName(),
                    translateTo.getHeatOrchestrationTemplate(), refResourceId,
                    translateTo.getContext());
            if (refAttachedResourceId.isPresent() ) {
              members.add(refAttachedResourceId.get());
              updateComputeConsolidationDataGroup(translateTo, refResourceId);
            }
          }
        }
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return members;
  }

  private void updateComputeConsolidationDataGroup(TranslateTo translateTo,
                                                   String refResourceId) {
    Resource refResource =
        getResource(translateTo.getHeatOrchestrationTemplate(), refResourceId,
            translateTo.getHeatFileName());
    if (isNovaServerResource(translateTo.getHeatOrchestrationTemplate(), refResourceId)) {
      String heatFileName = translateTo.getHeatFileName();
      Optional<String> translatedNovaServerId =
          ResourceTranslationFactory.getInstance(refResource)
              .translateResource(heatFileName, translateTo.getServiceTemplate(),
                  translateTo.getHeatOrchestrationTemplate(), refResource, refResourceId,
                  translateTo.getContext());
      //Add nova server information to compute data
      if (translatedNovaServerId.isPresent()) {
        NodeTemplate translatedNovaServerNodeTemplate = DataModelUtil
            .getNodeTemplate(translateTo.getServiceTemplate(),
                translatedNovaServerId.get());
        if (translatedNovaServerNodeTemplate != null) {
          ComputeTemplateConsolidationData computeTemplateConsolidationData = ConsolidationDataUtil
              .getComputeTemplateConsolidationData(translateTo.getContext(), translateTo
                      .getServiceTemplate(), translatedNovaServerNodeTemplate.getType(),
                      translatedNovaServerId.get());
          ConsolidationDataUtil.updateGroupIdInConsolidationData(computeTemplateConsolidationData,
              translateTo.getTranslatedId());
        }
      }
    }
  }

  private boolean isNovaServerResource(HeatOrchestrationTemplate heatOrchestrationTemplate,
                                              Object resourceToTranslate) {
    return heatOrchestrationTemplate.getResources().get(resourceToTranslate).getType()
        .equals(HeatResourcesTypes.NOVA_SERVER_RESOURCE_TYPE.getHeatResource());
  }


  private Optional<String> getToscaPolicies(Resource resource, String resourceId) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    Map<String, Object> properties = resource.getProperties();
    if (Objects.isNull(properties) || Objects.isNull(properties.get("group_type"))) {
      ResourceTranslationBase.logger
          .warn("Resource '" + resourceId + "'(" + resource.getType() + ")  missing group_type");
      return Optional.empty();
    }

    Object groupType = properties.get("group_type");
    if (!isGroupTypeValid(groupType)) {
      ResourceTranslationBase.logger.warn("Resource '" + resourceId + "'(" + resource.getType()
          + ")  contains unsupported policy '" + groupType
          + "'. This resource is been ignored during the translation");

      mdcDataDebugMessage.debugExitMessage(null, null);
      return Optional.empty();
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return getToscaPolicyByGroupType((String) groupType);

  }

  private boolean isGroupTypeValid(Object groupType) {
    if (!(groupType instanceof String)) {
      return false;
    }
    return supportedPolicies.contains(groupType);
  }

  private Optional<String> getToscaPolicyByGroupType(String groupType) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    String toscaPolicyType;
    switch (groupType) {
      case AFFINITY:
        toscaPolicyType = AttToscaPolicyType.PLACEMENT_VALET_AFFINITY;
        break;
      case EXCLUSIVITY:
        toscaPolicyType = AttToscaPolicyType.PLACEMENT_VALET_EXCLUSIVITY;
        break;
      default:
        toscaPolicyType = AttToscaPolicyType.PLACEMENT_VALET_DIVERSITY;
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return Optional.of(toscaPolicyType);
  }


  private String getValetGroupAssignmentTranslatedGroupId(TranslateTo translateTo) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    String resourceId = super.generateTranslatedId(translateTo);
    String groupName = null;
    Map<String, Object> properties = translateTo.getResource().getProperties();

    if (properties == null) {
      return resourceId + "_group";
    }

    Object groupNameProperty = properties.get("group_name");
    if (groupNameProperty instanceof String) {
      groupName = (String) groupNameProperty;
    }

    if (groupName != null && !Strings.isNullOrEmpty(groupName)) {
      groupName = groupName.replace(" ", "_");
      resourceId += "_" + groupName;

    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return resourceId + "_group";
  }
}
