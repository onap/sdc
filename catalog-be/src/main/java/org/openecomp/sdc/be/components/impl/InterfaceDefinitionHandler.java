/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.components.impl;

import static org.openecomp.sdc.be.components.impl.ImportUtils.Constants.QUOTE;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DEFAULT;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DESCRIPTION;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.IMPLEMENTATION;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.INPUTS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.NOTIFICATIONS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.OPERATIONS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.REQUIRED;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.STATUS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.TYPE;

import com.google.gson.Gson;
import fj.data.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InputDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.operations.impl.UniqueIdBuilder;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.tosca.utils.OperationArtifactUtil;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles interface definition TOSCA conversions
 */
@Component("interfaceDefinitionHandler")
public class InterfaceDefinitionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceDefinitionHandler.class);
    private static final String WITH_ATTRIBUTE = "with attribute '{}': '{}'";
    private final InterfaceOperationBusinessLogic interfaceOperationBusinessLogic;

    public InterfaceDefinitionHandler(final InterfaceOperationBusinessLogic interfaceOperationBusinessLogic) {
        this.interfaceOperationBusinessLogic = interfaceOperationBusinessLogic;
    }

    /**
     * Creates an interface definition based on a TOSCA map representing an interface definition.
     *
     * @param interfaceDefinitionToscaMap the TOSCA interface definition structure
     * @return an interface definition representation
     */
    public InterfaceDefinition create(final Map<String, Object> interfaceDefinitionToscaMap, final String model) {
        final InterfaceDefinition interfaceDefinition = new InterfaceDefinition();
        interfaceDefinition.setModel(model);
        if (interfaceDefinitionToscaMap.containsKey(TYPE.getElementName())) {
            final Object typeObj = interfaceDefinitionToscaMap.get(TYPE.getElementName());
            if (!(typeObj instanceof String)) {
                throw new ByActionStatusComponentException(ActionStatus.INVALID_YAML);
            }
            final String type = (String) typeObj;
            interfaceDefinition.setType(type);
            interfaceDefinition.setUniqueId(type);
        }
        final Map<String, InputDefinition> inputDefinitionMap = handleInputs(interfaceDefinitionToscaMap);
        if (!inputDefinitionMap.isEmpty()) {
            final Map<String, InputDataDefinition> collect = inputDefinitionMap.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, value -> new InputDataDefinition(value.getValue())));
            interfaceDefinition.setInputs(collect);
        }
        final Map<String, OperationDataDefinition> operationMap;
        if (interfaceDefinitionToscaMap.containsKey(OPERATIONS.getElementName()) || interfaceDefinitionToscaMap
            .containsKey(NOTIFICATIONS.getElementName())) {
            operationMap = handleOperations(interfaceDefinitionToscaMap);
            //TODO handle notifications
        } else {
            operationMap = handleLegacyOperations(interfaceDefinitionToscaMap);
        }
        if (!operationMap.isEmpty()) {
            validateOperations(interfaceDefinition.getType(), operationMap, model);
            interfaceDefinition.setOperations(operationMap);
        }
        return interfaceDefinition;
    }

    private void validateOperations(final String interfaceType, final Map<String, OperationDataDefinition> operationMap, final String model) {
        if (MapUtils.isEmpty(operationMap)) {
            return;
        }
        Either<Map<String, InterfaceDefinition>, ResponseFormat> interfaceDefinitionMapEither = interfaceOperationBusinessLogic
            .getAllInterfaceLifecycleTypes(model);
        if (interfaceDefinitionMapEither.isRight() || MapUtils.isEmpty(interfaceDefinitionMapEither.left().value())) {
            throw new ByActionStatusComponentException(ActionStatus.INTERFACE_UNKNOWN, interfaceType);
        }
        final Map<String, InterfaceDefinition> interfaceDefinitionMap = interfaceDefinitionMapEither.left().value();
        final Optional<InterfaceDefinition> interfaceDefinitionOptional = interfaceDefinitionMap.entrySet().stream()
            .filter(interfaceDefinitionEntry -> interfaceDefinitionEntry.getKey().equalsIgnoreCase(UniqueIdBuilder.buildInterfaceTypeUid(model, interfaceType))).map(Entry::getValue).findFirst();
        if (interfaceDefinitionOptional.isEmpty()) {
            throw new ByActionStatusComponentException(ActionStatus.INTERFACE_UNKNOWN, interfaceType);
        }
        final InterfaceDefinition interfaceDefinition = interfaceDefinitionOptional.get();
        operationMap.keySet().forEach(operation1 -> {
            if (!interfaceDefinition.hasOperation(operation1)) {
                throw new ByActionStatusComponentException(ActionStatus.INTERFACE_OPERATION_NOT_DEFINED, operation1, interfaceType);
            }
        });
    }

    private Map<String, OperationDataDefinition> handleOperations(final Map<String, Object> interfaceDefinitionToscaMap) {
        if (!interfaceDefinitionToscaMap.containsKey(OPERATIONS.getElementName())) {
            return Collections.emptyMap();
        }
        final Map<String, Object> operationMap = (Map<String, Object>) interfaceDefinitionToscaMap.get(OPERATIONS.getElementName());
        return operationMap.entrySet().stream()
            .map(interfaceEntry -> createOperation(interfaceEntry.getKey(), (Map<String, Object>) interfaceEntry.getValue()))
            .collect(Collectors.toMap(OperationDataDefinition::getName, operationDataDefinition -> operationDataDefinition));
    }

    private Map<String, OperationDataDefinition> handleLegacyOperations(final Map<String, Object> interfaceDefinitionToscaMap) {
        final List<String> notALegacyOperationEntry = Arrays
            .asList(OPERATIONS.getElementName(), TYPE.getElementName(), INPUTS.getElementName(), NOTIFICATIONS.getElementName());
        return interfaceDefinitionToscaMap.entrySet().stream().filter(interfaceEntry -> !notALegacyOperationEntry.contains(interfaceEntry.getKey()))
            .map(interfaceEntry -> createOperation(interfaceEntry.getKey(), (Map<String, Object>) interfaceEntry.getValue()))
            .collect(Collectors.toMap(OperationDataDefinition::getName, operationDataDefinition -> operationDataDefinition));
    }

    private OperationDataDefinition createOperation(
            final String operationName,
            final Map<String, Object> operationDefinitionMap
    ) {
        final OperationDataDefinition operation = new OperationDataDefinition();
        operation.setUniqueId(UUID.randomUUID().toString());
        operation.setName(operationName);
        Object operationDescription = operationDefinitionMap.get(
                DESCRIPTION.getElementName()
        );
        if (null != operationDescription) {
            operation.setDescription(operationDescription.toString());
        }
        operation.setImplementation(handleOperationImplementation(operationDefinitionMap).orElse(new ArtifactDataDefinition()));
        if (operationDefinitionMap.containsKey(INPUTS.getElementName())) {
            final Map<String, Object> interfaceInputs = (Map<String, Object>) operationDefinitionMap.get(INPUTS.getElementName());
            operation.setInputs(handleInterfaceOperationInputs(interfaceInputs));
        }
        return operation;
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

    private void handleInputToscaDefinition(final String inputName, final Object value, final OperationInputDefinition operationInput) {
        if (value instanceof Map) {
            final LinkedHashMap<String, Object> inputPropertyValue = (LinkedHashMap<String, Object>) value;
            LOGGER.debug("Creating interface operation input '{}'", inputName);
            if (inputPropertyValue.get(TYPE.getElementName()) != null) {
                final String type = inputPropertyValue.get(TYPE.getElementName()).toString();
                LOGGER.debug(WITH_ATTRIBUTE, TYPE.getElementName(), type);
                operationInput.setType(type);
            }
            if (inputPropertyValue.get(DESCRIPTION.getElementName()) != null) {
                final String description = inputPropertyValue.get(DESCRIPTION.getElementName()).toString();
                LOGGER.debug(WITH_ATTRIBUTE, DESCRIPTION.getElementName(), description);
                operationInput.setDescription(description);
            }
            if (inputPropertyValue.get(REQUIRED.getElementName()) != null) {
                final boolean required = Boolean.parseBoolean(inputPropertyValue.get(REQUIRED.getElementName()).toString());
                LOGGER.debug(WITH_ATTRIBUTE, REQUIRED.getElementName(), required);
                operationInput.setRequired(required);
            }
            if (inputPropertyValue.get(DEFAULT.getElementName()) != null) {
                final Gson gson = new Gson();
                final String json = gson.toJson(inputPropertyValue.get(DEFAULT.getElementName()));
                LOGGER.debug(WITH_ATTRIBUTE, DEFAULT.getElementName(), json);
                operationInput.setToscaDefaultValue(json);
            }
            if (inputPropertyValue.get(STATUS.getElementName()) != null) {
                final String status = inputPropertyValue.get(STATUS.getElementName()).toString();
                LOGGER.debug(WITH_ATTRIBUTE, STATUS.getElementName(), status);
                operationInput.setStatus(status);
            }
            return;
        }
        if (value instanceof String) {
            final String stringValue = (String) value;
            operationInput.setDefaultValue(stringValue);
            operationInput.setToscaDefaultValue(stringValue);
            operationInput.setValue(stringValue);
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<ArtifactDataDefinition> handleOperationImplementation(final Map<String, Object> operationDefinitionMap) {
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
                        propertyDef.setType(type.getType());
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
    

    private Map<String, InputDefinition> handleInputs(final Map<String, Object> interfaceDefinitionToscaMap) {
        if (!interfaceDefinitionToscaMap.containsKey(INPUTS.getElementName())) {
            return Collections.emptyMap();
        }
        final Either<Map<String, InputDefinition>, ResultStatusEnum> inputMapEither = ImportUtils.getInputs(interfaceDefinitionToscaMap);
        if (inputMapEither.isRight()) {
            return Collections.emptyMap();
        }
        return inputMapEither.left().value();
    }
}
