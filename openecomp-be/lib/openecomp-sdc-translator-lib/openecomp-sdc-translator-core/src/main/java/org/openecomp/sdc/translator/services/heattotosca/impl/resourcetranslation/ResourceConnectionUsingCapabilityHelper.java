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
import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_UNSUPPORTED_CAPABILITY_CONNECTION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.onap.sdc.tosca.datatypes.model.*;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslatedHeatResource;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.ResourceTranslationFactory;

public abstract class ResourceConnectionUsingCapabilityHelper extends BaseResourceConnection<CapabilityDefinition> {

    ResourceConnectionUsingCapabilityHelper(ResourceTranslationBase resourceTranslationBase,
                                            TranslateTo translateTo, FileData nestedFileData,
                                            NodeTemplate substitutionNodeTemplate,
                                            NodeType nodeType) {
        super(resourceTranslationBase, translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
    }

    abstract Map.Entry<String, RequirementDefinition> createRequirementDefinition(
            String capabilityKey);

    @Override
    String getMappedNodeTranslatedResourceId(ServiceTemplate nestedServiceTemplate,
                                             Map.Entry<String,
                                                     CapabilityDefinition> connectionPointEntry) {
        List<String> substitutionMapping =
                nestedServiceTemplate.getTopology_template().getSubstitution_mappings().getCapabilities()
                        .get(connectionPointEntry.getKey());
        return substitutionMapping.get(0);
    }

    @Override
    Map.Entry<String, CapabilityDefinition> getMappedConnectionPointEntry(
            ServiceTemplate nestedServiceTemplate,
            Map.Entry<String, CapabilityDefinition> connectionPointEntry) {
        List<String> substitutionMapping =
                nestedServiceTemplate.getTopology_template().getSubstitution_mappings().getCapabilities()
                        .get(connectionPointEntry.getKey());
        String mappedNodeTranslatedId = substitutionMapping.get(0);
        String mappedCapabilityId = substitutionMapping.get(1);
        NodeTemplate mappedNodeTemplate =
                nestedServiceTemplate.getTopology_template().getNode_templates()
                        .get(mappedNodeTranslatedId);
        NodeType substituteNodeType =
                translateTo.getContext().getGlobalSubstitutionServiceTemplate().getNode_types()
                        .get(mappedNodeTemplate.getType());
        Optional<CapabilityDefinition> capabilityDefinition =
                DataModelUtil.getCapabilityDefinition(substituteNodeType, mappedCapabilityId);
        return new Map.Entry<String, CapabilityDefinition>() {
            @Override
            public String getKey() {
                return mappedCapabilityId;
            }

            @Override
            public CapabilityDefinition getValue() {
                return capabilityDefinition.orElse(null);
            }

            @Override
            public CapabilityDefinition setValue(CapabilityDefinition value) {
                return null;
            }
        };
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

    void addRequirementToConnectResource(Map.Entry<String, CapabilityDefinition> connectionPointEntry,
                                         List<String> supportedSourceNodeTypes, String paramName) {
        Object paramValue = translateTo.getResource().getProperties().get(paramName);
        if (paramValue == null) {
            logger.warn(LOG_NESTED_RESOURCE_PROPERTY_NOT_DEFINED, paramName,
                    translateTo.getResourceId(), connectionPointEntry.getKey(), ToscaConstants.CAPABILITY);
            return;
        }

        Map.Entry<String, RequirementDefinition> requirementDefinition =
                createRequirementDefinition(connectionPointEntry.getKey());

        Optional<String> sourceResourceId =
                getConnectionResourceUsingGetResourceFunc(connectionPointEntry, paramName, paramValue,
                        supportedSourceNodeTypes);
        if (sourceResourceId.isPresent()) {
            Resource sourceResource = HeatToToscaUtil
                    .getResource(translateTo.getHeatOrchestrationTemplate(), sourceResourceId.get(),
                            translateTo.getHeatFileName());
            Optional<String> translatedSourceNodeId =
                    ResourceTranslationFactory.getInstance(sourceResource)
                            .translateResource(translateTo.getHeatFileName(), translateTo.getServiceTemplate(),
                                    translateTo.getHeatOrchestrationTemplate(), sourceResource,
                                    sourceResourceId.get(), translateTo.getContext());
            if (translatedSourceNodeId.isPresent()) {
                NodeTemplate sourceNodeTemplate = DataModelUtil
                        .getNodeTemplate(translateTo.getServiceTemplate(), translatedSourceNodeId.get());
                RequirementAssignment requirementAssignment = createRequirementAssignment(
                        requirementDefinition, translateTo.getTranslatedId(), sourceNodeTemplate);
                ConsolidationDataUtil.updateNodesConnectedData(translateTo, translateTo.getResourceId(),
                        translateTo.getResource(), sourceResource, translatedSourceNodeId.get(),
                        requirementDefinition.getKey(), requirementAssignment);
            } else {
                logger.warn(LOG_UNSUPPORTED_CAPABILITY_CONNECTION, sourceResource.getType(),
                        connectionPointEntry.getKey(),
                        connectionPointEntry.getValue().getType());
            }
        } else {
            Optional<TranslatedHeatResource> sharedSourceTranslatedHeatResource =
                    getConnectionTranslatedHeatResourceUsingGetParamFunc(connectionPointEntry, paramName,
                            supportedSourceNodeTypes);
            if (sharedSourceTranslatedHeatResource.isPresent()) {
                NodeTemplate sharedSourceNodeTemplate = DataModelUtil
                        .getNodeTemplate(translateTo.getServiceTemplate(),
                                sharedSourceTranslatedHeatResource.get().getTranslatedId());
                RequirementAssignment requirementAssignment = createRequirementAssignment(
                        requirementDefinition, translateTo.getTranslatedId(), sharedSourceNodeTemplate);

                ConsolidationDataUtil.updateNodesConnectedData(translateTo, translateTo.getResourceId(),
                        translateTo.getResource(), sharedSourceTranslatedHeatResource.get().getHeatResource(),
                        sharedSourceTranslatedHeatResource.get().getTranslatedId(),
                        requirementDefinition.getKey(),
                        requirementAssignment);
            }
        }
    }

}
