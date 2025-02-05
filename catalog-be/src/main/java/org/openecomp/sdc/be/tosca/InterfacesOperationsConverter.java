/*
 * Copyright © 2016-2020 European Support Limited
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

package org.openecomp.sdc.be.tosca;

import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.DEFAULT;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.INPUTS;
import static org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum.OPERATIONS;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.openecomp.sdc.be.datatypes.elements.ActivityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.FilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InputDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.MilestoneDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.OperationInputDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ActivityTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ConstraintType;
import org.openecomp.sdc.be.datatypes.enums.MilestoneTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.constraints.EqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterOrEqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.MaxLengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.MinLengthConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.ValidValuesConstraint;
import org.openecomp.sdc.be.tosca.PropertyConvertor.PropertyType;
import org.openecomp.sdc.be.tosca.model.ToscaActivity;
import org.openecomp.sdc.be.tosca.model.ToscaArtifactDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaInput;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaInterfaceOperationImplementation;
import org.openecomp.sdc.be.tosca.model.ToscaLifecycleOperationDefinition;
import org.openecomp.sdc.be.tosca.model.ToscaMilestone;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyAssignment;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyAssignmentJsonSerializer;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraint;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintEqual;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintGreaterOrEqual;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintGreaterThan;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintInRange;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintLength;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintLessOrEqual;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintLessThan;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintMaxLength;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintMinLength;
import org.openecomp.sdc.be.tosca.model.ToscaPropertyConstraintValidValues;
import org.openecomp.sdc.be.tosca.utils.OperationArtifactUtil;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.tosca.datatypes.ToscaFunctions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InterfacesOperationsConverter {

    public static final String SELF = "SELF";
    private static final String DERIVED_FROM_STANDARD_INTERFACE = "tosca.interfaces.node.lifecycle.Standard";
    private static final String DERIVED_FROM_BASE_DEFAULT = "org.openecomp.interfaces.node.lifecycle.";
    private static final String DEFAULT_HAS_UNDERSCORE = "_default";
    private static final String DOT = ".";
    private static final String DEFAULTP = "defaultp";
    private static final String LOCAL_INTERFACE_TYPE = "Local";
    private final PropertyConvertor propertyConvertor;

    @Autowired
    public InterfacesOperationsConverter(final PropertyConvertor propertyConvertor) {
        this.propertyConvertor = propertyConvertor;
    }

    private static Object getDefaultValue(Map<String, Object> inputValueMap) {
        Object defaultValue = null;
        for (Map.Entry<String, Object> operationEntry : inputValueMap.entrySet()) {
            final Object value = operationEntry.getValue();
            if (value instanceof Map) {
                getDefaultValue((Map<String, Object>) value);
            }
            final String key = operationEntry.getKey();
            if (key.equals(DEFAULTP)) {
                defaultValue = inputValueMap.remove(key);
            }
        }
        return defaultValue;
    }

    //Remove input type and copy default value directly into the proxy node template from the node type
    private static void handleOperationInputValue(Map<String, Object> operationsMap, String parentKey) {
        for (Map.Entry<String, Object> operationEntry : operationsMap.entrySet()) {
            final Object value = operationEntry.getValue();
            final String key = operationEntry.getKey();
            if (value instanceof Map) {
                if (INPUTS.getElementName().equals(parentKey)) {
                    Object defaultValue = getDefaultValue((Map<String, Object>) value);
                    operationsMap.put(key, defaultValue);
                } else {
                    handleOperationInputValue((Map<String, Object>) value, key);
                }
            }
        }
    }

    private static String getLastPartOfName(String toscaResourceName) {
        return toscaResourceName.substring(toscaResourceName.lastIndexOf(DOT) + 1);
    }

    private static String getInputValue(final OperationInputDefinition input) {
        if (null != input.getToscaFunction()) {
            return input.getToscaFunction().getJsonObjectValue().toString();
        }
        String inputValue = input.getValue() == null ? input.getToscaDefaultValue() : input.getValue();
        if (inputValue != null && inputValue.contains(ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName())) {
            Gson gson = new Gson();
            Map<String, List<String>> consumptionValue = gson.fromJson(inputValue, Map.class);
            List<String> mappedOutputValue = consumptionValue.get(ToscaFunctions.GET_OPERATION_OUTPUT.getFunctionName());
            //Extract the interface name from the interface type
            String interfaceType = mappedOutputValue.get(1);
            String interfaceName = interfaceType.substring(interfaceType.lastIndexOf('.') + 1);
            mappedOutputValue.remove(1);
            mappedOutputValue.add(1, interfaceName);
            inputValue = gson.toJson(consumptionValue);
        }
        return inputValue;
    }

    private static String getInterfaceType(Component component, String interfaceType) {
        if (LOCAL_INTERFACE_TYPE.equals(interfaceType)) {
            return DERIVED_FROM_BASE_DEFAULT + component.getComponentMetadataDefinition().getMetadataDataDefinition().getSystemName();
        }
        return interfaceType;
    }

    private static Map<String, Object> getObjectAsMap(final Object obj) {
        final Map<String, Object> objectAsMap;
        if (obj instanceof Map) {
            objectAsMap = (Map<String, Object>) obj;
        } else {
            final ObjectMapper objectMapper = new ObjectMapper();
            final SimpleModule module = new SimpleModule("ToscaPropertyAssignmentSerializer");
            module.addSerializer(ToscaPropertyAssignment.class, new ToscaPropertyAssignmentJsonSerializer());
            objectMapper.registerModule(module);
            if (obj instanceof ToscaInterfaceDefinition) {
                //Prevent empty field serialization in interface definition
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            }
            objectAsMap = objectMapper.convertValue(obj, Map.class);
        }

        final String defaultEntry = DEFAULT.getElementName();
        if (objectAsMap.containsKey(defaultEntry)) {
            objectAsMap.put(DEFAULT_HAS_UNDERSCORE, objectAsMap.remove(defaultEntry));
        }
        return objectAsMap;
    }

    /**
     * Creates the interface_types element.
     *
     * @param component to work on
     * @return the added element
     */
    public Map<String, Object> addInterfaceTypeElement(Component component, List<String> allInterfaceTypes) {
        if (component instanceof Product) {
            return null;
        }
        final Map<String, InterfaceDefinition> interfaces = component.getInterfaces();
        if (MapUtils.isEmpty(interfaces)) {
            return null;
        }
        Map<String, Object> toscaInterfaceTypes = new HashMap<>();
        for (InterfaceDefinition interfaceDefinition : interfaces.values()) {
            boolean isInterfaceTypeExistInGlobalType = allInterfaceTypes.stream()
                .anyMatch(type -> type.equalsIgnoreCase(interfaceDefinition.getType()));
            if (!isInterfaceTypeExistInGlobalType) {
                ToscaInterfaceNodeType toscaInterfaceType = new ToscaInterfaceNodeType();
                toscaInterfaceType.setDerived_from(DERIVED_FROM_STANDARD_INTERFACE);
                final Map<String, OperationDataDefinition> operations = interfaceDefinition.getOperations();
                Map<String, Object> toscaOperations = new HashMap<>();
                for (Map.Entry<String, OperationDataDefinition> operationEntry : operations.entrySet()) {
                    toscaOperations.put(operationEntry.getValue().getName(), null);
                }
                toscaInterfaceType.setOperations(toscaOperations);
                Map<String, Object> interfacesAsMap = getObjectAsMap(toscaInterfaceType);
                Map<String, Object> operationsMap = (Map<String, Object>) interfacesAsMap.remove(OPERATIONS.getElementName());
                interfacesAsMap.putAll(operationsMap);
                toscaInterfaceTypes.put(getInterfaceType(component, LOCAL_INTERFACE_TYPE), interfacesAsMap);
            }
        }
        return MapUtils.isNotEmpty(toscaInterfaceTypes) ? toscaInterfaceTypes : null;
    }

    private boolean isArtifactPresent(final OperationDataDefinition operationDataDefinition) {
        return operationDataDefinition.getImplementation() != null
            && StringUtils.isNotEmpty(operationDataDefinition.getImplementation().getArtifactName());
    }

    /**
     * Adds the 'interfaces' element to the node type provided.
     *
     * @param component to work on
     * @param nodeType  to which the interfaces element will be added
     */
    public void addInterfaceDefinitionElement(Component component, ToscaNodeType nodeType, Map<String, DataTypeDefinition> dataTypes,
                                              boolean isAssociatedComponent) {
        if (component instanceof Product) {
            return;
        }
        final Map<String, InterfaceDefinition> interfaces = component.getInterfaces();
        if (MapUtils.isEmpty(interfaces)) {
            return;
        }
        Map<String, Object> toscaInterfaceDefinitions = getInterfacesMap(component, dataTypes, isAssociatedComponent);
        if (MapUtils.isNotEmpty(toscaInterfaceDefinitions)) {
            nodeType.setInterfaces(toscaInterfaceDefinitions);
        }
    }

    private Map<String, Object> getInterfacesMap(Component component, Map<String, DataTypeDefinition> dataTypes, boolean isAssociatedComponent) {
        return getInterfacesMap(component, null, component.getInterfaces(), dataTypes, isAssociatedComponent);
    }

    public Map<String, Object> getInterfacesMap(final Component component, final ComponentInstance componentInstance,
                                                final Map<String, InterfaceDefinition> interfaces, final Map<String, DataTypeDefinition> dataTypes,
                                                final boolean isAssociatedComponent) {
        if (MapUtils.isEmpty(interfaces)) {
            return null;
        }
        final Map<String, Object> toscaInterfaceDefinitions = new HashMap<>();
        for (InterfaceDefinition interfaceDefinition : interfaces.values()) {
            handleInterfaceOperations(component, componentInstance, dataTypes, isAssociatedComponent,
                toscaInterfaceDefinitions, interfaceDefinition);
        }
        return toscaInterfaceDefinitions;
    }

    public Map<String, Object> getInterfacesMapFromComponentInstance(final Component component, final ComponentInstance componentInstance,
                                                                     final Map<String, DataTypeDefinition> dataTypes,
                                                                     final boolean isAssociatedComponent) {
        final Map<String, Object> toscaInterfaceDefinitions = new HashMap<>();
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        for (final Map.Entry<String, Object> interfaceEntry : componentInstance.getInterfaces().entrySet()) {
            final InterfaceDefinition interfaceDefinition = objectMapper.convertValue(interfaceEntry.getValue(), InterfaceDefinition.class);
            handleInterfaceOperations(component, componentInstance, dataTypes, isAssociatedComponent,
                toscaInterfaceDefinitions, interfaceDefinition);
        }
        return toscaInterfaceDefinitions;
    }

    private void handleInterfaceOperations(final Component component, final ComponentInstance componentInstance,
                                           final Map<String, DataTypeDefinition> dataTypes, final boolean isAssociatedComponent,
                                           final Map<String, Object> toscaInterfaceDefinitions,
                                           final InterfaceDefinition interfaceDefinition) {
        final String interfaceType;
        if (componentInstance != null && LOCAL_INTERFACE_TYPE.equals(interfaceDefinition.getType())) {
            interfaceType = DERIVED_FROM_BASE_DEFAULT + componentInstance.getSourceModelName();
        } else {
            interfaceType = getInterfaceType(component, interfaceDefinition.getType());
        }
        final ToscaInterfaceDefinition toscaInterfaceDefinition = new ToscaInterfaceDefinition();
        if (componentInstance == null) {
            toscaInterfaceDefinition.setType(interfaceType);
        }
        final Map<String, OperationDataDefinition> operations = interfaceDefinition.getOperations();
        final Map<String, Object> toscaOperationMap = new HashMap<>();
        for (final Entry<String, OperationDataDefinition> operationEntry : operations.entrySet()) {
            if (operationHasAnImplementation(operationEntry.getValue())) {
                final ToscaLifecycleOperationDefinition toscaLifecycleOperationDefinition = new ToscaLifecycleOperationDefinition();
                handleInterfaceOperationImplementation(component, componentInstance, isAssociatedComponent, operationEntry.getValue(),
                    toscaLifecycleOperationDefinition, dataTypes);
                if (StringUtils.isNotEmpty(operationEntry.getValue().getDescription())) {
                    toscaLifecycleOperationDefinition.setDescription(operationEntry.getValue().getDescription());
                }
                fillToscaOperationInputs(operationEntry.getValue(), dataTypes, toscaLifecycleOperationDefinition);
                fillToscaOperationMilestones(operationEntry.getValue(), dataTypes, toscaLifecycleOperationDefinition);
                toscaOperationMap.put(operationEntry.getValue().getName(), toscaLifecycleOperationDefinition);
            }
        }
        toscaInterfaceDefinition.setOperations(toscaOperationMap);
        final Map<String, Object> interfaceInputMap = createInterfaceInputMap(interfaceDefinition, dataTypes);
        if (MapUtils.isNotEmpty(interfaceInputMap)) {
            toscaInterfaceDefinition.setInputs(interfaceInputMap);
        }
        final Map<String, Object> interfaceDefinitionAsMap = getObjectAsMap(toscaInterfaceDefinition);
        if (interfaceDefinitionAsMap.containsKey(INPUTS.getElementName())) {
            handleDefaults((Map<String, Object>) interfaceDefinitionAsMap.get(INPUTS.getElementName()));
        }
        final Map<String, Object> operationsMap = (Map<String, Object>) interfaceDefinitionAsMap.remove(OPERATIONS.getElementName());
        handleOperationInputValue(operationsMap, interfaceType);
        interfaceDefinitionAsMap.putAll(operationsMap);
        toscaInterfaceDefinitions.put(getLastPartOfName(interfaceType), interfaceDefinitionAsMap);
    }

    private void fillToscaOperationMilestones(OperationDataDefinition operation, Map<String, DataTypeDefinition> dataTypes,
                                              ToscaLifecycleOperationDefinition toscaOperation) {
        if (Objects.isNull(operation.getMilestones()) || operation.getMilestones().isEmpty()) {
            toscaOperation.setMilestones(null);
            return;
        }
        Map<String, ToscaMilestone> toscaMilestones = new HashMap<>();
        for (Entry<String, MilestoneDataDefinition> milestone : operation.getMilestones().entrySet()) {
            ListDataDefinition<ActivityDataDefinition> activities = milestone.getValue().getActivities();
            if (MilestoneTypeEnum.getEnum(milestone.getKey()).isEmpty() || activities == null || activities.isEmpty()) {
                continue;
            }
            List<Map<String, ToscaActivity>> toscaActivities = new ArrayList<>();
            for (ActivityDataDefinition activity : activities.getListToscaDataDefinition()) {
                if (ActivityTypeEnum.getEnum(activity.getType()).isEmpty()) {
                    continue;
                }
                Map<String, ToscaActivity> toscaActivityMap = new HashMap<>();
                ToscaActivity toscaActivity = new ToscaActivity();
                toscaActivity.setWorkflow(activity.getWorkflow());
                Map<String, Object> inputs = getToscaActivityInputs(activity.getInputs(), dataTypes);
                if (MapUtils.isNotEmpty(inputs)) {
                    toscaActivity.setInputs(inputs);
                }
                toscaActivityMap.put(activity.getType(), toscaActivity);
                toscaActivities.add(toscaActivityMap);
            }
            ToscaMilestone toscaMilestone = new ToscaMilestone();
            toscaMilestone.setActivities(toscaActivities);
            toscaMilestone.setFilters(getToscaFilters(milestone.getValue().getFilters()));
            toscaMilestones.put(milestone.getKey(), toscaMilestone);
        }
        toscaOperation.setMilestones(toscaMilestones);
    }

    private List<Map<String, ToscaPropertyConstraint>> getToscaFilters(ListDataDefinition<FilterDataDefinition> filters) {
        if (filters != null && !filters.isEmpty()) {
            List<Map<String, ToscaPropertyConstraint>> toscaFilters = new ArrayList<>();
            for (FilterDataDefinition filter : filters.getListToscaDataDefinition()) {
                Optional<ConstraintType> typeOptional = ConstraintType.findByType(filter.getConstraint());
                if (typeOptional.isEmpty()) {
                    continue;
                }
                ConstraintType type = typeOptional.get();
                Object value = filter.isToscaFunction() ? filter.getToscaFunction().getJsonObjectValue() : filter.getFilterValue();
                if (ConstraintType.EQUAL.equals(type)) {
                    EqualConstraint equalConstraint = new EqualConstraint(value);
                    ToscaPropertyConstraint prop = new ToscaPropertyConstraintEqual(equalConstraint.getEqual());
                    toscaFilters.add(Map.of(filter.getName(), prop));
                }
                if (ConstraintType.GREATER_THAN.equals(type)) {
                    GreaterThanConstraint greaterThanConstraint = new GreaterThanConstraint(value);
                    ToscaPropertyConstraintGreaterThan prop = new ToscaPropertyConstraintGreaterThan(greaterThanConstraint.getGreaterThan());
                    toscaFilters.add(Map.of(filter.getName(), prop));
                }
                if (ConstraintType.GREATER_OR_EQUAL.equals(type)) {
                    GreaterOrEqualConstraint greaterOrEqualConstraint = new GreaterOrEqualConstraint<>(value);
                    ToscaPropertyConstraintGreaterOrEqual prop =
                        new ToscaPropertyConstraintGreaterOrEqual(greaterOrEqualConstraint.getGreaterOrEqual());
                    toscaFilters.add(Map.of(filter.getName(), prop));
                }
                if (ConstraintType.LESS_THAN.equals(type)) {
                    LessThanConstraint lessThanConstraint = new LessThanConstraint(value);
                    ToscaPropertyConstraintLessThan prop = new ToscaPropertyConstraintLessThan(lessThanConstraint.getLessThan());
                    toscaFilters.add(Map.of(filter.getName(), prop));
                }
                if (ConstraintType.LESS_OR_EQUAL.equals(type)) {
                    LessOrEqualConstraint lessOrEqualConstraint = new LessOrEqualConstraint<>(value);
                    ToscaPropertyConstraintLessOrEqual prop = new ToscaPropertyConstraintLessOrEqual(lessOrEqualConstraint.getLessOrEqual());
                    toscaFilters.add(Map.of(filter.getName(), prop));
                }
                if (ConstraintType.IN_RANGE.equals(type)) {
                    InRangeConstraint inRangeConstraint = new InRangeConstraint((List<Object>) value);
                    toscaFilters.add(Map.of(filter.getName(), new ToscaPropertyConstraintInRange(inRangeConstraint.getInRange())));
                }
                if (ConstraintType.VALID_VALUES.equals(type)) {
                    ValidValuesConstraint validValues = new ValidValuesConstraint((List<Object>) value);
                    List prop = validValues.getValidValues();
                    toscaFilters.add(Map.of(filter.getName(), (new ToscaPropertyConstraintValidValues(prop))));
                }
                if (ConstraintType.LENGTH.equals(type)) {
                    LengthConstraint lengthConstraint = new LengthConstraint((Integer) value);
                    toscaFilters.add(Map.of(filter.getName(), (new ToscaPropertyConstraintLength(lengthConstraint.getLength()))));
                }
                if (ConstraintType.MIN_LENGTH.equals(type)) {
                    MinLengthConstraint minLengthConstraint = new MinLengthConstraint((Integer) value);
                    toscaFilters.add(Map.of(filter.getName(), new ToscaPropertyConstraintMinLength(minLengthConstraint.getMinLength())));
                }
                if (ConstraintType.MAX_LENGTH.equals(type)) {
                    MaxLengthConstraint maxLengthConstraint = new MaxLengthConstraint((Integer) value);
                    toscaFilters.add(Map.of(filter.getName(), new ToscaPropertyConstraintMaxLength(maxLengthConstraint.getMaxLength())));
                }
            }
            return toscaFilters;
        }
        return null;
    }

    private Map<String, Object> getToscaActivityInputs(ListDataDefinition<OperationInputDefinition> inputs,
                                                       Map<String, DataTypeDefinition> dataTypes) {
        if (Objects.isNull(inputs) || inputs.isEmpty()) {
            return null;
        }
        Map<String, Object> toscaInputs = new HashMap<>();
        for (OperationInputDefinition input : inputs.getListToscaDataDefinition()) {
            Object value = propertyConvertor.convertToToscaObject(input, getInputValue(input), dataTypes, false);
            if (ObjectUtils.isNotEmpty(value)) {
                toscaInputs.put(input.getName(), value);
            }
        }
        return toscaInputs;
    }

    private boolean operationHasAnImplementation(OperationDataDefinition operation) {
        return operation.getImplementation() != null && StringUtils.isNotEmpty(operation.getImplementation().getArtifactName()) &&
            !operation.getImplementation().getArtifactName().equals("''");
    }

    private void handleInterfaceOperationImplementation(final Component component, final ComponentInstance componentInstance,
                                                        final boolean isAssociatedComponent,
                                                        final OperationDataDefinition operationDataDefinition,
                                                        final ToscaLifecycleOperationDefinition toscaOperation,
                                                        final Map<String, DataTypeDefinition> dataTypes) {
        final ArtifactDataDefinition implementation = operationDataDefinition.getImplementation();
        if (implementation == null) {
            return;
        }

        if (isArtifactPresent(operationDataDefinition)) {
            final String operationArtifactPath =
                OperationArtifactUtil.createOperationArtifactPath(component, componentInstance, operationDataDefinition, isAssociatedComponent);
            final ToscaInterfaceOperationImplementation toscaInterfaceOperationImplementation = new ToscaInterfaceOperationImplementation();
            if (implementation.getTimeout() != null && implementation.getTimeout() > 0) {
                toscaInterfaceOperationImplementation.setTimeout(implementation.getTimeout());
            }
            if (implementation.getArtifactType() != null) {
                final ToscaArtifactDefinition toscaArtifactDefinition = new ToscaArtifactDefinition();
                toscaArtifactDefinition.setFile(operationArtifactPath);
                final String artifactVersion = implementation.getArtifactVersion();
                toscaArtifactDefinition.setArtifact_version(!artifactVersion.equals(NumberUtils.INTEGER_ZERO.toString()) ? artifactVersion : null);
                toscaArtifactDefinition.setType(implementation.getArtifactType());
                final Map<String, ToscaPropertyAssignment> propertiesMap = handleImplementationProperties(operationDataDefinition, dataTypes);
                if (MapUtils.isNotEmpty(propertiesMap)) {
                    toscaArtifactDefinition.setProperties(propertiesMap);
                }
                toscaInterfaceOperationImplementation.setPrimary(toscaArtifactDefinition);
                toscaOperation.setImplementation(toscaInterfaceOperationImplementation);
            } else {
                if (toscaInterfaceOperationImplementation.getTimeout() != null) {
                    final ToscaArtifactDefinition toscaArtifactDefinition = new ToscaArtifactDefinition();
                    toscaArtifactDefinition.setFile(
                        StringUtils.isBlank(operationArtifactPath) || "null".equals(operationArtifactPath) ? null : operationArtifactPath);
                    toscaInterfaceOperationImplementation.setPrimary(toscaArtifactDefinition);
                    toscaOperation.setImplementation(toscaInterfaceOperationImplementation);
                } else {
                    toscaOperation.setImplementation(
                        StringUtils.isBlank(operationArtifactPath) || "null".equals(operationArtifactPath) ? null : operationArtifactPath);
                }
            }
        }
    }

    private Map<String, ToscaPropertyAssignment> handleImplementationProperties(final OperationDataDefinition operationDataDefinition,
                                                                                final Map<String, DataTypeDefinition> dataTypes) {
        if (operationDataDefinition.getImplementation() == null) {
            return new HashMap<>();
        }

        final List<PropertyDataDefinition> properties = operationDataDefinition.getImplementation().getProperties();
        if (CollectionUtils.isEmpty(properties)) {
            return new HashMap<>();
        }

        final Map<String, ToscaPropertyAssignment> propertiesMap = new HashMap<>();
        properties.stream()
            .filter(propertyDataDefinition -> StringUtils.isNotEmpty(propertyDataDefinition.getValue()))
            .forEach(propertyDataDefinition -> {
                    final String propertyValue =
                        propertyDataDefinition.getValue() != null ? propertyDataDefinition.getValue() : propertyDataDefinition.getDefaultValue();
                    final ToscaPropertyAssignment toscaPropertyAssignment = new ToscaPropertyAssignment();
                    toscaPropertyAssignment.setValue(propertyConvertor.convertToToscaObject(propertyDataDefinition, propertyValue, dataTypes, false));
                    propertiesMap.put(propertyDataDefinition.getName(), toscaPropertyAssignment);
                }
            );

        return propertiesMap;
    }

    public void removeInterfacesWithoutOperations(final Map<String, Object> interfaceMap) {
        if (MapUtils.isEmpty(interfaceMap)) {
            return;
        }
        final Set<String> emptyInterfaces = interfaceMap.entrySet().stream().filter(entry -> {
            final Object value = entry.getValue();
            if (value instanceof ToscaInterfaceDefinition) {
                final ToscaInterfaceDefinition interfaceDefinition = (ToscaInterfaceDefinition) value;
                return MapUtils.isEmpty(interfaceDefinition.getOperations());
            } else if (value instanceof Map) {
                final Map<String, Object> interfaceDefMap = (Map<String, Object>) value;
                return MapUtils.isEmpty(interfaceDefMap);
            }
            return false;
        }).map(Entry::getKey).collect(Collectors.toSet());
        emptyInterfaces.forEach(interfaceMap::remove);
    }

    private Map<String, Object> createInterfaceInputMap(final InterfaceDefinition interfaceDefinition,
                                                        final Map<String, DataTypeDefinition> allDataTypeMap) {
        final Map<String, InputDataDefinition> inputMap = interfaceDefinition.getInputs();
        if (MapUtils.isEmpty(inputMap)) {
            return Collections.emptyMap();
        }
        final Map<String, Object> toscaInterfaceInputMap = new HashMap<>();
        for (final Entry<String, InputDataDefinition> inputEntry : inputMap.entrySet()) {
            final InputDataDefinition inputDataDefinition = inputEntry.getValue();
            final ToscaProperty toscaProperty = propertyConvertor
                .convertProperty(allDataTypeMap, new PropertyDefinition(inputDataDefinition), PropertyType.INPUT);
            toscaInterfaceInputMap.put(inputEntry.getKey(), new ToscaInput(toscaProperty));
        }
        return toscaInterfaceInputMap;
    }

    /*
     * workaround for : currently "defaultp" is not being converted to "default" by the relevant code in
     * ToscaExportHandler so, any string Map key named "defaultp" will have its named changed to "default"
     * @param operationsMap the map to update
     */
    private void handleDefaults(Map<String, Object> operationsMap) {
        for (Map.Entry<String, Object> operationEntry : operationsMap.entrySet()) {
            final Object value = operationEntry.getValue();
            if (value instanceof Map) {
                handleDefaults((Map<String, Object>) value);
            }
            final String key = operationEntry.getKey();
            if (key.equals(DEFAULTP)) {
                Object removed = operationsMap.remove(key);
                operationsMap.put(ToscaTagNamesEnum.DEFAULT.getElementName(), removed);
            }
        }
    }

    private void fillToscaOperationInputs(OperationDataDefinition operation, Map<String, DataTypeDefinition> dataTypes,
                                          ToscaLifecycleOperationDefinition toscaOperation) {
        if (Objects.isNull(operation.getInputs()) || operation.getInputs().isEmpty()) {
            toscaOperation.setInputs(null);
            return;
        }
        Map<String, ToscaProperty> toscaInputs = new HashMap<>();
        for (OperationInputDefinition input : operation.getInputs().getListToscaDataDefinition()) {
            ToscaProperty toscaInput = new ToscaProperty();
            toscaInput.setDescription(input.getDescription());
            toscaInput.setType(input.getType());
            toscaInput.setRequired(input.isRequired());
            toscaInput.setDefaultp(propertyConvertor.convertToToscaObject(input, getInputValue(input), dataTypes, false));
            toscaInputs.put(input.getName(), toscaInput);
        }
        toscaOperation.setInputs(toscaInputs);
    }

}
