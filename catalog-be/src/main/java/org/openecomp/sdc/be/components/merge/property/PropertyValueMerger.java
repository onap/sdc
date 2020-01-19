/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 */

package org.openecomp.sdc.be.components.merge.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.springframework.stereotype.Component;

@Component
public class PropertyValueMerger {
    
    @SuppressWarnings("unchecked")
    /**
     * merges property value oldVal into property value newVal recursively
     * @param oldVal - cannot be {@code Null}
     */
    protected Object merge(Object oldVal, Object newVal, List<String> inputNamesToMerge, String type, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        if (isEmptyValue(newVal)) {
            return removeUnwantedGetInputValues(oldVal, inputNamesToMerge);
        }
        if (isMapTypeValues(oldVal, newVal)) {
            return mergeMapValue((Map<String, Object>) oldVal, (Map<String, Object>) newVal, inputNamesToMerge, type, innerType, dataTypes);
        }
        if (isListTypeValues(oldVal, newVal)) {
            return mergeListValue((List<Object>) oldVal, (List<Object>) newVal, inputNamesToMerge, innerType, dataTypes);
        }
        if (isSameTypeValues(oldVal, newVal)) {
            return mergeScalarValue(oldVal, newVal);
        }
        return newVal;
    }
    
    private Map<String, Object> mergeMapValue(Map<String, Object> oldValMap, Map<String, Object> newValMap, List<String> inputNamesToMerge, String type, String innertType, Map<String, DataTypeDefinition> dataTypes) {
        mergeEntriesExistInOldValue(oldValMap, newValMap, inputNamesToMerge, type, innertType, dataTypes);//continue the recursion
        if (type != null && !type.equals("map") && !type.equals("json")) {
            setOldEntriesNotExistInNewValue(oldValMap, newValMap, inputNamesToMerge);
        }
        
        return newValMap;
    }

    private void mergeEntriesExistInOldValue(Map<String, Object> oldValMap, Map<String, Object> newValMap, List<String> inputNamesToMerge, String type, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        for (Map.Entry<String, Object> newValEntry : newValMap.entrySet()) {
            Object oldVal = oldValMap.get(newValEntry.getKey());
            if (oldVal != null) {
                ImmutablePair<String, String> types = getTypeAndInnerTypePair(newValEntry.getKey(), type, innerType, dataTypes);
                newValMap.put(newValEntry.getKey(), merge(oldVal, newValEntry.getValue(), inputNamesToMerge, types.getLeft(), types.getRight(), dataTypes));
            }
        }
    }
    
    private void setOldEntriesNotExistInNewValue(Map<String, Object> oldVal, Map<String, Object> newVal, List<String> getInputNamesToMerge) {
        for (Map.Entry<String, Object> oldValEntry : oldVal.entrySet()) {
            if (!isInputEntry(oldValEntry) || isInputToMerge(getInputNamesToMerge, oldValEntry)) {
                Object oldValObj = oldValEntry.getValue();
                newVal.computeIfAbsent(oldValEntry.getKey(), key -> removeUnwantedGetInputValues(oldValObj, getInputNamesToMerge));
            }
        }
    }
    
    private ImmutablePair<String, String> getTypeAndInnerTypePair(String propName, String type, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        if (type == null || (ToscaPropertyType.isScalarType(type) && ToscaPropertyType.isScalarType(innerType))) {
            return ImmutablePair.of(innerType, null);
        }
        
        String newInnerType = null;
        DataTypeDefinition innerTypeDef = dataTypes.get(type);
        if (innerTypeDef != null) {
            List<PropertyDefinition> properties = innerTypeDef.getProperties();
            if (properties!= null) {
                Optional<PropertyDefinition> optionalProperty = findProperty(properties, propName);
                
                innerType = optionalProperty.map(PropertyDefinition::getType)
                                            .orElse(innerType);
                
                newInnerType = optionalProperty.map(PropertyDefinition::getSchemaType)
                                               .orElse(null);
            }
        }
                
        return ImmutablePair.of(innerType, newInnerType);
    }
    
    private Optional<PropertyDefinition> findProperty(List<PropertyDefinition> properties, String propName) {
        return properties.stream()
                .filter(p -> propName.equals(p.getName()))
                .findFirst();
    }

    private List<Object> mergeListValue(List<Object> oldVal, List<Object> newVal, List<String> inputNamesToMerge, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        List<Object> mergedList = newVal;
    
        if (oldVal.size() == newVal.size()) {
            mergedList = mergeLists(oldVal, newVal, inputNamesToMerge, innerType, dataTypes);
        }

        return mergedList;
    }
    

    private List<Object> mergeLists(List<Object> oldVal, List<Object> newVal, List<String> inputNamesToMerge, String innerType, Map<String, DataTypeDefinition> dataTypes) {
        int minListSize = Math.min(oldVal.size(), newVal.size());
        List<Object> mergedList = new ArrayList<>();
        for (int i = 0; i < minListSize; i++) {
            Object mergedVal = merge(oldVal.get(i), newVal.get(i), inputNamesToMerge, innerType, null, dataTypes);
            mergedList.add(mergedVal);
        }
        return mergedList;
    }
    
    
    private Object mergeScalarValue(Object oldVal, Object newVal) {
        return isEmptyValue(newVal) ? oldVal : newVal;
    }
    
    static boolean isEmptyValue(Object val) {
        return val == null ||
               val instanceof String && StringUtils.isEmpty((String)val) ||
               val instanceof Map && ((Map<?,?>) val).isEmpty() ||
               val instanceof List && ((List<?>) val).isEmpty();

    }
    
    @SuppressWarnings("unchecked")
    Object removeUnwantedGetInputValues(Object val, List<String> inputNamesToMerge) {
        if (val instanceof  Map) {
            return removeUnwantedGetInputValues((Map<String, Object>) val, inputNamesToMerge);
        }
        if (val instanceof List) {
            return removeUnwantedGetInputValues((List<Object>)val, inputNamesToMerge);
        }
        return val;
    }

    private List<Object> removeUnwantedGetInputValues(List<Object> listVal, List<String> inputNamesToMerge) {
        return listVal.stream().map(val -> removeUnwantedGetInputValues(val, inputNamesToMerge)).collect(Collectors.toList());
    }

    private Map<String, Object> removeUnwantedGetInputValues(Map<String, Object> val, List<String> inputNamesToMerge) {
        return val.entrySet().stream().filter(entry -> !isInputEntry(entry) || isInputToMerge(inputNamesToMerge, entry))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> removeUnwantedGetInputValues(entry.getValue(), inputNamesToMerge)));
    }

    private boolean isInputToMerge(List<String> inputNamesToMerge, Map.Entry<String, Object> entry) {
        return inputNamesToMerge.contains(retrieveInputName(entry.getValue()));
    }

    private boolean isMapTypeValues(Object oldVal, Object newVal) {
        return newVal instanceof Map && oldVal instanceof Map;
    }

    private boolean isListTypeValues(Object oldVal, Object newVal) {
        return newVal instanceof List && oldVal instanceof List;
    }

    private boolean isSameTypeValues(Object oldVal, Object newVal) {
        return oldVal.getClass().equals(newVal.getClass());
    }

    private String retrieveInputName(Object inputValue) {
        return inputValue instanceof List ? (String)((List<?>) inputValue).get(0) : (String)inputValue;
    }

    protected boolean isInputEntry(Map.Entry<String, Object> oldValEntry) {
        return oldValEntry.getKey().equals(TypeUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
    }
    
    
}
