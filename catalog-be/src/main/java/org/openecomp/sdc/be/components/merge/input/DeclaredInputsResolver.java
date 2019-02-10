package org.openecomp.sdc.be.components.merge.input;


import org.openecomp.sdc.be.dao.utils.MapUtil;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.InputDefinition;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openecomp.sdc.be.utils.PropertyDefinitionUtils.resolveGetInputProperties;

@org.springframework.stereotype.Component
public class DeclaredInputsResolver {
    /**
     * @param oldComponent the old state of {@link Component} that is being updated
     * @param newComponent the new state of {@link Component} that is being updated
     * @param properties a list of properties
     * @return a list of all inputs that were previously declared and need to be merged to the updating component
     * An input needs to merged if a property was declared as an input (by the user) in previous component version and the declared input not exist in new version
     */
    List<InputDefinition> getPreviouslyDeclaredInputsToMerge(Component oldComponent, Component newComponent, Map<String, List<PropertyDataDefinition>> properties) {
        List<InputDefinition> oldInputs = oldComponent.safeGetInputs();
        return getPreviouslyDeclaredInputsToMerge(oldInputs, newComponent, properties);
    }

    public List<InputDefinition> getPreviouslyDeclaredInputsToMerge(List<InputDefinition> oldInputs, Component newComponent, Map<String, List<PropertyDataDefinition>> properties) {
        Map<String, List<PropertyDataDefinition>> getInputProperties = resolveGetInputProperties(properties);
        List<RedeclareInputData> inputsToRedeclareData = buildRedeclareInputData(newComponent, getInputProperties);
        return findPrevDeclaredInputs(oldInputs, inputsToRedeclareData);
    }

    private List<RedeclareInputData> buildRedeclareInputData(Component newComponent, Map<String, List<PropertyDataDefinition>> getInputProperties) {
        Map<String, InputDefinition> inputsById = MapUtil.toMap(newComponent.getInputs(), InputDefinition::getUniqueId);
        List<RedeclareInputData> redeclareInputData = new ArrayList<>();
        getInputProperties.forEach((instanceId, getInputProps) -> redeclareInputData.addAll(findInputsToRedeclare(inputsById, instanceId, getInputProps)));
        return redeclareInputData;

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

    private List<RedeclareInputData> findInputsToRedeclare(Map<String, InputDefinition> inputsById, String instanceId, List<PropertyDataDefinition> getInputProps) {
        List<RedeclareInputData> redeclareInputDataList = new ArrayList<>();
        getInputProps.forEach(property -> {
            List<String> inputsToRedeclareIds = findInputsToRedeclareIds(inputsById, property);
            RedeclareInputData redeclareInputData = new RedeclareInputData(property.getUniqueId(), inputsToRedeclareIds, instanceId, property.getDefaultValue());
            redeclareInputDataList.add(redeclareInputData);
        });
        return redeclareInputDataList;
    }

    private List<InputDefinition> prepareInputsForRedeclaration(Map<String, InputDefinition> oldInputsById, RedeclareInputData redeclareInputData) {
        List<InputDefinition> inputsForRedeclaration = redeclareInputData.declaredInputIds.stream()
                                            .filter(oldInputsById::containsKey)
                                            .map(oldInputsById::get)
                                            .map(InputDefinition::new)
                                            .collect(Collectors.toList());
        
        inputsForRedeclaration.forEach(input -> {
            input.setPropertyId(redeclareInputData.propertyId);
            input.setInstanceUniqueId(redeclareInputData.propertyOwnerId);
            
            if(!Strings.isNullOrEmpty(redeclareInputData.value)) {
                input.setValue(redeclareInputData.value);
                input.setDefaultValue(redeclareInputData.value);
            }
        });
        return inputsForRedeclaration;
    }

    private List<String> findInputsToRedeclareIds(Map<String, InputDefinition> inputsById, PropertyDataDefinition property) {
        List<GetInputValueDataDefinition> getInputValues = property.getGetInputValues();
        return getInputValues.stream()
                .filter(getInputVal -> isGetInputValueHasNoCorrespondingInput(getInputVal, inputsById))
                .map(GetInputValueDataDefinition::getInputId)
                .collect(Collectors.toList());
    }

    private boolean isGetInputValueHasNoCorrespondingInput(GetInputValueDataDefinition getInputVal, Map<String, InputDefinition> inputsById) {
        return !inputsById.containsKey(getInputVal.getInputId());
    }

    private class RedeclareInputData {
        private String propertyId;
        private List<String> declaredInputIds;
        private String propertyOwnerId;
        private String value;

        RedeclareInputData(String propertyId, List<String> declaredInputIds, String propertyOwnerId, String value) {
            this.propertyId = propertyId;
            this.declaredInputIds = declaredInputIds;
            this.propertyOwnerId = propertyOwnerId;
            this.value = value;
        }

    }
}
