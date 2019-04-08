/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019 Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.core.impl;

import org.apache.commons.collections.MapUtils;
import org.onap.sdc.tosca.datatypes.model.ArtifactDefinition;
import org.onap.sdc.tosca.datatypes.model.CapabilityAssignment;
import org.onap.sdc.tosca.datatypes.model.NodeFilter;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ParameterDefinition;
import org.onap.sdc.tosca.datatypes.model.RequirementAssignment;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.SubstitutionMapping;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.converter.datatypes.Constants;
import org.openecomp.core.converter.errors.SubstitutionMappingsConverterErrorBuilder;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.tosca.services.DataModelUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.openecomp.core.converter.datatypes.Constants.capabilities;
import static org.openecomp.core.converter.datatypes.Constants.inputs;
import static org.openecomp.core.converter.datatypes.Constants.nodeType;
import static org.openecomp.core.converter.datatypes.Constants.outputs;
import static org.openecomp.core.converter.datatypes.Constants.requirements;

public class VnfTopologyTemplateConverter {

    public void convertTopologyTemplate(ServiceTemplate serviceTemplate,
                                         ServiceTemplateReaderService readerService) {

        convertInputs(serviceTemplate, readerService);
        convertNodeTemplates(serviceTemplate, readerService);
        convertOutputs(serviceTemplate, readerService);
        convertSubstitutionMappings(serviceTemplate, readerService);
    }

    private void convertInputs(ServiceTemplate serviceTemplate,
                               ServiceTemplateReaderService readerService) {
        Map<String, Object> inputs = readerService.getInputs();
        addInputsOrOutputsToServiceTemplate(serviceTemplate, inputs, Constants.inputs);
    }

    private void convertOutputs(ServiceTemplate serviceTemplate,
                                ServiceTemplateReaderService readerService) {
        Map<String, Object> outputs = readerService.getOutputs();
        addInputsOrOutputsToServiceTemplate(serviceTemplate, outputs, Constants.outputs);
    }

    private void addInputsOrOutputsToServiceTemplate(ServiceTemplate serviceTemplate,
                                                     Map<String, Object> mapToConvert,
                                                     String inputsOrOutputs) {
        if (MapUtils.isEmpty(mapToConvert)) {
            return;
        }

        for (Map.Entry<String, Object> entry : mapToConvert.entrySet()) {
            Optional<ParameterDefinition> parameterDefinition =
                    ToscaConverterUtil.createObjectFromClass(
                            entry.getKey(), entry.getValue(), ParameterDefinition.class);

            parameterDefinition.ifPresent(parameterDefinitionValue -> {
                Optional<Object> defaultValue =
                        ToscaConverterUtil.getDefaultValue(entry.getValue(), parameterDefinition.get());
                defaultValue.ifPresent(parameterDefinitionValue::set_default);
                addToServiceTemplateAccordingToSection(
                        serviceTemplate, inputsOrOutputs, entry.getKey(), parameterDefinition.get());
            });
        }
    }

    private void addToServiceTemplateAccordingToSection(ServiceTemplate serviceTemplate,
                                                        String inputsOrOutputs,
                                                        String parameterId,
                                                        ParameterDefinition parameterDefinition) {
        if (inputsOrOutputs.equals(inputs)) {
            DataModelUtil
                    .addInputParameterToTopologyTemplate(serviceTemplate, parameterId, parameterDefinition);
        } else if (inputsOrOutputs.equals(outputs)) {
            DataModelUtil
                    .addOutputParameterToTopologyTemplate(serviceTemplate, parameterId, parameterDefinition);
        }
    }

    private void convertNodeTemplates(ServiceTemplate serviceTemplate,
                                      ServiceTemplateReaderService readerService) {
        Map<String, Object> nodeTemplates = readerService.getNodeTemplates();
        if (MapUtils.isEmpty(nodeTemplates)) {
            return;
        }

        for (Map.Entry<String, Object> nodeTemplateEntry : nodeTemplates.entrySet()) {
            NodeTemplate nodeTemplate = convertNodeTemplate(nodeTemplateEntry.getValue());
            DataModelUtil.addNodeTemplate(serviceTemplate, nodeTemplateEntry.getKey(), nodeTemplate);
        }
    }

    private NodeTemplate convertNodeTemplate(Object candidateNodeTemplate) {
        NodeTemplate nodeTemplate = new NodeTemplate();

        Map<String, Object> nodeTemplateAsMap = (Map<String, Object>) candidateNodeTemplate;
        nodeTemplate.setArtifacts((Map<String, ArtifactDefinition>) nodeTemplateAsMap.get("artifacts"));
        nodeTemplate.setAttributes((Map<String, Object>) nodeTemplateAsMap.get("attributes"));
        nodeTemplate.setCopy((String) nodeTemplateAsMap.get("copy"));
        nodeTemplate.setDescription((String) nodeTemplateAsMap.get("description"));
        nodeTemplate.setDirectives((List<String>) nodeTemplateAsMap.get("directives"));
        nodeTemplate.setInterfaces(
                (Map<String, Object>) nodeTemplateAsMap.get("interfaces"));
        nodeTemplate.setNode_filter((NodeFilter) nodeTemplateAsMap.get("node_filter"));
        nodeTemplate.setProperties((Map<String, Object>) nodeTemplateAsMap.get("properties"));
        nodeTemplate.setRequirements(
                (List<Map<String, RequirementAssignment>>) nodeTemplateAsMap.get("requirements"));
        nodeTemplate.setType((String) nodeTemplateAsMap.get("type"));
        nodeTemplate.setCapabilities(
                convertCapabilities((Map<String, Object>) nodeTemplateAsMap.get("capabilities")));

        return nodeTemplate;
    }

    private Map<String, CapabilityAssignment> convertCapabilities(Map<String, Object> capabilities) {
        if (MapUtils.isEmpty(capabilities)) {
            return null;
        }

        Map<String, CapabilityAssignment> convertedCapabilities = new HashMap<>();
        for (Map.Entry<String, Object> capabilityAssignmentEntry : capabilities.entrySet()) {
            Optional<CapabilityAssignment> capabilityAssignment = ToscaConverterUtil.createObjectFromClass
                    (capabilityAssignmentEntry.getKey(), capabilityAssignmentEntry.getValue(),
                            CapabilityAssignment.class);

            capabilityAssignment.ifPresent(capabilityAssignmentValue ->
                    convertedCapabilities.put(capabilityAssignmentEntry.getKey(), capabilityAssignmentValue));

        }
        return convertedCapabilities;
    }

    private void convertSubstitutionMappings(ServiceTemplate serviceTemplate,
                                             ServiceTemplateReaderService readerService) {
        Map<String, Object> substitutionMappings = readerService.getSubstitutionMappings();
        if (MapUtils.isEmpty(substitutionMappings)) {
            return;
        }
        SubstitutionMapping substitutionMapping = convertSubstitutionMappings(substitutionMappings);
        DataModelUtil.addSubstitutionMapping(serviceTemplate, substitutionMapping);
    }

    private SubstitutionMapping convertSubstitutionMappings(
            Map<String, Object> substitutionMappings) {
        SubstitutionMapping substitutionMapping = new SubstitutionMapping();

        substitutionMapping.setNode_type((String) substitutionMappings.get(nodeType));
        substitutionMapping.setCapabilities(
                convertSubstitutionMappingsSections(capabilities, substitutionMappings.get(capabilities)));
        substitutionMapping.setRequirements(
                convertSubstitutionMappingsSections(requirements, substitutionMappings.get(requirements)));

        return substitutionMapping;
    }

    private Map<String, List<String>> convertSubstitutionMappingsSections(String sectionName,
                                                                          Object sectionToConvert) {

        if (Objects.isNull(sectionToConvert)) {
            return null;
        }

        if (!(sectionToConvert instanceof Map)) {
            throw new CoreException(
                    new SubstitutionMappingsConverterErrorBuilder(
                            sectionName, sectionToConvert.getClass().getSimpleName()).build());
        }

        return convertSection(sectionToConvert);
    }

    private Map<String, List<String>> convertSection(Object sectionToConvert) {

        Map<String, Object> sectionAsMap = (Map<String, Object>) sectionToConvert;
        Map<String, List<String>> convertedSection = new HashMap<>();

        if (MapUtils.isEmpty(sectionAsMap)) {
            return null;
        }

        for (Map.Entry<String, Object> entry : sectionAsMap.entrySet()) {
            if (entry.getValue() instanceof List) {
                convertedSection.put(entry.getKey(), (List<String>) entry.getValue());
            }
        }

        return convertedSection;
    }

}
