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

package org.openecomp.sdc.tosca.services;

import java.io.InputStream;
import org.onap.sdc.tosca.datatypes.model.*;
import org.openecomp.sdc.tosca.datatypes.ToscaElementTypes;
import org.openecomp.sdc.tosca.datatypes.ToscaFlatData;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ToscaAnalyzerService {

    /*
        node template with type equal to node type or derived from node type
         */
    Map<String, NodeTemplate> getNodeTemplatesByType(ServiceTemplate serviceTemplate, String nodeType,
                                                            ToscaServiceModel toscaServiceModel);

    Optional<NodeType> fetchNodeType(String nodeTypeKey, Collection<ServiceTemplate> serviceTemplates);

    boolean isTypeOf(NodeTemplate nodeTemplate, String nodeType, ServiceTemplate serviceTemplate,
                            ToscaServiceModel toscaServiceModel);

    boolean isTypeOf(InterfaceDefinitionType interfaceDefinition, String interfaceType, ServiceTemplate serviceTemplate,
                            ToscaServiceModel toscaServiceModel);

    boolean isTypeOf(DefinitionOfDataType parameterDefinition, String dataType, ServiceTemplate serviceTemplate,
                            ToscaServiceModel toscaServiceModel);

    boolean isTypeOf(CapabilityDefinition capabilityDefinition, String capabilityType, ServiceTemplate serviceTemplate,
                            ToscaServiceModel toscaServiceModel);

    List<RequirementAssignment> getRequirements(NodeTemplate nodeTemplate, String requirementId);

    Optional<NodeTemplate> getNodeTemplateById(ServiceTemplate serviceTemplate, String nodeTemplateId);

    Optional<String> getSubstituteServiceTemplateName(String substituteNodeTemplateId,
                                                             NodeTemplate substitutableNodeTemplate);

    Map<String, NodeTemplate> getSubstitutableNodeTemplates(ServiceTemplate serviceTemplate);

    Optional<Map.Entry<String, NodeTemplate>> getSubstitutionMappedNodeTemplateByExposedReq(String substituteServiceTemplateFileName,
                                                                                                   ServiceTemplate substituteServiceTemplate,
                                                                                                   String requirementId);

    /*
        match only for the input which is not null
         */
    boolean isDesiredRequirementAssignment(RequirementAssignment requirementAssignment, String capability, String node,
                                                  String relationship);

    ToscaFlatData getFlatEntity(ToscaElementTypes elementType, String type, ServiceTemplate serviceTemplate,
                                       ToscaServiceModel toscaModel);

    boolean isSubstitutableNodeTemplate(NodeTemplate nodeTemplate);

    NodeType createInitSubstitutionNodeType(ServiceTemplate substitutionServiceTemplate,
                                                   String nodeTypeDerivedFromValue);

    boolean isRequirementExistInNodeTemplate(NodeTemplate nodeTemplate, String requirementId,
                                                    RequirementAssignment requirementAssignment);

    Map<String, PropertyDefinition> manageSubstitutionNodeTypeProperties(ServiceTemplate substitutionServiceTemplate);

    Map<String, CapabilityDefinition> calculateExposedCapabilities(Map<String, CapabilityDefinition> nodeTypeCapabilitiesDefinition,
                                                                          Map<String, Map<String, RequirementAssignment>> fullFilledRequirementsDefinitionMap);

    List<Map<String, RequirementDefinition>> calculateExposedRequirements(List<Map<String, RequirementDefinition>> nodeTypeRequirementsDefinitionList,
                                                                                 Map<String, RequirementAssignment> nodeTemplateRequirementsAssignment);

    ToscaServiceModel loadToscaCsarPackage(InputStream toscaCsarPackage);
}
