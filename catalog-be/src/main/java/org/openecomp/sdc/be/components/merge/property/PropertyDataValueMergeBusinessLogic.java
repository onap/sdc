package org.openecomp.sdc.be.components.merge.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.tosca.ToscaFunctions;
import org.openecomp.sdc.be.tosca.PropertyConvertor;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import fj.data.Either;

@Component
public class PropertyDataValueMergeBusinessLogic {

    private static final Logger LOGGER = Logger.getLogger(PropertyDataValueMergeBusinessLogic.class);

    private final PropertyValueMerger propertyValueMerger;
    private final ApplicationDataTypeCache dataTypeCache;
    
    private final PropertyConvertor propertyConvertor = PropertyConvertor.getInstance();
    private final Gson gson = new Gson();

    
    public PropertyDataValueMergeBusinessLogic(PropertyValueMerger propertyValueMerger, ApplicationDataTypeCache dataTypeCache) {
        this.propertyValueMerger = propertyValueMerger;
        this.dataTypeCache = dataTypeCache;
    }

    /**
     *
     * @param oldProp the old property to merge value from
     * @param newProp the new property to merge value into
     */
    public void mergePropertyValue(PropertyDataDefinition oldProp, PropertyDataDefinition newProp, List<String> getInputNamesToMerge) {
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> dataTypesEither = dataTypeCache.getAll();
        if (dataTypesEither.isRight()) {
            LOGGER.debug("failed to fetch data types, skip merging of previous property values. status: {}", dataTypesEither.right().value());
        }
        else {
            mergePropertyValue(oldProp, newProp, dataTypesEither.left().value(), getInputNamesToMerge);
        }
    }
    
    private void mergePropertyValue(PropertyDataDefinition oldProp, PropertyDataDefinition newProp, Map<String, DataTypeDefinition> dataTypes, List<String> getInputNamesToMerge) {
        Object oldValAsObject = convertPropertyStrValueToObject(oldProp, dataTypes);
        Object newValAsObject = convertPropertyStrValueToObject(newProp, dataTypes);
        if(oldValAsObject != null){
            Object mergedValue =  propertyValueMerger.merge(oldValAsObject, newValAsObject, getInputNamesToMerge, newProp.getType(), newProp.getSchemaType(), dataTypes);
            newProp.setValue(convertPropertyValueObjectToString(mergedValue));
            
            mergePropertyGetInputsValues(oldProp, newProp);
        }
        
    }
    
    private String convertPropertyValueObjectToString(Object mergedValue) {
        if (PropertyValueMerger.isEmptyValue(mergedValue)) {
            return null;
        }
        return mergedValue instanceof String? mergedValue.toString() : gson.toJson(mergedValue);
    }

    private Object convertPropertyStrValueToObject(PropertyDataDefinition propertyDataDefinition, Map<String, DataTypeDefinition> dataTypes) {
        String propValue = propertyDataDefinition.getValue() == null ? "": propertyDataDefinition.getValue();
        String propertyType = propertyDataDefinition.getType();
        String innerType = propertyDataDefinition.getSchemaType();
        return propertyConvertor.convertToToscaObject(propertyType, propValue, innerType, dataTypes, true);
    }

    protected void mergePropertyGetInputsValues(PropertyDataDefinition oldProp, PropertyDataDefinition newProp) {
        if (!oldProp.isGetInputProperty()) {
            return;
        }
        List<GetInputValueDataDefinition> getInputsToMerge = findOldGetInputValuesToMerge(oldProp, newProp);
        List<GetInputValueDataDefinition> newPropGetInputValues = Optional.ofNullable(newProp.getGetInputValues()).orElse(new ArrayList<>());
        newPropGetInputValues.addAll(getInputsToMerge);
        newProp.setGetInputValues(newPropGetInputValues);
    }

    private List<GetInputValueDataDefinition> findOldGetInputValuesToMerge(PropertyDataDefinition oldProp, PropertyDataDefinition newProp) {
        List<GetInputValueDataDefinition> oldGetInputValues = oldProp.getGetInputValues();
        List<GetInputValueDataDefinition> newGetInputValues = Optional.ofNullable(newProp.getGetInputValues()).orElse(Collections.emptyList());
        List<String> newGetInputNames = newGetInputValues.stream().map(GetInputValueDataDefinition::getInputName).collect(Collectors.toList());
        return oldGetInputValues.stream()
                .filter(getInput -> !newGetInputNames.contains(getInput.getInputName()))
                .filter(getInput -> isValueContainsGetInput(getInput.getInputName(), newProp.getValue()))
                .collect(Collectors.toList());
    }

    private boolean isValueContainsGetInput(String inputName, String value) {
        String getInputEntry = "\"%s\":\"%s\"";
        return value != null && value.contains(String.format(getInputEntry, ToscaFunctions.GET_INPUT.getFunctionName(), inputName));
    }
}
