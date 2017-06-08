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

import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class ResourceConnectionUsingRequirementHelper
    extends BaseResourceConnection<RequirementDefinition> {
  public ResourceConnectionUsingRequirementHelper(ResourceTranslationBase resourceTranslationBase,
                                                  TranslateTo translateTo, FileData nestedFileData,
                                                  NodeTemplate substitutionNodeTemplate,
                                                  NodeType nodeType) {
    super(resourceTranslationBase, translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
  }

  @Override
  String getMappedNodeTranslatedResourceId(ServiceTemplate nestedServiceTemplate,
                                           Map.Entry<String,
                                               RequirementDefinition> connectionPointEntry) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    List<String> substitutionMapping =
        nestedServiceTemplate.getTopology_template().getSubstitution_mappings().getRequirements()
            .get(connectionPointEntry.getKey());

    mdcDataDebugMessage.debugExitMessage(null, null);
    return substitutionMapping.get(0);
  }

  @Override
  Map.Entry<String, RequirementDefinition> getMappedConnectionPointEntry(
      ServiceTemplate nestedServiceTemplate,
      Map.Entry<String, RequirementDefinition> connectionPointEntry) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    List<String> substitutionMapping =
        nestedServiceTemplate.getTopology_template().getSubstitution_mappings().getRequirements()
            .get(connectionPointEntry.getKey());
    String mappedNodeTranslatedId = substitutionMapping.get(0);
    String mappedReqId = substitutionMapping.get(1);
    NodeTemplate mappedNodeTemplate =
        nestedServiceTemplate.getTopology_template().getNode_templates()
            .get(mappedNodeTranslatedId);
    NodeType substituteNodeType =
        translateTo.getContext().getGlobalSubstitutionServiceTemplate().getNode_types()
            .get(mappedNodeTemplate.getType());
    Optional<RequirementDefinition> requirementDefinition =
        DataModelUtil.getRequirementDefinition(substituteNodeType, mappedReqId);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return new Map.Entry<String, RequirementDefinition>() {
      @Override
      public String getKey() {
        return mappedReqId;
      }

      @Override
      public RequirementDefinition getValue() {
        return requirementDefinition.get();
      }

      @Override
      public RequirementDefinition setValue(RequirementDefinition value) {
        return null;
      }
    };
  }

  @Override
  List<Map<String, RequirementDefinition>> getAllConnectionPoints() {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    List<Map<String, RequirementDefinition>> exposedRequirementsList = new ArrayList<>();
    List<Predicate<RequirementDefinition>> predicates = getPredicatesListForConnectionPoints();
    List<Map<String, RequirementDefinition>> requirements = this.nodeType.getRequirements();
    if (requirements == null) {
      return exposedRequirementsList;
    }
    requirements.stream()
        .map(Map::entrySet)
        .forEach(x -> x.stream()
            .filter(entry -> predicates
                .stream()
                .anyMatch(p -> p.test(entry.getValue())))
            .forEach(entry -> {
              Map<String, RequirementDefinition> exposedRequirementsMap = new HashMap<>();
              exposedRequirementsMap.put(entry.getKey(), entry.getValue());
              exposedRequirementsList.add(exposedRequirementsMap);
            }));

    mdcDataDebugMessage.debugExitMessage(null, null);
    return exposedRequirementsList;
  }

  void addRequirementToConnectResource(
      Map.Entry<String, RequirementDefinition> requirementDefinitionEntry, String paramName,
      Object paramValue, List<String> supportedNetworkTypes) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    if (paramValue == null) {
      logger.warn("'" + paramName + "' property is not define in nested resource '"
          + translateTo.getResourceId() + "' for the nested heat file, therefore, '"
          + requirementDefinitionEntry.getKey() + "' TOSCA requirement will not be connected.");
      return;
    }
    Optional<String> targetTranslatedNodeId =
        getConnectionTranslatedNodeUsingGetResourceFunc(requirementDefinitionEntry, paramName,
            paramValue, supportedNetworkTypes);
    if (targetTranslatedNodeId.isPresent()) {
      createRequirementAssignment(requirementDefinitionEntry, targetTranslatedNodeId.get(),
          substitutionNodeTemplate);
    } else {
      targetTranslatedNodeId =
          getConnectionTranslatedNodeUsingGetParamFunc(requirementDefinitionEntry, paramName,
              supportedNetworkTypes);
      targetTranslatedNodeId
          .ifPresent(targetTranslatedId -> createRequirementAssignment(requirementDefinitionEntry,
              targetTranslatedId, substitutionNodeTemplate));
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }
}
