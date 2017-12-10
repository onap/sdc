package org.openecomp.sdc.be.components.merge.property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.tosca.ToscaFunctions;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.tosca.PropertyConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import fj.data.Either;

@Component
public class PropertyDataValueMergeBusinessLogic {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyDataValueMergeBusinessLogic.class);

    private final PropertyConvertor propertyConvertor = PropertyConvertor.getInstance();

    private PropertyValueMerger complexPropertyValueMerger = ComplexPropertyValueMerger.getInstance();

    private PropertyValueMerger scalarPropertyValueMerger = ScalarPropertyValueMerger.getInstance();

    @Resource
    private ApplicationDataTypeCache dataTypeCache;

    private final Gson gson = new Gson();

    /**
     *
     * @param oldProp the old property to merge value from
     * @param newProp the new property to merge value into
     * @param getInputNamesToMerge inputs names which their corresponding get_input values are allowed to be merged
     */
    public void mergePropertyValue(PropertyDataDefinition oldProp, PropertyDataDefinition newProp, List<String> getInputNamesToMerge) {
        Either<Map<String, DataTypeDefinition>, TitanOperationStatus> dataTypesEither = dataTypeCache.getAll();
        if (dataTypesEither.isRight()) {
            LOGGER.debug("failed to fetch data types, skip merging of previous property values. status: {}", dataTypesEither.right().value());
        }
        mergePropertyValue(oldProp, newProp, dataTypesEither.left().value(), getInputNamesToMerge);
        mergeComplexPropertyGetInputsValues(oldProp, newProp);
    }

    private void mergePropertyValue(PropertyDataDefinition oldProp, PropertyDataDefinition newProp, Map<String, DataTypeDefinition> dataTypes, List<String> getInputNamesToMerge) {
        Object oldValAsObject = convertPropertyStrValueToObject(oldProp, dataTypes);
        Object newValAsObject = convertPropertyStrValueToObject(newProp, dataTypes);
        PropertyValueMerger propertyValueMerger = getPropertyValueMerger(newProp);
        if(oldValAsObject != null){
            Object mergedValue = propertyValueMerger.mergeValues(oldValAsObject, newValAsObject, getInputNamesToMerge);
            newProp.setValue(convertPropertyValueObjectToString(mergedValue));
        }
    }

    private PropertyValueMerger getPropertyValueMerger(PropertyDataDefinition newProp) {
        if (ToscaPropertyType.isPrimitiveType(newProp.getType()) || ToscaPropertyType.isPrimitiveType(newProp.getSchemaType())) {
            return scalarPropertyValueMerger;
        }
        return complexPropertyValueMerger;
    }

    private String convertPropertyValueObjectToString(Object mergedValue) {
        if (isEmptyValue(mergedValue)) {
            return null;
        }
        return mergedValue instanceof String? mergedValue.toString() : gson.toJson(mergedValue);
    }

    private Object convertPropertyStrValueToObject(PropertyDataDefinition propertyDataDefinition, Map<String, DataTypeDefinition> dataTypes) {
            String propValue = propertyDataDefinition.getValue() == null ? "": propertyDataDefinition.getValue();
            String propertyType = propertyDataDefinition.getType();
            String innerType = propertyDataDefinition.getSchemaType();
            return propertyConvertor.convertToToscaObject(propertyType, propValue, innerType, dataTypes);
    }


    @SuppressWarnings("unchecked")
    private Object removeUnwantedGetInputValues(Object val, List<String> getInputNamesToMerge) {
        if (val instanceof  Map) {
            return removeUnwantedGetInputValues((Map<String, Object>) val, getInputNamesToMerge);
        }
        if (val instanceof List) {
            return removeUnwantedGetInputValues((List<Object>)val, getInputNamesToMerge);
        }
        return val;
    }

    private List<Object> removeUnwantedGetInputValues(List<Object> listVal, List<String> getInputNamesToMerge) {
        return listVal.stream().map(val -> removeUnwantedGetInputValues(val, getInputNamesToMerge)).collect(Collectors.toList());
    }

    private Map<String, Object> removeUnwantedGetInputValues(Map<String, Object> val, List<String> getInputNamesToMerge) {
        return val.entrySet().stream().filter(entry -> !isGetInputEntry(entry) || isGetInputToMerge(getInputNamesToMerge, entry))
                               .collect(Collectors.toMap(Map.Entry::getKey, entry -> removeUnwantedGetInputValues(entry.getValue(), getInputNamesToMerge)));
    }

    private boolean isGetInputToMerge(List<String> getInputNamesToMerge, Map.Entry<String, Object> entry) {
        return getInputNamesToMerge.contains(retrieveGetInputInputName(entry.getValue()));
    }

    private String retrieveGetInputInputName(Object getInputValue) {
        return getInputValue instanceof List ? (String)((List) getInputValue).get(0) : (String)getInputValue;
    }

    private boolean isGetInputEntry(Map.Entry<String, Object> oldValEntry) {
        return oldValEntry.getKey().equals(ImportUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
    }

    private boolean isEmptyValue(Object val) {
        return val == null ||
               val instanceof Map && ((Map) val).isEmpty() ||
               val instanceof List && ((List) val).isEmpty();
    }

    private void mergeComplexPropertyGetInputsValues(PropertyDataDefinition oldProp, PropertyDataDefinition newProp) {
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
