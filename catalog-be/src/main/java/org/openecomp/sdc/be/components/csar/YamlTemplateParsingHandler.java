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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.components.csar;

import static java.util.stream.Collectors.toList;
import static org.openecomp.sdc.be.components.impl.ImportUtils.Constants.QUOTE;
import static org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import static org.openecomp.sdc.be.components.impl.ImportUtils.ToscaElementTypeEnum;
import static org.openecomp.sdc.be.components.impl.ImportUtils.findFirstToscaListElement;
import static org.openecomp.sdc.be.components.impl.ImportUtils.findFirstToscaMapElement;
import static org.openecomp.sdc.be.components.impl.ImportUtils.findToscaElement;
import static org.openecomp.sdc.be.components.impl.ImportUtils.loadYamlAsStrictMap;
import static org.openecomp.sdc.be.datatypes.enums.MetadataKeyEnum.NAME;
import static org.openecomp.sdc.be.model.tosca.ToscaType.STRING;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.ARTIFACTS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.ATTRIBUTES;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.CAPABILITIES;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.CAPABILITY;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DEFAULT_VALUE;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DESCRIPTION;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.FILE;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.GET_INPUT;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.GROUPS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.IMPLEMENTATION;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.INPUTS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.INTERFACES;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.IS_PASSWORD;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.MEMBERS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.NODE;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.NODE_TEMPLATES;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.NODE_TYPE;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.OPERATIONS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.OUTPUTS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.POLICIES;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.PROPERTIES;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.RELATIONSHIP;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.RELATIONSHIP_TEMPLATES;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.REQUIREMENTS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.SUBSTITUTION_FILTERS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.SUBSTITUTION_MAPPINGS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.TARGETS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.TOPOLOGY_TEMPLATE;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.TYPE;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.VALID_SOURCE_TYPES;

import com.att.aft.dme2.internal.gson.reflect.TypeToken;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import fj.data.Either;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.be.components.impl.AnnotationBusinessLogic;
import org.openecomp.sdc.be.components.impl.GroupTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.NodeFilterUploadCreator;
import org.openecomp.sdc.be.components.impl.PolicyTypeBusinessLogic;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.PolicyTargetType;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementSubstitutionFilterPropertyDataDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.OutputDefinition;
import org.openecomp.sdc.be.model.ParsedToscaYamlInfo;
import org.openecomp.sdc.be.model.PolicyDefinition;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.UploadArtifactInfo;
import org.openecomp.sdc.be.model.UploadAttributeInfo;
import org.openecomp.sdc.be.model.UploadCapInfo;
import org.openecomp.sdc.be.model.UploadComponentInstanceInfo;
import org.openecomp.sdc.be.model.UploadInterfaceInfo;
import org.openecomp.sdc.be.model.UploadPropInfo;
import org.openecomp.sdc.be.model.UploadReqInfo;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceDefinition;
import org.openecomp.sdc.be.tosca.utils.OperationArtifactUtil;
import org.openecomp.sdc.be.ui.model.OperationUi;
import org.openecomp.sdc.be.ui.model.PropertyAssignmentUi;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.yaml.snakeyaml.parser.ParserException;

/**
 * A handler class designed to parse the YAML file of the service template for a JAVA object
 */
@org.springframework.stereotype.Component
public class YamlTemplateParsingHandler {

    private static final int SUB_MAPPING_CAPABILITY_OWNER_NAME_IDX = 0;
    private static final int SUB_MAPPING_CAPABILITY_NAME_IDX = 1;
    private static final Logger log = Logger.getLogger(YamlTemplateParsingHandler.class);
    private static final String WITH_ATTRIBUTE = "with attribute '{}': '{}'";
    private final Gson gson = new Gson();
    private final JanusGraphDao janusGraphDao;
    private final GroupTypeBusinessLogic groupTypeBusinessLogic;
    private final AnnotationBusinessLogic annotationBusinessLogic;
    private final PolicyTypeBusinessLogic policyTypeBusinessLogic;
    private final ToscaFunctionYamlParsingHandler toscaFunctionYamlParsingHandler;

    public YamlTemplateParsingHandler(JanusGraphDao janusGraphDao,
                                      GroupTypeBusinessLogic groupTypeBusinessLogic,
                                      AnnotationBusinessLogic annotationBusinessLogic,
                                      PolicyTypeBusinessLogic policyTypeBusinessLogic,
                                      final ToscaFunctionYamlParsingHandler toscaFunctionYamlParsingHandler
    ) {
        this.janusGraphDao = janusGraphDao;
        this.groupTypeBusinessLogic = groupTypeBusinessLogic;
        this.annotationBusinessLogic = annotationBusinessLogic;
        this.policyTypeBusinessLogic = policyTypeBusinessLogic;
        this.toscaFunctionYamlParsingHandler = toscaFunctionYamlParsingHandler;
    }

    public ParsedToscaYamlInfo parseResourceInfoFromYAML(String fileName, String resourceYml, Map<String, String> createdNodesToscaResourceNames,
                                                         Map<String, NodeTypeInfo> nodeTypesInfo, String nodeName,
                                                         Component component, String interfaceTemplateYaml) {
        log.debug("#parseResourceInfoFromYAML - Going to parse yaml {} ", fileName);
        Map<String, Object> mappedToscaTemplate = getMappedToscaTemplate(fileName, resourceYml, nodeTypesInfo, nodeName);
        ParsedToscaYamlInfo parsedToscaYamlInfo = new ParsedToscaYamlInfo();
        Map<String, Object> mappedTopologyTemplate = (Map<String, Object>) findToscaElement(mappedToscaTemplate, TOPOLOGY_TEMPLATE,
            ToscaElementTypeEnum.ALL).left().on(err -> failIfNotTopologyTemplate(fileName));
        final Map<String, Object> mappedTopologyTemplateInputs = mappedTopologyTemplate.entrySet().stream()
            .filter(entry -> entry.getKey().equals(INPUTS.getElementName())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        final Map<String, Object> mappedTopologyTemplateOutputs = mappedTopologyTemplate.entrySet().stream()
            .filter(entry -> entry.getKey().equals(OUTPUTS.getElementName())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        parsedToscaYamlInfo.setInputs(getInputs(mappedTopologyTemplateInputs));
        parsedToscaYamlInfo.setOutputs(getOutputs(mappedTopologyTemplateOutputs));
        parsedToscaYamlInfo.setInstances(getInstances(
                mappedToscaTemplate,
                createdNodesToscaResourceNames
        ));
        associateRelationshipTemplatesToInstances(parsedToscaYamlInfo.getInstances(), mappedTopologyTemplate);
        parsedToscaYamlInfo.setGroups(getGroups(mappedToscaTemplate, component.getModel()));
        parsedToscaYamlInfo.setPolicies(getPolicies(mappedToscaTemplate, component.getModel()));
        Map<String, Object> substitutionMappings = getSubstitutionMappings(mappedToscaTemplate);
        if (substitutionMappings != null) {
            if (component.isService() && !interfaceTemplateYaml.isEmpty()) {
                parsedToscaYamlInfo.setProperties(getProperties(loadYamlAsStrictMap(interfaceTemplateYaml)));
                parsedToscaYamlInfo.setSubstitutionFilterProperties(getSubstitutionFilterProperties(mappedToscaTemplate));
            }
            parsedToscaYamlInfo.setSubstitutionMappingNodeType((String) substitutionMappings.get(NODE_TYPE.getElementName()));
        }
        log.debug("#parseResourceInfoFromYAML - The yaml {} has been parsed ", fileName);
        return parsedToscaYamlInfo;
    }

    private Map<String, Object> getMappedToscaTemplate(String fileName, String resourceYml, Map<String, NodeTypeInfo> nodeTypesInfo,
                                                       String nodeName) {
        Map<String, Object> mappedToscaTemplate;
        if (isNodeExist(nodeTypesInfo, nodeName)) {
            mappedToscaTemplate = nodeTypesInfo.get(nodeName).getMappedToscaTemplate();
        } else {
            mappedToscaTemplate = loadYaml(fileName, resourceYml);
        }
        return mappedToscaTemplate;
    }

    private Map<String, Object> loadYaml(String fileName, String resourceYml) {
        Map<String, Object> mappedToscaTemplate = null;
        try {
            mappedToscaTemplate = loadYamlAsStrictMap(resourceYml);
        } catch (ParserException e) {
            log.debug("#getMappedToscaTemplate - Failed to load YAML file {}", fileName, e);
            rollbackWithException(ActionStatus.TOSCA_PARSE_ERROR, fileName, e.getMessage());
        }
        return mappedToscaTemplate;
    }

    private boolean isNodeExist(Map<String, NodeTypeInfo> nodeTypesInfo, String nodeName) {
        return nodeTypesInfo != null && nodeName != null && nodeTypesInfo.containsKey(nodeName);
    }

    private Map<String, InputDefinition> getInputs(Map<String, Object> toscaJson) {
        Map<String, InputDefinition> inputs = ImportUtils.getInputs(toscaJson, annotationBusinessLogic.getAnnotationTypeOperations()).left()
            .on(err -> new HashMap<>());
        annotationBusinessLogic.validateAndMergeAnnotationsAndAssignToInput(inputs);
        return inputs;
    }

    private Map<String, OutputDefinition> getOutputs(Map<String, Object> toscaJson) {
        return ImportUtils.getOutputs(toscaJson).left().on(err -> new HashMap<>());
    }

    private Map<String, PropertyDefinition> getProperties(Map<String, Object> toscaJson) {
        return ImportUtils.getProperties(toscaJson).left().on(err -> new HashMap<>());
    }

    private ListDataDefinition<RequirementSubstitutionFilterPropertyDataDefinition> getSubstitutionFilterProperties(Map<String, Object> toscaJson) {
        ListDataDefinition<RequirementSubstitutionFilterPropertyDataDefinition> propertyList = new ListDataDefinition<>();
        Map<String, Object> substitutionFilters = findFirstToscaMapElement(toscaJson, SUBSTITUTION_FILTERS).left().on(err -> new HashMap<>());
        if (MapUtils.isEmpty(substitutionFilters)) {
            return propertyList;
        }
        ArrayList<Map<String, List<Map<String, Object>>>> substitutionFilterProperties =
            (ArrayList<Map<String, List<Map<String, Object>>>>) substitutionFilters.get("properties");
        if (CollectionUtils.isEmpty(substitutionFilterProperties)) {
            return propertyList;
        }
        for (Map<String, List<Map<String, Object>>> filterProps : substitutionFilterProperties) {
            for (Map.Entry<String, List<Map<String, Object>>> filterPropsMap : filterProps.entrySet()) {
                for (Map<String, Object> mapValue : filterPropsMap.getValue()) {
                    RequirementSubstitutionFilterPropertyDataDefinition requirementSubstitutionFilterPropertyDataDefinition =
                        new RequirementSubstitutionFilterPropertyDataDefinition();
                    requirementSubstitutionFilterPropertyDataDefinition.setName(filterPropsMap.getKey());
                    requirementSubstitutionFilterPropertyDataDefinition.setConstraints(
                        getSubstitutionFilterConstraints(filterPropsMap.getKey(), mapValue));
                    propertyList.add(requirementSubstitutionFilterPropertyDataDefinition);
                }
            }
        }
        return propertyList;
    }

    private List<String> getSubstitutionFilterConstraints(String name, Map<String, Object> value) {
        List<String> constraints = new ArrayList<>();
        for (Map.Entry<String, Object> valueMap : value.entrySet()) {
            constraints.add(name + ": {" + valueMap.getKey() + ": " + valueMap.getValue() + "}");
        }
        return constraints;
    }

    private Map<String, PolicyDefinition> getPolicies(Map<String, Object> toscaJson, String model) {
        Map<String, Object> mappedTopologyTemplate = (Map<String, Object>) findToscaElement(toscaJson, TOPOLOGY_TEMPLATE, ToscaElementTypeEnum.ALL)
            .left().on(err -> new HashMap<>());
        Map<String, Object> foundPolicies = (Map<String, Object>) mappedTopologyTemplate.get(POLICIES.getElementName());
        if (MapUtils.isNotEmpty(foundPolicies)) {
            return foundPolicies.entrySet().stream().map(policyToCreate -> createPolicy(policyToCreate, model))
                .collect(Collectors.toMap(PolicyDefinition::getName, p -> p));
        }
        return Collections.emptyMap();
    }

    private PolicyDefinition createPolicy(Map.Entry<String, Object> policyNameValue, String model) {
        PolicyDefinition emptyPolicyDef = new PolicyDefinition();
        String policyName = policyNameValue.getKey();
        emptyPolicyDef.setName(policyName);
        try {
            // There's no need to null test in conjunction with an instanceof test. null is not an instanceof anything, so a null check is redundant.
            if (policyNameValue.getValue() instanceof Map) {
                Map<String, Object> policyTemplateJsonMap = (Map<String, Object>) policyNameValue.getValue();
                validateAndFillPolicy(emptyPolicyDef, policyTemplateJsonMap, model);
            } else {
                rollbackWithException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);
            }
        } catch (ClassCastException e) {
            log.debug("#createPolicy - Failed to create the policy {}. The exception occurred", policyName, e);
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        return emptyPolicyDef;
    }

    private void validateAndFillPolicy(PolicyDefinition emptyPolicyDefinition, Map<String, Object> policyTemplateJsonMap, String model) {
        String policyTypeName = (String) policyTemplateJsonMap.get(TYPE.getElementName());
        if (StringUtils.isEmpty(policyTypeName)) {
            log.debug("#validateAndFillPolicy - The 'type' member is not found under policy {}", emptyPolicyDefinition.getName());
            rollbackWithException(ActionStatus.POLICY_MISSING_POLICY_TYPE, emptyPolicyDefinition.getName());
        }
        emptyPolicyDefinition.setType(policyTypeName);
        // set policy targets
        emptyPolicyDefinition.setTargets(validateFillPolicyTargets(policyTemplateJsonMap));
        PolicyTypeDefinition policyTypeDefinition = validateGetPolicyTypeDefinition(policyTypeName, model);
        // set policy properties
        emptyPolicyDefinition.setProperties(validateFillPolicyProperties(policyTypeDefinition, policyTemplateJsonMap));
    }

    private PolicyTypeDefinition validateGetPolicyTypeDefinition(String policyType, String modelName) {
        PolicyTypeDefinition policyTypeDefinition = policyTypeBusinessLogic.getLatestPolicyTypeByType(policyType, modelName);
        if (policyTypeDefinition == null) {
            log.debug("#validateAndFillPolicy - The policy type {} not found", policyType);
            rollbackWithException(ActionStatus.POLICY_TYPE_IS_INVALID, policyType);
        }
        return policyTypeDefinition;
    }

    private List<PropertyDataDefinition> validateFillPolicyProperties(final PolicyTypeDefinition policyTypeDefinition,
                                                                      final Map<String, Object> policyTemplateJsonMap) {
        if (policyTypeDefinition == null || CollectionUtils.isEmpty(policyTypeDefinition.getProperties())
            || MapUtils.isEmpty(policyTemplateJsonMap)) {
            return Collections.emptyList();
        }
        final Map<String, Object> propertiesJsonMap = (Map<String, Object>) policyTemplateJsonMap.get(PROPERTIES.getElementName());
        if (MapUtils.isEmpty(propertiesJsonMap)) {
            return Collections.emptyList();
        }
        return propertiesJsonMap.entrySet().stream()
            .map(propertyJson -> {
                final PropertyDefinition originalProperty =
                    policyTypeDefinition.getProperties().stream()
                        .filter(propertyDefinition -> propertyDefinition.getName().equals(propertyJson.getKey()))
                        .findFirst()
                        .orElse(null);
                if (originalProperty == null) {
                    return null;
                }
                final UploadPropInfo uploadPropInfo = buildProperty(propertyJson.getKey(), propertyJson.getValue());
                final PropertyDefinition propertyDefinition = new PropertyDefinition(originalProperty);
                propertyDefinition.setValue(gson.toJson(uploadPropInfo.getValue()));
                propertyDefinition.setToscaFunction(uploadPropInfo.getToscaFunction());
                propertyDefinition.setGetInputValues(uploadPropInfo.getGet_input());
                propertyDefinition.setDescription(uploadPropInfo.getDescription());
                return propertyDefinition;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Map<PolicyTargetType, List<String>> validateFillPolicyTargets(Map<String, Object> policyTemplateJson) {
        Map<PolicyTargetType, List<String>> targets = new EnumMap<>(PolicyTargetType.class);
        if (policyTemplateJson.containsKey(TARGETS.getElementName()) && policyTemplateJson.get(TARGETS.getElementName()) instanceof List) {
            List<String> targetsElement = (List<String>) policyTemplateJson.get(TARGETS.getElementName());
            targets.put(PolicyTargetType.COMPONENT_INSTANCES, targetsElement);
        }
        return targets;
    }

    private Map<String, UploadComponentInstanceInfo> getInstances(
            Map<String, Object> toscaJson,
            Map<String, String> createdNodesToscaResourceNames
    ) {
        Map<String, Object> nodeTemplates = findFirstToscaMapElement(toscaJson, NODE_TEMPLATES)
                .left().on(err -> new HashMap<>());
        if (nodeTemplates.isEmpty()) {
            return Collections.emptyMap();
        }
        return getInstances(
                toscaJson,
                createdNodesToscaResourceNames,
                nodeTemplates
        );
    }

    private Map<String, UploadComponentInstanceInfo> getInstances(
            Map<String, Object> toscaJson,
            Map<String, String> createdNodesToscaResourceNames,
            Map<String, Object> nodeTemplates
    ) {
        Map<String, Object> substitutionMappings = getSubstitutionMappings(toscaJson);
        return nodeTemplates.entrySet().stream()
            .map(node -> buildModuleComponentInstanceInfo(
                    node,
                    substitutionMappings,
                    createdNodesToscaResourceNames
            ))
            .collect(Collectors.toMap(UploadComponentInstanceInfo::getName, i -> i));
    }

    private Map<String, Object> getSubstitutionMappings(Map<String, Object> toscaJson) {
        Either<Map<String, Object>, ResultStatusEnum> eitherSubstitutionMappings = findFirstToscaMapElement(toscaJson, SUBSTITUTION_MAPPINGS);
        if (eitherSubstitutionMappings.isLeft()) {
            return eitherSubstitutionMappings.left().value();
        }
        return null;
    }

    private void associateRelationshipTemplatesToInstances(final Map<String, UploadComponentInstanceInfo> instances,
                                                           final Map<String, Object> toscaJson) {
        if (MapUtils.isEmpty(instances)) {
            return;
        }
        for (UploadComponentInstanceInfo instance : instances.values()) {
            final Map<String, List<OperationUi>> operations = new HashMap<>();
            final Map<String, List<UploadReqInfo>> requirements = instance.getRequirements();
            if (MapUtils.isNotEmpty(requirements)) {
                requirements.values()
                    .forEach(requirementInfoList -> requirementInfoList.stream()
                        .filter(requirement -> StringUtils.isNotEmpty(requirement.getRelationshipTemplate()))
                        .forEach(requirement -> operations.put(requirement.getRelationshipTemplate(),
                            getOperationsFromRelationshipTemplate(toscaJson, requirement.getRelationshipTemplate()))));
            }
            instance.setOperations(operations);
        }
    }

    private Map<String, Object> getRelationshipTemplates(final Map<String, Object> toscaJson, final String relationshipTemplate) {
        final Either<Map<String, Object>, ResultStatusEnum> eitherRelationshipTemplates = findFirstToscaMapElement(toscaJson, RELATIONSHIP_TEMPLATES);
        if (eitherRelationshipTemplates.isRight()) {
            throw new ByActionStatusComponentException(ActionStatus.RELATIONSHIP_TEMPLATE_NOT_FOUND);
        }
        final Map<String, Object> relationshipTemplateMap = eitherRelationshipTemplates.left().value();
        final Map<String, Map<String, Object>> relationship = (Map<String, Map<String, Object>>) relationshipTemplateMap.get(relationshipTemplate);
        if (relationship == null) {
            throw new ByActionStatusComponentException(ActionStatus.RELATIONSHIP_TEMPLATE_DEFINITION_NOT_FOUND);
        }
        return relationship.get(INTERFACES.getElementName());
    }

    private List<ToscaInterfaceDefinition> buildToscaInterfacesFromRelationship(final Map<String, Object> interfaces) {
        return interfaces.entrySet().stream()
            .map(entry -> {
                final var toscaInterfaceDefinition = new ToscaInterfaceDefinition();
                toscaInterfaceDefinition.setType(entry.getKey());
                final Map<String, Object> toscaInterfaceMap = (Map<String, Object>) entry.getValue();
                toscaInterfaceDefinition.setOperations((Map<String, Object>) toscaInterfaceMap.get(OPERATIONS.getElementName()));
                return toscaInterfaceDefinition;
            })
            .collect(toList());
    }

    private Optional<Object> getImplementation(final Map<String, Object> operationToscaMap) {
        if (MapUtils.isEmpty(operationToscaMap) || !operationToscaMap.containsKey(IMPLEMENTATION.getElementName())) {
            return Optional.empty();
        }
        return Optional.ofNullable(operationToscaMap.get(IMPLEMENTATION.getElementName()));
    }

    private List<PropertyAssignmentUi> getOperationsInputs(final Map<String, Object> operationToscaMap) {
        if (MapUtils.isEmpty(operationToscaMap) || !operationToscaMap.containsKey(INPUTS.getElementName())) {
            return Collections.emptyList();
        }
        final Map<String, Object> inputsMap = (Map<String, Object>) operationToscaMap.get(INPUTS.getElementName());
        return inputsMap.entrySet().stream().map(this::buildInputAssignment).collect(toList());
    }

    private PropertyAssignmentUi buildInputAssignment(final Entry<String, Object> inputAssignmentMap) {
        var propertyAssignmentUi = new PropertyAssignmentUi();
        propertyAssignmentUi.setName(inputAssignmentMap.getKey());
        propertyAssignmentUi.setValue(inputAssignmentMap.getValue().toString());
        propertyAssignmentUi.setType(STRING.getType());
        return propertyAssignmentUi;
    }

    private List<OperationUi> getOperationsFromRelationshipTemplate(final Map<String, Object> toscaJson, final String relationshipTemplate) {
        final List<OperationUi> operationUiList = new ArrayList<>();
        final List<ToscaInterfaceDefinition> interfaces =
            buildToscaInterfacesFromRelationship(getRelationshipTemplates(toscaJson, relationshipTemplate));
        interfaces.stream()
            .filter(interfaceDefinition -> MapUtils.isNotEmpty(interfaceDefinition.getOperations()))
            .forEach(interfaceDefinition ->
                interfaceDefinition.getOperations()
                    .forEach((operationType, operationValue) ->
                        operationUiList.add(buildOperation(interfaceDefinition.getType(), operationType, (Map<String, Object>) operationValue))
            ));
        return operationUiList;
    }

    private OperationUi buildOperation(final String interfaceType, final String operationType, final Map<String, Object> operationToscaMap) {
        var operationUi = new OperationUi();
        operationUi.setInterfaceType(interfaceType);
        operationUi.setOperationType(operationType);
        getImplementation(operationToscaMap).ifPresent(operationUi::setImplementation);
        final List<PropertyAssignmentUi> operationsInputs = getOperationsInputs(operationToscaMap);
        if (CollectionUtils.isNotEmpty(operationsInputs)) {
            operationUi.setInputs(operationsInputs);
        }
        return operationUi;
    }

    @SuppressWarnings("unchecked")
    private Map<String, GroupDefinition> getGroups(Map<String, Object> toscaJson, String model) {
        Map<String, Object> mappedTopologyTemplate = (Map<String, Object>) findToscaElement(toscaJson, TOPOLOGY_TEMPLATE, ToscaElementTypeEnum.ALL)
            .left().on(err -> new HashMap<>());
        Map<String, Object> foundGroups = (Map<String, Object>) mappedTopologyTemplate.get(GROUPS.getElementName());
        if (MapUtils.isNotEmpty(foundGroups)) {
            Map<String, GroupDefinition> groups = foundGroups.entrySet().stream().map(groupToCreate -> createGroup(groupToCreate, model))
                .collect(Collectors.toMap(GroupDefinition::getName, g -> g));
            Map<String, Object> substitutionMappings = getSubstitutionMappings(toscaJson);
            if (capabilitiesSubstitutionMappingsExist(substitutionMappings)) {
                groups.forEach((key, value) -> updateCapabilitiesNames(value,
                    getNamesToUpdate(key, (Map<String, List<String>>) substitutionMappings.get(CAPABILITIES.getElementName()))));
            }
            return groups;
        }
        return new HashMap<>();
    }

    private void updateCapabilitiesNames(GroupDefinition group, Map<String, String> capabilityNames) {
        if (MapUtils.isNotEmpty(group.getCapabilities())) {
            group.getCapabilities().values().stream().flatMap(Collection::stream).filter(cap -> capabilityNames.containsKey(cap.getName()))
                .forEach(cap -> cap.setName(capabilityNames.get(cap.getName())));
        }
    }

    private Map<String, String> getNamesToUpdate(String name, Map<String, List<String>> pair) {
        return pair.entrySet().stream().filter(e -> e.getValue().get(SUB_MAPPING_CAPABILITY_OWNER_NAME_IDX).equalsIgnoreCase(name))
            .collect(Collectors.toMap(e -> e.getValue().get(SUB_MAPPING_CAPABILITY_NAME_IDX), Map.Entry::getKey, (n1, n2) -> n1));
    }

    private boolean capabilitiesSubstitutionMappingsExist(Map<String, Object> substitutionMappings) {
        return substitutionMappings != null && substitutionMappings.containsKey(CAPABILITIES.getElementName());
    }

    private GroupDefinition createGroup(Map.Entry<String, Object> groupNameValue, String model) {
        GroupDefinition group = new GroupDefinition();
        group.setName(groupNameValue.getKey());
        try {
            if (groupNameValue.getValue() instanceof Map) {
                Map<String, Object> groupTemplateJsonMap = (Map<String, Object>) groupNameValue.getValue();
                validateAndFillGroup(group, groupTemplateJsonMap, model);
                validateUpdateGroupProperties(group, groupTemplateJsonMap);
                validateUpdateGroupCapabilities(group, groupTemplateJsonMap);
            } else {
                rollbackWithException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);
            }
        } catch (ClassCastException e) {
            log.debug("#createGroup - Failed to create the group {}. The exception occurres", groupNameValue.getKey(), e);
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        return group;
    }

    private Map<String, CapabilityDefinition> addCapabilities(Map<String, CapabilityDefinition> cap, Map<String, CapabilityDefinition> otherCap) {
        cap.putAll(otherCap);
        return cap;
    }

    private Map<String, CapabilityDefinition> addCapability(CapabilityDefinition cap) {
        Map<String, CapabilityDefinition> map = Maps.newHashMap();
        map.put(cap.getName(), cap);
        return map;
    }

    private void setMembers(GroupDefinition groupInfo, Map<String, Object> groupTemplateJsonMap) {
        if (groupTemplateJsonMap.containsKey(MEMBERS.getElementName())) {
            Object members = groupTemplateJsonMap.get(MEMBERS.getElementName());
            if (members != null) {
                if (members instanceof List) {
                    setMembersFromList(groupInfo, (List<?>) members);
                } else {
                    log.debug("The 'members' member is not of type list under group {}", groupInfo.getName());
                    rollbackWithException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);
                }
            }
        }
    }

    private void setMembersFromList(GroupDefinition groupInfo, List<?> membersAsList) {
        groupInfo.setMembers(membersAsList.stream().collect(Collectors.toMap(Object::toString, member -> "")));
    }

    @SuppressWarnings("unchecked")
    private void validateUpdateGroupProperties(GroupDefinition groupInfo, Map<String, Object> groupTemplateJsonMap) {
        if (groupTemplateJsonMap.containsKey(PROPERTIES.getElementName())) {
            Object propertiesElement = groupTemplateJsonMap.get(PROPERTIES.getElementName());
            if (propertiesElement instanceof Map) {
                mergeGroupProperties(groupInfo, (Map<String, Object>) propertiesElement);
            }
        }
    }

    private void mergeGroupProperties(final GroupDefinition groupDefinition, final Map<String, Object> parsedProperties) {
        if (CollectionUtils.isEmpty(groupDefinition.getProperties())) {
            return;
        }
        validateGroupProperties(parsedProperties, groupDefinition);
        groupDefinition.getProperties().stream()
            .filter(property -> parsedProperties.containsKey(property.getName()))
            .forEach(property -> mergeGroupProperty(property, parsedProperties.get(property.getName())));
    }

    private void mergeGroupProperty(final PropertyDataDefinition property, final Object propertyYaml) {
        final UploadPropInfo uploadPropInfo = buildProperty(property.getName(), propertyYaml);
        property.setToscaFunction(uploadPropInfo.getToscaFunction());
        property.setValue(convertPropertyValue(ToscaPropertyType.isValidType(property.getType()), uploadPropInfo.getValue()));
        property.setGetInputValues(uploadPropInfo.getGet_input());
    }

    private String convertPropertyValue(ToscaPropertyType type, Object value) {
        String convertedValue = null;
        if (value != null) {
            if (type == null || value instanceof Map || value instanceof List) {
                convertedValue = gson.toJson(value);
            } else {
                convertedValue = value.toString();
            }
        }
        return convertedValue;
    }

    private void setDescription(GroupDefinition groupInfo, Map<String, Object> groupTemplateJsonMap) {
        if (groupTemplateJsonMap.containsKey(DESCRIPTION.getElementName())) {
            groupInfo.setDescription((String) groupTemplateJsonMap.get(DESCRIPTION.getElementName()));
        }
    }

    private void validateAndFillGroup(GroupDefinition groupInfo, Map<String, Object> groupTemplateJsonMap, String model) {
        String type = (String) groupTemplateJsonMap.get(TYPE.getElementName());
        if (StringUtils.isEmpty(type)) {
            log.debug("#validateAndFillGroup - The 'type' member is not found under group {}", groupInfo.getName());
            rollbackWithException(ActionStatus.GROUP_MISSING_GROUP_TYPE, groupInfo.getName());
        }
        groupInfo.setType(type);
        GroupTypeDefinition groupType = groupTypeBusinessLogic.getLatestGroupTypeByType(type, model);
        if (groupType == null) {
            log.debug("#validateAndFillGroup - The group type {} not found", groupInfo.getName());
            rollbackWithException(ActionStatus.GROUP_TYPE_IS_INVALID, type);
        }
        groupInfo.convertFromGroupProperties(groupType.getProperties());
        groupInfo.convertCapabilityDefinitions(groupType.getCapabilities());
        setDescription(groupInfo, groupTemplateJsonMap);
        setMembers(groupInfo, groupTemplateJsonMap);
    }

    @SuppressWarnings("unchecked")
    private void validateUpdateGroupCapabilities(GroupDefinition groupInfo, Map<String, Object> groupTemplateJsonMap) {
        if (groupTemplateJsonMap.containsKey(CAPABILITIES.getElementName())) {
            Object capabilities = groupTemplateJsonMap.get(CAPABILITIES.getElementName());
            if (capabilities instanceof List) {
                validateUpdateCapabilities(groupInfo, ((List<Object>) capabilities).stream().map(o -> buildGroupCapability(groupInfo, o))
                    .collect(Collectors.toMap(CapabilityDefinition::getType, this::addCapability, this::addCapabilities)));
            } else if (capabilities instanceof Map) {
                validateUpdateCapabilities(groupInfo,
                    ((Map<String, Object>) capabilities).entrySet().stream().map(e -> buildGroupCapability(groupInfo, e))
                        .collect(Collectors.toMap(CapabilityDefinition::getType, this::addCapability, this::addCapabilities)));
            } else {
                log.debug("#setCapabilities - Failed to import the capabilities of the group {}. ", groupInfo.getName());
                rollbackWithException(ActionStatus.INVALID_YAML);
            }
        }
    }

    private void validateUpdateCapabilities(GroupDefinition groupInfo, Map<String, Map<String, CapabilityDefinition>> capabilityInfo) {
        validateGroupCapabilities(groupInfo, capabilityInfo);
        groupInfo.updateCapabilitiesProperties(capabilityInfo);
    }

    private void validateGroupCapabilities(GroupDefinition group, Map<String, Map<String, CapabilityDefinition>> parsedCapabilities) {
        if (MapUtils.isNotEmpty(parsedCapabilities)) {
            if (MapUtils.isEmpty(group.getCapabilities())) {
                failOnMissingCapabilityTypes(group, Lists.newArrayList(parsedCapabilities.keySet()));
            }
            List<String> missingCapTypes = parsedCapabilities.keySet().stream().filter(ct -> !group.getCapabilities().containsKey(ct))
                .collect(toList());
            if (CollectionUtils.isNotEmpty(missingCapTypes)) {
                failOnMissingCapabilityTypes(group, missingCapTypes);
            }
            group.getCapabilities().entrySet().forEach(e -> validateCapabilities(group, e.getValue(), parsedCapabilities.get(e.getKey())));
        }
    }

    private void validateCapabilities(GroupDefinition group, List<CapabilityDefinition> capabilities,
                                      Map<String, CapabilityDefinition> parsedCapabilities) {
        List<String> allowedCapNames = capabilities.stream().map(CapabilityDefinition::getName).distinct().collect(toList());
        List<String> missingCapNames = parsedCapabilities.keySet().stream().filter(c -> !allowedCapNames.contains(c)).collect(toList());
        if (CollectionUtils.isNotEmpty(missingCapNames)) {
            failOnMissingCapabilityNames(group, missingCapNames);
        }
        validateCapabilitiesProperties(capabilities, parsedCapabilities);
    }

    private void validateCapabilitiesProperties(List<CapabilityDefinition> capabilities, Map<String, CapabilityDefinition> parsedCapabilities) {
        capabilities.forEach(c -> validateCapabilityProperties(c, parsedCapabilities.get(c.getName())));
    }

    private void validateCapabilityProperties(CapabilityDefinition capability, CapabilityDefinition parsedCapability) {
        if (parsedCapability != null && parsedCapability.getProperties() != null) {
            List<String> parsedPropertiesNames = parsedCapability.getProperties().stream().map(ComponentInstanceProperty::getName).collect(toList());
            validateProperties(capability.getProperties().stream().map(PropertyDataDefinition::getName).collect(toList()), parsedPropertiesNames,
                ActionStatus.PROPERTY_NOT_FOUND, capability.getName(), capability.getType());
        }
    }

    private void validateGroupProperties(Map<String, Object> parsedProperties, GroupDefinition groupInfo) {
        List<String> parsedPropertiesNames = new ArrayList<>(parsedProperties.keySet());
        validateProperties(groupInfo.getProperties().stream().map(PropertyDataDefinition::getName).collect(toList()), parsedPropertiesNames,
            ActionStatus.GROUP_PROPERTY_NOT_FOUND, groupInfo.getName(), groupInfo.getType());
    }

    private void validateProperties(List<String> validProperties, List<String> parsedProperties, ActionStatus actionStatus, String name,
                                    String type) {
        if (CollectionUtils.isNotEmpty(parsedProperties)) {
            verifyMissingProperties(actionStatus, name, type, parsedProperties.stream().filter(n -> !validProperties.contains(n)).collect(toList()));
        }
    }

    private void verifyMissingProperties(ActionStatus actionStatus, String name, String type, List<String> missingProperties) {
        if (CollectionUtils.isNotEmpty(missingProperties)) {
            if (log.isDebugEnabled()) {
                log.debug("#validateProperties - Failed to validate properties. The properties {} are missing on {} of the type {}. ",
                    missingProperties.toString(), name, type);
            }
            rollbackWithException(actionStatus, missingProperties.toString(), missingProperties.toString(), name, type);
        }
    }

    @SuppressWarnings("unchecked")
    private CapabilityDefinition buildGroupCapability(GroupDefinition groupInfo, Object capObject) {
        if (!(capObject instanceof Map)) {
            log.debug("#convertToGroupCapability - Failed to import the capability {}. ", capObject);
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        return buildGroupCapability(groupInfo, ((Map<String, Object>) capObject).entrySet().iterator().next());
    }

    @SuppressWarnings("unchecked")
    private CapabilityDefinition buildGroupCapability(GroupDefinition groupInfo, Map.Entry<String, Object> capEntry) {
        CapabilityDefinition capability = new CapabilityDefinition();
        capability.setOwnerType(CapabilityDataDefinition.OwnerType.GROUP);
        capability.setName(capEntry.getKey());
        capability.setParentName(capEntry.getKey());
        capability.setOwnerId(groupInfo.getName());
        if (!(capEntry.getValue() instanceof Map)) {
            log.debug("#convertMapEntryToCapabilityDefinition - Failed to import the capability {}. ", capEntry.getKey());
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        Map<String, Object> capabilityValue = (Map<String, Object>) capEntry.getValue();
        String type = (String) capabilityValue.get(TYPE.getElementName());
        if (StringUtils.isEmpty(type)) {
            log.debug("#convertMapEntryToCapabilityDefinition - Failed to import the capability {}. Missing capability type. ", capEntry.getKey());
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        capability.setType(type);
        if (!(capabilityValue.get(PROPERTIES.getElementName()) instanceof Map)) {
            log.debug("#convertMapEntryToCapabilityDefinition - Failed to import the capability {}. ", capEntry.getKey());
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        Map<String, Object> properties = (Map<String, Object>) capabilityValue.get(PROPERTIES.getElementName());
        capability.setProperties(properties.entrySet().stream().map(this::convertToProperty).collect(toList()));
        return capability;
    }

    private ComponentInstanceProperty convertToProperty(Map.Entry<String, Object> e) {
        ComponentInstanceProperty property = new ComponentInstanceProperty();
        property.setName(e.getKey());
        property.setValue((String) e.getValue());
        return property;
    }

    @SuppressWarnings("unchecked")
    private UploadComponentInstanceInfo buildModuleComponentInstanceInfo(
            Map.Entry<String, Object> nodeTemplateJsonEntry,
            Map<String, Object> substitutionMappings,
            Map<String, String> createdNodesToscaResourceNames
    ) {
        UploadComponentInstanceInfo nodeTemplateInfo = new UploadComponentInstanceInfo();
        nodeTemplateInfo.setName(nodeTemplateJsonEntry.getKey());
        try {
            if (nodeTemplateJsonEntry.getValue() instanceof String) {
                String nodeTemplateJsonString = (String) nodeTemplateJsonEntry.getValue();
                nodeTemplateInfo.setType(nodeTemplateJsonString);
            } else if (nodeTemplateJsonEntry.getValue() instanceof Map) {
                Map<String, Object> nodeTemplateJsonMap = (Map<String, Object>) nodeTemplateJsonEntry.getValue();
                setToscaResourceType(createdNodesToscaResourceNames, nodeTemplateInfo, nodeTemplateJsonMap);
                setRequirements(nodeTemplateInfo, nodeTemplateJsonMap);
                setCapabilities(nodeTemplateInfo, nodeTemplateJsonMap);
                setArtifacts(nodeTemplateInfo, nodeTemplateJsonMap);
                updateProperties(nodeTemplateInfo, nodeTemplateJsonMap);
                updateAttributes(nodeTemplateInfo, nodeTemplateJsonMap);
                updateInterfaces(nodeTemplateInfo, nodeTemplateJsonMap);
                setDirectives(nodeTemplateInfo, nodeTemplateJsonMap);
                setNodeFilter(nodeTemplateInfo, nodeTemplateJsonMap);
                setSubstitutions(substitutionMappings, nodeTemplateInfo);
            } else {
                rollbackWithException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE);
            }
        } catch (ClassCastException e) {
            BeEcompErrorManager.getInstance().logBeSystemError("Import Resource - create capability");
            log.debug("error when creating capability, message:{}", e.getMessage(), e);
            rollbackWithException(ActionStatus.INVALID_YAML);
        }
        return nodeTemplateInfo;
    }

    @SuppressWarnings("unchecked")
    private void setSubstitutions(Map<String, Object> substitutionMappings, UploadComponentInstanceInfo nodeTemplateInfo) {
        if (substitutionMappings != null) {
            if (substitutionMappings.containsKey(CAPABILITIES.getElementName())) {
                nodeTemplateInfo.setCapabilitiesNamesToUpdate(getNamesToUpdate(nodeTemplateInfo.getName(),
                    (Map<String, List<String>>) substitutionMappings.get(CAPABILITIES.getElementName())));
            }
            if (substitutionMappings.containsKey(REQUIREMENTS.getElementName())) {
                nodeTemplateInfo.setRequirementsNamesToUpdate(getNamesToUpdate(nodeTemplateInfo.getName(),
                    (Map<String, List<String>>) substitutionMappings.get(REQUIREMENTS.getElementName())));
            }
        }
    }

    private void updateProperties(UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(PROPERTIES.getElementName())) {
            Map<String, List<UploadPropInfo>> properties =
                buildPropModuleFromYaml((Map<String, Object>) nodeTemplateJsonMap.get(PROPERTIES.getElementName()));
            if (!properties.isEmpty()) {
                nodeTemplateInfo.setProperties(properties);
            }
        }
    }

    private void updateAttributes(UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(ATTRIBUTES.getElementName())) {
            Map<String, UploadAttributeInfo> attributes = buildAttributeModuleFromYaml(nodeTemplateJsonMap);
            if (!attributes.isEmpty()) {
                nodeTemplateInfo.setAttributes(attributes);
            }
        }
    }

    private void updateInterfaces(
            UploadComponentInstanceInfo nodeTemplateInfo,
            Map<String, Object> nodeTemplateJsonMap
    ){
        if (nodeTemplateJsonMap.containsKey(INTERFACES.getElementName())) {
            Map<String, UploadInterfaceInfo> interfaces = buildInterfacesModuleFromYaml(
                    nodeTemplateJsonMap
            );
            if (!interfaces.isEmpty()) {
                nodeTemplateInfo.setInterfaces(interfaces);
            }
        }
    }

    private void setCapabilities(UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(CAPABILITIES.getElementName())) {
            Map<String, List<UploadCapInfo>> eitherCapRes = createCapModuleFromYaml(nodeTemplateJsonMap);
            if (!eitherCapRes.isEmpty()) {
                nodeTemplateInfo.setCapabilities(eitherCapRes);
            }
        }
    }

    private void setArtifacts(UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(ARTIFACTS.getElementName())) {
            Map<String, Map<String, UploadArtifactInfo>> eitherArtifactsRes = createArtifactsModuleFromYaml(nodeTemplateJsonMap);
            if (!eitherArtifactsRes.isEmpty()) {
                nodeTemplateInfo.setArtifacts(eitherArtifactsRes);
            }
        }
    }

    private void setRequirements(UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(REQUIREMENTS.getElementName())) {
            Map<String, List<UploadReqInfo>> regResponse = createReqModuleFromYaml(nodeTemplateJsonMap, nodeTemplateInfo.getName());
            if (!regResponse.isEmpty()) {
                nodeTemplateInfo.setRequirements(regResponse);
            }
        }
    }

    private void setToscaResourceType(Map<String, String> createdNodesToscaResourceNames, UploadComponentInstanceInfo nodeTemplateInfo,
                                      Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(TYPE.getElementName())) {
            String toscaResourceType = (String) nodeTemplateJsonMap.get(TYPE.getElementName());
            if (createdNodesToscaResourceNames.containsKey(toscaResourceType)) {
                toscaResourceType = createdNodesToscaResourceNames.get(toscaResourceType);
            }
            nodeTemplateInfo.setType(toscaResourceType);
        }
    }

    private void setDirectives(UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        List<String> directives = (List<String>) nodeTemplateJsonMap.get(TypeUtils.ToscaTagNamesEnum.DIRECTIVES.getElementName());
        nodeTemplateInfo.setDirectives(directives);
    }

    private void setNodeFilter(UploadComponentInstanceInfo nodeTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(TypeUtils.ToscaTagNamesEnum.NODE_FILTER.getElementName())) {
            nodeTemplateInfo.setUploadNodeFilterInfo(new NodeFilterUploadCreator()
                .createNodeFilterData(nodeTemplateJsonMap.get(TypeUtils.ToscaTagNamesEnum.NODE_FILTER.getElementName())));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<UploadReqInfo>> createReqModuleFromYaml(Map<String, Object> nodeTemplateJsonMap, String nodeName) {
        Map<String, List<UploadReqInfo>> moduleRequirements = new HashMap<>();
        Either<List<Object>, ResultStatusEnum> requirementsListRes = findFirstToscaListElement(nodeTemplateJsonMap, REQUIREMENTS);
        if (requirementsListRes.isLeft()) {
            for (Object jsonReqObj : requirementsListRes.left().value()) {
                String reqName = ((Map<String, Object>) jsonReqObj).keySet().iterator().next();
                Object reqJson = ((Map<String, Object>) jsonReqObj).get(reqName);
                addModuleNodeTemplateReq(moduleRequirements, reqJson, reqName, nodeName);
            }
        } else {
            Either<Map<String, Object>, ResultStatusEnum> requirementsMapRes = findFirstToscaMapElement(nodeTemplateJsonMap, REQUIREMENTS);
            if (requirementsMapRes.isLeft()) {
                for (Map.Entry<String, Object> entry : requirementsMapRes.left().value().entrySet()) {
                    String reqName = entry.getKey();
                    Object reqJson = entry.getValue();
                    addModuleNodeTemplateReq(moduleRequirements, reqJson, reqName, nodeName);
                }
            }
        }
        return moduleRequirements;
    }

    private void addModuleNodeTemplateReq(Map<String, List<UploadReqInfo>> moduleRequirements, Object requirementJson, String requirementName, String nodeName) {
        UploadReqInfo requirement = buildModuleNodeTemplateReg(requirementJson, nodeName);
        requirement.setName(requirementName);
        if (moduleRequirements.containsKey(requirementName)) {
            moduleRequirements.get(requirementName).add(requirement);
        } else {
            List<UploadReqInfo> list = new ArrayList<>();
            list.add(requirement);
            moduleRequirements.put(requirementName, list);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Map<String, UploadArtifactInfo>> createArtifactsModuleFromYaml(Map<String, Object> nodeTemplateJsonMap) {
        Map<String, Map<String, UploadArtifactInfo>> moduleArtifacts = new HashMap<>();
        Either<List<Object>, ResultStatusEnum> artifactsListRes = findFirstToscaListElement(nodeTemplateJsonMap, ARTIFACTS);
        if (artifactsListRes.isLeft()) {
            for (Object jsonArtifactObj : artifactsListRes.left().value()) {
                String key = ((Map<String, Object>) jsonArtifactObj).keySet().iterator().next();
                Object artifactJson = ((Map<String, Object>) jsonArtifactObj).get(key);
                addModuleNodeTemplateArtifacts(moduleArtifacts, artifactJson, key);
            }
        } else {
            Either<Map<String, Map<String, Object>>, ResultStatusEnum> artifactsMapRes = findFirstToscaMapElement(nodeTemplateJsonMap, ARTIFACTS);
            if (artifactsMapRes.isLeft()) {
                for (Map.Entry<String, Map<String, Object>> entry : artifactsMapRes.left().value().entrySet()) {
                    String artifactName = entry.getKey();
                    Object artifactJson = entry.getValue();
                    addModuleNodeTemplateArtifacts(moduleArtifacts, artifactJson, artifactName);
                }
            }
        }
        return moduleArtifacts;
    }

    private void addModuleNodeTemplateArtifacts(Map<String, Map<String, UploadArtifactInfo>> moduleArtifacts, Object artifactJson,
                                                String artifactName) {
        UploadArtifactInfo artifact = buildModuleNodeTemplateArtifact(artifactJson);
        artifact.setName(artifactName);
        if (moduleArtifacts.containsKey(ARTIFACTS.getElementName())) {
            moduleArtifacts.get(ARTIFACTS.getElementName()).put(artifactName, artifact);
        } else {
            Map<String, UploadArtifactInfo> map = new HashMap<>();
            map.put(artifactName, artifact);
            moduleArtifacts.put(ARTIFACTS.getElementName(), map);
        }
    }

    @SuppressWarnings("unchecked")
    private UploadArtifactInfo buildModuleNodeTemplateArtifact(Object artifactObject) {
        UploadArtifactInfo artifactTemplateInfo = new UploadArtifactInfo();
        if (artifactObject instanceof Map) {
            fillArtifact(artifactTemplateInfo, (Map<String, Object>) artifactObject);
        }
        return artifactTemplateInfo;
    }

    private void fillArtifact(UploadArtifactInfo artifactTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(TYPE.getElementName())) {
            artifactTemplateInfo.setType((String) nodeTemplateJsonMap.get(TYPE.getElementName()));
        }
        if (nodeTemplateJsonMap.containsKey(FILE.getElementName())) {
            artifactTemplateInfo.setFile((String) nodeTemplateJsonMap.get(FILE.getElementName()));
        }
        if (nodeTemplateJsonMap.containsKey(PROPERTIES.getElementName())) {
            Map<String, List<UploadPropInfo>> props =
                buildPropModuleFromYaml((Map<String, Object>) nodeTemplateJsonMap.get(PROPERTIES.getElementName()));
            if (!props.isEmpty()) {
                List<UploadPropInfo> properties = props.values().stream().flatMap(Collection::stream).collect(toList());
                artifactTemplateInfo.setProperties(properties);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<UploadCapInfo>> createCapModuleFromYaml(Map<String, Object> nodeTemplateJsonMap) {
        Map<String, List<UploadCapInfo>> moduleCap = new HashMap<>();
        Either<List<Object>, ResultStatusEnum> capabilitiesListRes = findFirstToscaListElement(nodeTemplateJsonMap, CAPABILITIES);
        if (capabilitiesListRes.isLeft()) {
            for (Object jsonCapObj : capabilitiesListRes.left().value()) {
                String key = ((Map<String, Object>) jsonCapObj).keySet().iterator().next();
                Object capJson = ((Map<String, Object>) jsonCapObj).get(key);
                addModuleNodeTemplateCap(moduleCap, capJson, key);
            }
        } else {
            Either<Map<String, Object>, ResultStatusEnum> capabilitiesMapRes = findFirstToscaMapElement(nodeTemplateJsonMap, CAPABILITIES);
            if (capabilitiesMapRes.isLeft()) {
                for (Map.Entry<String, Object> entry : capabilitiesMapRes.left().value().entrySet()) {
                    String capName = entry.getKey();
                    Object capJson = entry.getValue();
                    addModuleNodeTemplateCap(moduleCap, capJson, capName);
                }
            }
        }
        return moduleCap;
    }

    private void addModuleNodeTemplateCap(Map<String, List<UploadCapInfo>> moduleCap, Object capJson, String key) {
        UploadCapInfo capabilityDef = buildModuleNodeTemplateCap(capJson);
        capabilityDef.setKey(key);
        if (moduleCap.containsKey(key)) {
            moduleCap.get(key).add(capabilityDef);
        } else {
            List<UploadCapInfo> list = new ArrayList<>();
            list.add(capabilityDef);
            moduleCap.put(key, list);
        }
    }

    @SuppressWarnings("unchecked")
    private UploadCapInfo buildModuleNodeTemplateCap(Object capObject) {
        UploadCapInfo capTemplateInfo = new UploadCapInfo();
        if (capObject instanceof String) {
            String nodeTemplateJsonString = (String) capObject;
            capTemplateInfo.setNode(nodeTemplateJsonString);
        } else if (capObject instanceof Map) {
            fillCapability(capTemplateInfo, (Map<String, Object>) capObject);
        }
        return capTemplateInfo;
    }

    private void fillCapability(UploadCapInfo capTemplateInfo, Map<String, Object> nodeTemplateJsonMap) {
        if (nodeTemplateJsonMap.containsKey(NODE.getElementName())) {
            capTemplateInfo.setNode((String) nodeTemplateJsonMap.get(NODE.getElementName()));
        }
        if (nodeTemplateJsonMap.containsKey(TYPE.getElementName())) {
            capTemplateInfo.setType((String) nodeTemplateJsonMap.get(TYPE.getElementName()));
        }
        if (nodeTemplateJsonMap.containsKey(VALID_SOURCE_TYPES.getElementName())) {
            Either<List<Object>, ResultStatusEnum> validSourceTypesRes = findFirstToscaListElement(nodeTemplateJsonMap, VALID_SOURCE_TYPES);
            if (validSourceTypesRes.isLeft()) {
                capTemplateInfo.setValidSourceTypes(validSourceTypesRes.left().value().stream().map(Object::toString).collect(toList()));
            }
        }
        if (nodeTemplateJsonMap.containsKey(PROPERTIES.getElementName())) {
            Map<String, List<UploadPropInfo>> props =
                buildPropModuleFromYaml((Map<String, Object>) nodeTemplateJsonMap.get(PROPERTIES.getElementName()));
            if (!props.isEmpty()) {
                List<UploadPropInfo> properties = props.values().stream().flatMap(Collection::stream).collect(toList());
                capTemplateInfo.setProperties(properties);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private UploadReqInfo buildModuleNodeTemplateReg(Object regObject, String nodeName) {
        UploadReqInfo regTemplateInfo = new UploadReqInfo();
        if (regObject instanceof String) {
            String nodeTemplateJsonString = (String) regObject;
            regTemplateInfo.setNode(nodeTemplateJsonString);
        } else if (regObject instanceof Map) {
            Map<String, Object> nodeTemplateJsonMap = (Map<String, Object>) regObject;
            if (nodeTemplateJsonMap.containsKey(NODE.getElementName())) {
                regTemplateInfo.setNode((String) nodeTemplateJsonMap.get(NODE.getElementName()));
            }
            if (nodeTemplateJsonMap.containsKey(CAPABILITY.getElementName())) {
                regTemplateInfo.setCapabilityName((String) nodeTemplateJsonMap.get(CAPABILITY.getElementName()));
            }
            if (nodeTemplateJsonMap.containsKey(RELATIONSHIP.getElementName())) {
                final String template = (String) nodeTemplateJsonMap.get(RELATIONSHIP.getElementName());
                if (StringUtils.isNotEmpty(nodeName) && template.contains(nodeName)) {
                regTemplateInfo.setRelationshipTemplate(template);
                }
            }
        }
        return regTemplateInfo;
    }

    private Map<String, UploadAttributeInfo> buildAttributeModuleFromYaml(
            Map<String, Object> nodeTemplateJsonMap) {
        Map<String, UploadAttributeInfo> moduleAttribute = new HashMap<>();
        Either<Map<String, Object>, ResultStatusEnum> toscaAttributes = findFirstToscaMapElement(nodeTemplateJsonMap, ATTRIBUTES);
        if (toscaAttributes.isLeft()) {
            Map<String, Object> jsonAttributes = toscaAttributes.left().value();
            for (Map.Entry<String, Object> jsonAttributeObj : jsonAttributes.entrySet()) {
                UploadAttributeInfo attributeDef = buildAttribute(jsonAttributeObj.getKey(), jsonAttributeObj.getValue());
                moduleAttribute.put(attributeDef.getName(), attributeDef);
            }
        }
        return moduleAttribute;
    }

    private UploadAttributeInfo buildAttribute(String attributeName, Object attributeValue) {
        UploadAttributeInfo attributeDef = new UploadAttributeInfo();
        attributeDef.setValue(attributeValue);
        attributeDef.setName(attributeName);
        return attributeDef;
    }

    private Map<String, List<UploadPropInfo>> buildPropModuleFromYaml(final Map<String, Object> propertyMap) {
        final Map<String, List<UploadPropInfo>> moduleProp = new HashMap<>();
        propertyMap.entrySet().forEach(propertyMapEntry -> addProperty(moduleProp, propertyMapEntry));
        return moduleProp;
    }

    private Map<String, UploadInterfaceInfo> buildInterfacesModuleFromYaml(
            Map<String, Object> nodeTemplateJsonMap
    ) {
        Map<String, UploadInterfaceInfo> moduleInterfaces = new HashMap<>();
        Either<Map<String, Object>, ResultStatusEnum> toscaInterfaces = findFirstToscaMapElement(nodeTemplateJsonMap, INTERFACES);
        if (toscaInterfaces.isLeft()) {
            Map<String, Object> jsonInterfaces = toscaInterfaces.left().value();
            for (Map.Entry<String, Object> jsonInterfacesObj : jsonInterfaces.entrySet()) {
                addInterfaces(moduleInterfaces, jsonInterfacesObj);
            }
        }
        return moduleInterfaces;
    }

    private void addProperty(Map<String, List<UploadPropInfo>> moduleProp, Map.Entry<String, Object> jsonPropObj) {
        UploadPropInfo propertyDef = buildProperty(jsonPropObj.getKey(), jsonPropObj.getValue());
        if (moduleProp.containsKey(propertyDef.getName())) {
            moduleProp.get(propertyDef.getName()).add(propertyDef);
        } else {
            List<UploadPropInfo> list = new ArrayList<>();
            list.add(propertyDef);
            moduleProp.put(propertyDef.getName(), list);
        }
    }

    private void addInterfaces(Map<String, UploadInterfaceInfo> moduleInterface, Map.Entry<String, Object> jsonPropObj) {
        UploadInterfaceInfo interfaceInfo = buildInterface(jsonPropObj.getKey(), jsonPropObj.getValue());
        moduleInterface.put(jsonPropObj.getKey(), interfaceInfo);
    }

    @SuppressWarnings("unchecked")
    private UploadPropInfo buildProperty(String propName, Object propValueObj) {
        final var propertyDef = new UploadPropInfo();
        propertyDef.setValue(propValueObj);
        propertyDef.setName(propName);
        if (propValueObj instanceof Map) {
            final Map<String, Object> propValueMap = (Map<String, Object>) propValueObj;
            if (propValueMap.containsKey(TYPE.getElementName())) {
                propertyDef.setType(propValueMap.get(TYPE.getElementName()).toString());
            }
            if (containsGetInput(propValueObj)) {
                fillInputRecursively(propName, propValueMap, propertyDef);
            }
            if (toscaFunctionYamlParsingHandler.isPropertyValueToscaFunction(propValueObj)) {
                toscaFunctionYamlParsingHandler.buildToscaFunctionBasedOnPropertyValue(propValueMap).ifPresent(propertyDef::setToscaFunction);
            }
            if (propValueMap.containsKey(DESCRIPTION.getElementName())) {
                propertyDef.setDescription((propValueMap).get(DESCRIPTION.getElementName()).toString());
            }
            if (propValueMap.containsKey(DEFAULT_VALUE.getElementName())) {
                propertyDef.setValue(propValueMap.get(DEFAULT_VALUE.getElementName()));
            }
            if (propValueMap.containsKey(IS_PASSWORD.getElementName())) {
                propertyDef.setPassword(Boolean.getBoolean(propValueMap.get(IS_PASSWORD.getElementName()).toString()));
            } else {
                propertyDef.setValue(propValueObj);
            }
        } else if (propValueObj instanceof List) {
            fillInputsListRecursively(propertyDef, (List<Object>) propValueObj);
            propertyDef.setValue(propValueObj);
        }
        return propertyDef;
    }

    private UploadInterfaceInfo buildInterface(String interfaceName, Object interfaceValue) {
        UploadInterfaceInfo interfaceDef = new UploadInterfaceInfo();
        interfaceDef.setValue(interfaceValue);
        interfaceDef.setName(interfaceName);
        interfaceDef.setKey(interfaceName);
        Map<String, OperationDataDefinition> operations = new HashMap<>();
        if (interfaceValue instanceof Map) {
            Map<String, Object> operationsMap = (Map<String, Object>) interfaceValue;
            for (Map.Entry<String, Object> operationEntry : operationsMap.entrySet()) {
                OperationDataDefinition operationDef = new OperationDataDefinition();
                operationDef.setName(operationEntry.getKey());
                Map<String, Object> operationValue = (Map<String, Object>) operationEntry.getValue();
                if (operationValue.containsKey(DESCRIPTION.getElementName())) {
                    operationDef.setDescription(operationValue.get(DESCRIPTION.getElementName()).toString());
                }
                operationDef.setImplementation(handleOperationImplementation(operationValue).orElse(new ArtifactDataDefinition()));
                if (operationValue.containsKey(INPUTS.getElementName())) {
                    final Map<String, Object> interfaceInputs = (Map<String, Object>) operationValue.get(INPUTS.getElementName());
                    operationDef.setInputs(handleInterfaceOperationInputs(interfaceInputs));
                }
                operations.put(operationEntry.getKey(), operationDef);
            }
            interfaceDef.setOperations(operations);
            if (operationsMap.containsKey(TYPE.getElementName())) {
                interfaceDef.setType(((Map<String, Object>) interfaceValue).get(TYPE.getElementName()).toString());
            }
        }
        return interfaceDef;
    }

    private ListDataDefinition<OperationInputDefinition> handleInterfaceOperationInputs(final Map<String, Object> interfaceInputs) {
        final ListDataDefinition<OperationInputDefinition> inputs = new ListDataDefinition<>();
        for (final Entry<String, Object> interfaceInput : interfaceInputs.entrySet()) {
            final OperationInputDefinition operationInput = new OperationInputDefinition();
            operationInput.setUniqueId(UUID.randomUUID().toString());
            operationInput.setInputId(operationInput.getUniqueId());
            operationInput.setName(interfaceInput.getKey());
            handleInputToscaDefinition(interfaceInput.getKey(), interfaceInput.getValue(), operationInput);
            inputs.add(operationInput);
        }
        return inputs;
    }

    private void handleInputToscaDefinition(
        final String inputName,
        final Object value,
        final OperationInputDefinition operationInput
    ) {
        if (value instanceof Map) {
            log.debug("Creating interface operation input '{}'", inputName);
            Gson gson = new Gson();
            Type type = new TypeToken<LinkedHashMap<String, Object>>(){}.getType();
            String stringValue = gson.toJson(value, type);
            operationInput.setValue(stringValue);
        }
        if (value instanceof String) {
            final String stringValue = (String) value;
            operationInput.setDefaultValue(stringValue);
            operationInput.setToscaDefaultValue(stringValue);
            operationInput.setValue(stringValue);
        }
    }

    private Optional<ArtifactDataDefinition> handleOperationImplementation(
        final Map<String, Object> operationDefinitionMap
    ) {
        if (!operationDefinitionMap.containsKey(IMPLEMENTATION.getElementName())) {
            return Optional.empty();
        }
        final ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();
        if (operationDefinitionMap.get(IMPLEMENTATION.getElementName()) instanceof Map &&
                ((Map)operationDefinitionMap.get(IMPLEMENTATION.getElementName())).containsKey("primary")) {
            Map<String, Object> implDetails = (Map) ((Map)operationDefinitionMap.get(IMPLEMENTATION.getElementName())).get("primary");

            if (implDetails.get("file") != null) {
                final String file = implDetails.get("file").toString();
                artifactDataDefinition.setArtifactName(generateArtifactName(file));
            }
            if (implDetails.get("type") != null) {
                artifactDataDefinition.setArtifactType(implDetails.get("type").toString());
            }
            if (implDetails.get("artifact_version") != null) {
                artifactDataDefinition.setArtifactVersion(implDetails.get("artifact_version").toString());
            }

            if(implDetails.get("properties") instanceof Map) {
                List<PropertyDataDefinition> operationProperties = artifactDataDefinition.getProperties() == null ? new ArrayList<>() : artifactDataDefinition.getProperties();
                Map<String, Object> properties = (Map<String, Object>) implDetails.get("properties");
                properties.forEach((k,v) -> {
                    ToscaPropertyType type = getTypeFromObject(v);
                    if (type != null) {
                        PropertyDataDefinition propertyDef = new PropertyDataDefinition();
                        propertyDef.setName(k);
                        propertyDef.setValue(v.toString());
                        artifactDataDefinition.addProperty(propertyDef);
                    }
                });
            }
        }
        if (operationDefinitionMap.get(IMPLEMENTATION.getElementName()) instanceof String) {
            final String implementation = (String) operationDefinitionMap.get(IMPLEMENTATION.getElementName());
            artifactDataDefinition.setArtifactName(generateArtifactName(implementation));
        }
        return Optional.of(artifactDataDefinition);
    }

    private String generateArtifactName(final String name) {
        if (OperationArtifactUtil.artifactNameIsALiteralValue(name)) {
            return name;
        } else {
            return QUOTE + name + QUOTE;
        }
    }

    private ToscaPropertyType getTypeFromObject(final Object value) {
        if (value instanceof String) {
            return ToscaPropertyType.STRING;
        }
        if (value instanceof Integer) {
            return ToscaPropertyType.INTEGER;
        }
        if (value instanceof Boolean) {
            return ToscaPropertyType.BOOLEAN;
        }
        if (value instanceof Float || value instanceof Double) {
            return ToscaPropertyType.FLOAT;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private boolean containsGetInput(Object propValue) {
        return ((Map<String, Object>) propValue).containsKey(GET_INPUT.getElementName()) || ImportUtils.containsGetInput(propValue);
    }

    @SuppressWarnings("unchecked")
    private void fillInputsListRecursively(UploadPropInfo propertyDef, List<Object> propValueList) {
        for (Object objValue : propValueList) {
            if (objValue instanceof Map) {
                Map<String, Object> objMap = (Map<String, Object>) objValue;
                if (objMap.containsKey(GET_INPUT.getElementName())) {
                    fillInputRecursively(propertyDef.getName(), objMap, propertyDef);
                } else {
                    Set<String> keys = objMap.keySet();
                    findAndFillInputsListRecursively(propertyDef, objMap, keys);
                }
            } else if (objValue instanceof List) {
                List<Object> propSubValueList = (List<Object>) objValue;
                fillInputsListRecursively(propertyDef, propSubValueList);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void findAndFillInputsListRecursively(UploadPropInfo propertyDef, Map<String, Object> objMap, Set<String> keys) {
        for (String key : keys) {
            Object value = objMap.get(key);
            if (value instanceof Map) {
                fillInputRecursively(key, (Map<String, Object>) value, propertyDef);
            } else if (value instanceof List) {
                List<Object> propSubValueList = (List<Object>) value;
                fillInputsListRecursively(propertyDef, propSubValueList);
            }
        }
    }

    private void fillInputRecursively(String propName, Map<String, Object> propValue, UploadPropInfo propertyDef) {
        if (propValue.containsKey(GET_INPUT.getElementName())) {
            Object getInput = propValue.get(GET_INPUT.getElementName());
            GetInputValueDataDefinition getInputInfo = new GetInputValueDataDefinition();
            List<GetInputValueDataDefinition> getInputs = propertyDef.getGet_input();
            if (getInputs == null) {
                getInputs = new ArrayList<>();
            }
            if (getInput instanceof String) {
                getInputInfo.setInputName((String) getInput);
                getInputInfo.setPropName(propName);
            } else if (getInput instanceof List) {
                fillInput(propName, getInput, getInputInfo);
            }
            getInputs.add(getInputInfo);
            propertyDef.setGet_input(getInputs);
            propertyDef.setValue(propValue);
        } else {
            findAndFillInputRecursively(propValue, propertyDef);
        }
    }

    @SuppressWarnings("unchecked")
    private void findAndFillInputRecursively(Map<String, Object> propValue, UploadPropInfo propertyDef) {
        for (Map.Entry<String, Object> entry : propValue.entrySet()) {
            String propName = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                fillInputRecursively(propName, (Map<String, Object>) value, propertyDef);
            } else if (value instanceof List) {
                fillInputsRecursively(propertyDef, propName, (List<Object>) value);
            }
        }
    }

    private void fillInputsRecursively(UploadPropInfo propertyDef, String propName, List<Object> inputs) {
        inputs.stream().filter(Map.class::isInstance).forEach(o -> fillInputRecursively(propName, (Map<String, Object>) o, propertyDef));
    }

    @SuppressWarnings("unchecked")
    private void fillInput(String propName, Object getInput, GetInputValueDataDefinition getInputInfo) {
        List<Object> getInputList = (List<Object>) getInput;
        getInputInfo.setPropName(propName);
        getInputInfo.setInputName((String) getInputList.get(0));
        if (getInputList.size() > 1) {
            Object indexObj = getInputList.get(1);
            if (indexObj instanceof Integer) {
                getInputInfo.setIndexValue((Integer) indexObj);
            } else if (indexObj instanceof Float) {
                int index = ((Float) indexObj).intValue();
                getInputInfo.setIndexValue(index);
            } else if (indexObj instanceof Map && ((Map<String, Object>) indexObj).containsKey(GET_INPUT.getElementName())) {
                Object index = ((Map<String, Object>) indexObj).get(GET_INPUT.getElementName());
                GetInputValueDataDefinition getInputInfoIndex = new GetInputValueDataDefinition();
                getInputInfoIndex.setInputName((String) index);
                getInputInfoIndex.setPropName(propName);
                getInputInfo.setGetInputIndex(getInputInfoIndex);
            }
            getInputInfo.setList(true);
        }
    }

    private Object failIfNotTopologyTemplate(String fileName) {
        janusGraphDao.rollback();
        throw new ByActionStatusComponentException(ActionStatus.NOT_TOPOLOGY_TOSCA_TEMPLATE, fileName);
    }

    private void rollbackWithException(ActionStatus actionStatus, String... params) {
        janusGraphDao.rollback();
        throw new ByActionStatusComponentException(actionStatus, params);
    }

    private void failOnMissingCapabilityTypes(GroupDefinition groupDefinition, List<String> missingCapTypes) {
        if (log.isDebugEnabled()) {
            log.debug(
                "#failOnMissingCapabilityTypes - Failed to validate the capabilities of the group {}. The capability types {} are missing on the group type {}. ",
                groupDefinition.getName(), missingCapTypes.toString(), groupDefinition.getType());
        }
        if (CollectionUtils.isNotEmpty(missingCapTypes)) {
            rollbackWithException(ActionStatus.MISSING_CAPABILITY_TYPE, missingCapTypes.toString());
        }
    }

    private void failOnMissingCapabilityNames(GroupDefinition groupDefinition, List<String> missingCapNames) {
        if (log.isDebugEnabled()) {
            log.debug(
                "#failOnMissingCapabilityNames - Failed to validate the capabilities of the group {}. The capabilities with the names {} are missing on the group type {}. ",
                groupDefinition.getName(), missingCapNames.toString(), groupDefinition.getType());
        }
        rollbackWithException(ActionStatus.MISSING_CAPABILITIES, missingCapNames.toString(), CapabilityDataDefinition.OwnerType.GROUP.getValue(),
            groupDefinition.getName());
    }
}
