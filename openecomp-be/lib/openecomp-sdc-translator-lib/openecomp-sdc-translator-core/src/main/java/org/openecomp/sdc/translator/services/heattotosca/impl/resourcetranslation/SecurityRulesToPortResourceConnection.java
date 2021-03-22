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

import static org.openecomp.sdc.translator.services.heattotosca.HeatToToscaLogConstants.LOG_UNSUPPORTED_SECURITY_RULE_PORT_CAPABILITY_CONNECTION;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import org.onap.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.NodeType;
import org.onap.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.HeatResourcesTypes;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaRelationshipType;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.AttachedResourceId;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;

class SecurityRulesToPortResourceConnection extends ResourceConnectionUsingCapabilityHelper {

    SecurityRulesToPortResourceConnection(ResourceTranslationNestedImpl resourceTranslationNested, TranslateTo translateTo, FileData nestedFileData,
                                          NodeTemplate substitutionNodeTemplate, NodeType nodeType) {
        super(resourceTranslationNested, translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
    }

    @Override
    protected boolean isDesiredNodeTemplateType(NodeTemplate nodeTemplate) {
        return nodeTemplate.getType().equals(ToscaNodeType.NEUTRON_PORT);
    }

    @Override
    protected List<Predicate<CapabilityDefinition>> getPredicatesListForConnectionPoints() {
        ArrayList<Predicate<CapabilityDefinition>> predicates = new ArrayList<>(1);
        predicates.add(cap -> cap.getType().equals(ToscaCapabilityType.NATIVE_ATTACHMENT));
        return predicates;
    }

    @Override
    protected Optional<List<String>> getConnectorPropertyParamName(String heatResourceId, Resource heatResource,
                                                                   HeatOrchestrationTemplate nestedHeatOrchestrationTemplate,
                                                                   String nestedHeatFileName) {
        Object securityGroups = heatResource.getProperties().get(Constants.SECURITY_GROUPS_PROPERTY_NAME);
        List<String> paramsList = new ArrayList<>();
        if (securityGroups instanceof List) {
            ((List) securityGroups).forEach(group -> {
                Optional<AttachedResourceId> attachedResourceId = HeatToToscaUtil
                    .extractAttachedResourceId(nestedFileData.getFile(), nestedHeatOrchestrationTemplate, translateTo.getContext(), group);
                if (attachedResourceId.isPresent() && attachedResourceId.get().isGetParam() && attachedResourceId.get()
                    .getEntityId() instanceof String) {
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
    void addRequirementToConnectResources(Map.Entry<String, CapabilityDefinition> connectionPointEntry, List<String> paramNames) {
        if (paramNames == null || paramNames.isEmpty()) {
            return;
        }
        List<String> supportedSecurityRulesTypes = Collections
            .singletonList(HeatResourcesTypes.NEUTRON_SECURITY_GROUP_RESOURCE_TYPE.getHeatResource());
        for (String paramName : paramNames) {
            addRequirementToConnectResource(connectionPointEntry, supportedSecurityRulesTypes, paramName);
        }
    }

    @Override
    boolean validateResourceTypeSupportedForReqCreation(String nestedResourceId, String nestedPropertyName, String connectionPointId,
                                                        Resource connectedResource, List<String> supportedTypes) {
        if (resourceTranslationBase.isUnsupportedResourceType(connectedResource, supportedTypes)) {
            logger.warn(LOG_UNSUPPORTED_SECURITY_RULE_PORT_CAPABILITY_CONNECTION, nestedResourceId, nestedPropertyName, connectedResource.getType(),
                connectionPointId, supportedTypes.toString());
            return false;
        }
        return true;
    }

    @Override
    Map.Entry<String, RequirementDefinition> createRequirementDefinition(String capabilityKey) {
        RequirementDefinition definition = new RequirementDefinition();
        definition.setCapability(capabilityKey);
        definition.setRelationship(ToscaRelationshipType.ATTACHES_TO);
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
