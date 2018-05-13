package org.openecomp.sdc.be.components.merge.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;

@org.springframework.stereotype.Component
public class InputsValuesMergingBusinessLogic {

    /**
     * Merge old inputs values into the updated inputs
     * An input value is merged if the input previous version had a user defined value and its value is empty in current version
     * @param oldInputs the currently persisted inputs mapped by their names
     * @param updatedInputs the currently being update inputs mapped by their names
     */
    public void mergeComponentInputs(Map<String, InputDefinition> oldInputs, Map<String, InputDefinition> updatedInputs) {
        updatedInputs.forEach((inputName, input) -> mergeInputsValues(oldInputs.get(inputName), input));
    }

    /**
     * @param oldComponent the old state of {@link Component} that is being updated
     * @param newComponent the new state of {@link Component} that is being updated
     * @return a list of all inputs that were previously declared and need to be merged to the updating component
     * An input needs to merged if a property was declared as an input (by the user) in previous component version and the declared input not exist in new version
     */
    public List<InputDefinition> getPreviouslyDeclaredInputsToMerge(Component oldComponent, Component newComponent) {
        if (oldComponent == null || oldComponent.getInputs() == null || newComponent == null ) {
            return Collections.emptyList();
        }
        Map<String, List<PropertyDataDefinition>> getInputProperties = getAllGetInputPropertyData(newComponent);
        List<RedeclareInputData> inputsToRedeclareData = buildRedeclareInputData(newComponent, getInputProperties);
        return findPrevDeclaredInputs(oldComponent.getInputs(), inputsToRedeclareData);
    }

    /**
     * @param oldInputs list of previous inputs to find inputs to redeclare from
     * @param newComponent the new state of {@link Component} that is being updated
     * @param instanceId instance id
     * @return a list of all inputs that were previously declared and need to be merged to the updating component
     * An input needs to merged if an instance property was declared as an input (by the user) in previous component version and the declared input not exist in new version
     */
    public List<InputDefinition> getPreviouslyDeclaredInputsToMerge(List<InputDefinition> oldInputs, Component newComponent, String instanceId) {
        if (oldInputs == null || newComponent == null ) {
            return Collections.emptyList();
        }
        Map<String, List<PropertyDataDefinition>> getInputProperties = getAllGetInputPropertyData(newComponent, instanceId);
        List<RedeclareInputData> inputsToRedeclareData = buildRedeclareInputData(newComponent, getInputProperties);
        return findPrevDeclaredInputs(oldInputs, inputsToRedeclareData);
    }

    private List<InputDefinition> findPrevDeclaredInputs(List<InputDefinition> oldInputs, List<RedeclareInputData> inputsToRedeclareData) {
        Map<String, InputDefinition> oldInputsById = MapUtil.toMap(oldInputs, InputDefinition::getUniqueId);
        List<InputDefinition> inputsToRedeclare = new ArrayList<>();
        inputsToRedeclareData.forEach(redeclareInputData -> {
            List<InputDefinition> inputDefinitions = prepareInputsForRedeclaration(oldInputsById, redeclareInputData);
            inputsToRedeclare.addAll(inputDefinitions);
        });
        return inputsToRedeclare;
    }

    private List<InputDefinition> prepareInputsForRedeclaration(Map<String, InputDefinition> oldInputsById, RedeclareInputData redeclareInputData) {
        List<InputDefinition> inputsForRedeclaration = redeclareInputData.declaredInputIds.stream().map(oldInputsById::get).collect(Collectors.toList());
        inputsForRedeclaration.forEach(input -> {
            input.setPropertyId(redeclareInputData.propertyId);
            input.setInstanceUniqueId(redeclareInputData.instanceId);
        });
        return inputsForRedeclaration;
    }

    private <T extends PropertyDataDefinition> Map<String, List<PropertyDataDefinition>> findGetInputPropsDefinitions(Map<String, List<T>> instancesPropDefinitions) {
        Map<String, List<PropertyDataDefinition>> getInputProps = new HashMap<>();
        if (instancesPropDefinitions == null) {
            return getInputProps;
        }
        return instancesPropDefinitions.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> this.filterGetInputProps(entry.getValue())));
    }

    private <T extends PropertyDataDefinition> List<PropertyDataDefinition> filterGetInputProps(List<T> propDefinitions) {
        return propDefinitions
                .stream()
                .filter(PropertyDataDefinition::isGetInputProperty)
                .collect(Collectors.toList());
    }

    private void mergeInputsValues(InputDefinition oldInput, InputDefinition updatedInput) {
        if (shouldMergeOldValue(oldInput, updatedInput)) {
            updatedInput.setDefaultValue(oldInput.getDefaultValue());
        }
    }

    private boolean shouldMergeOldValue(InputDefinition oldInput, InputDefinition newInput) {
        return isNonEmptyDefaultValue(oldInput) && isEmptyDefaultValue(newInput) && isSameType(oldInput, newInput);
    }

    private boolean isSameType(InputDefinition oldInput, InputDefinition updatedInput) {
        return oldInput.typeEquals(updatedInput);
    }

    private boolean isEmptyDefaultValue(InputDefinition input) {
        return input != null && StringUtils.isEmpty(input.getDefaultValue());
    }

    private boolean isNonEmptyDefaultValue(InputDefinition input) {
        return input != null && !isEmptyDefaultValue(input);
    }

    private List<RedeclareInputData> buildRedeclareInputData(Component newComponent, Map<String, List<PropertyDataDefinition>> getInputProperties) {
        Map<String, InputDefinition> inputsById = MapUtil.toMap(newComponent.getInputs(), InputDefinition::getUniqueId);
        List<RedeclareInputData> redeclareInputData = new ArrayList<>();
        getInputProperties.forEach((instanceId, getInputProps) ->  {
            redeclareInputData.addAll(findInputsToRedeclare(inputsById, instanceId, getInputProps));
        });
        return redeclareInputData;

    }

    private Map<String, List<PropertyDataDefinition>> getAllGetInputPropertyData(Component newComponent) {
        Map<String, List<PropertyDataDefinition>> getInputInstanceProps = findGetInputPropsDefinitions(newComponent.getComponentInstancesProperties());
        Map<String, List<PropertyDataDefinition>> getInputInstanceInputs = findGetInputPropsDefinitions(newComponent.getComponentInstancesInputs());
        getInputInstanceInputs.putAll(getInputInstanceProps);
        return getInputInstanceInputs;
    }

    private Map<String, List<PropertyDataDefinition>> getAllGetInputPropertyData(Component newComponent, String instanceId) {
        List<PropertyDataDefinition> getInputInstanceProps = this.filterGetInputProps(newComponent.safeGetComponentInstanceProperties(instanceId));
        List<PropertyDataDefinition> getInputInstanceInputs = this.filterGetInputProps(newComponent.safeGetComponentInstanceInput(instanceId));
        getInputInstanceInputs.addAll(getInputInstanceProps);
        return Collections.singletonMap(instanceId, getInputInstanceInputs);
    }

    private List<RedeclareInputData> findInputsToRedeclare(Map<String, InputDefinition> inputsById, String instanceId, List<PropertyDataDefinition> getInputProps) {
        List<RedeclareInputData> redeclareInputDataList = new ArrayList<>();
        getInputProps.forEach(property -> {
            List<String> inputsToRedeclareIds = findInputsToRedeclareIds(inputsById, property);
            RedeclareInputData redeclareInputData = new RedeclareInputData(property.getUniqueId(), inputsToRedeclareIds, instanceId);
            redeclareInputDataList.add(redeclareInputData);
        });
        return redeclareInputDataList;
    }

    private List<String> findInputsToRedeclareIds(Map<String, InputDefinition> inputsById, PropertyDataDefinition property) {
        List<GetInputValueDataDefinition> getInputValues = property.getGetInputValues();
        return getInputValues.stream()
                .filter(getInputVal -> getInputValueWithNoCorrespondingInput(getInputVal, inputsById))
                .map(GetInputValueDataDefinition::getInputId)
                .collect(Collectors.toList());
    }

    private boolean getInputValueWithNoCorrespondingInput(GetInputValueDataDefinition getInputVal, Map<String, InputDefinition> inputsById) {
        return !inputsById.containsKey(getInputVal.getInputId());
    }

    private class RedeclareInputData {
        private String propertyId;
        private List<String> declaredInputIds;
        private String instanceId;

        public RedeclareInputData(String propertyId, List<String> declaredInputIds, String instanceId) {
            this.propertyId = propertyId;
            this.declaredInputIds = declaredInputIds;
            this.instanceId = instanceId;
        }

    }


}