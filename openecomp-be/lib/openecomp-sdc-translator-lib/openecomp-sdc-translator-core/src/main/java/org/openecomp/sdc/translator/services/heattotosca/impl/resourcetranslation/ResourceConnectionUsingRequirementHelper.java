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

import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_NESTED_RESOURCE_PROPERTY_NOT_DEFINED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;

abstract class ResourceConnectionUsingRequirementHelper extends BaseResourceConnection<RequirementDefinition> {

    ResourceConnectionUsingRequirementHelper(ResourceTranslationBase resourceTranslationBase, TranslateTo translateTo, FileData nestedFileData,
                                             NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
        super(resourceTranslationBase, translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
    }

    @Override
    String getMappedNodeTranslatedResourceId(ServiceTemplate nestedServiceTemplate, Map.Entry<String, RequirementDefinition> connectionPointEntry) {
        List<String> substitutionMapping = nestedServiceTemplate.getTopology_template().getSubstitution_mappings().getRequirements()
            .get(connectionPointEntry.getKey());
        return substitutionMapping.get(0);
    }

    @Override
    Map.Entry<String, RequirementDefinition> getMappedConnectionPointEntry(ServiceTemplate nestedServiceTemplate,
                                                                           Map.Entry<String, RequirementDefinition> connectionPointEntry) {
        List<String> substitutionMapping = nestedServiceTemplate.getTopology_template().getSubstitution_mappings().getRequirements()
            .get(connectionPointEntry.getKey());
        String mappedNodeTranslatedId = substitutionMapping.get(0);
        String mappedReqId = substitutionMapping.get(1);
        NodeTemplate mappedNodeTemplate = nestedServiceTemplate.getTopology_template().getNode_templates().get(mappedNodeTranslatedId);
        NodeType substituteNodeType = translateTo.getContext().getGlobalSubstitutionServiceTemplate().getNode_types()
            .get(mappedNodeTemplate.getType());
        Optional<RequirementDefinition> requirementDefinition = DataModelUtil.getRequirementDefinition(substituteNodeType, mappedReqId);
        return new Map.Entry<String, RequirementDefinition>() {
            @Override
            public String getKey() {
                return mappedReqId;
            }

            @Override
            public RequirementDefinition getValue() {
                return requirementDefinition.orElse(null);
            }

            @Override
            public RequirementDefinition setValue(RequirementDefinition value) {
                return null;
            }
        };
    }

    @Override
    List<Map<String, RequirementDefinition>> getAllConnectionPoints() {
        List<Map<String, RequirementDefinition>> exposedRequirementsList = new ArrayList<>();
        List<Predicate<RequirementDefinition>> predicates = getPredicatesListForConnectionPoints();
        List<Map<String, RequirementDefinition>> requirements = this.nodeType.getRequirements();
        if (requirements == null) {
            return exposedRequirementsList;
        }
        requirements.stream().map(Map::entrySet)
            .forEach(x -> x.stream().filter(entry -> predicates.stream().anyMatch(p -> p.test(entry.getValue()))).forEach(entry -> {
                Map<String, RequirementDefinition> exposedRequirementsMap = new HashMap<>();
                exposedRequirementsMap.put(entry.getKey(), entry.getValue());
                exposedRequirementsList.add(exposedRequirementsMap);
            }));
        return exposedRequirementsList;
    }

    void addRequirementToConnectResource(Map.Entry<String, RequirementDefinition> requirementDefinitionEntry, String paramName, Object paramValue,
                                         List<String> supportedNetworkTypes) {
        if (paramValue == null) {
            logger.warn(LOG_NESTED_RESOURCE_PROPERTY_NOT_DEFINED, paramName, translateTo.getResourceId(), requirementDefinitionEntry.getKey(),
                ToscaConstants.REQUIREMENT);
            return;
        }
        Optional<String> targetTranslatedNodeId = getConnectionTranslatedNodeUsingGetResourceFunc(requirementDefinitionEntry, paramName, paramValue,
            supportedNetworkTypes);
        if (targetTranslatedNodeId.isPresent()) {
            createRequirementAssignment(requirementDefinitionEntry, targetTranslatedNodeId.get(), substitutionNodeTemplate);
        } else {
            targetTranslatedNodeId = getConnectionTranslatedNodeUsingGetParamFunc(requirementDefinitionEntry, paramName, supportedNetworkTypes);
            targetTranslatedNodeId.ifPresent(
                targetTranslatedId -> createRequirementAssignment(requirementDefinitionEntry, targetTranslatedId, substitutionNodeTemplate));
        }
    }
}
