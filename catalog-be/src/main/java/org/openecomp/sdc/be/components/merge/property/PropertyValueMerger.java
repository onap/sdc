package org.openecomp.sdc.be.components.merge.property;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils;

public abstract class PropertyValueMerger {

    abstract Object merge(Object oldVal, Object newVal, List<String> someStrings);

    @SuppressWarnings("unchecked")
    /**
     * merges property value oldVal into property value newVal recursively
     * @param oldVal - cannot be {@code Null}
     */
    protected Object mergeValues(Object oldVal, Object newVal, List<String> getInputNamesToMerge) {
        if (isEmptyValue(newVal)) {
            return removeUnwantedGetInputValues(oldVal, getInputNamesToMerge);
        }
        if (isMapTypeValues(oldVal, newVal)) {
            return mergeMapValue((Map<String, Object>) oldVal, (Map<String, Object>) newVal, getInputNamesToMerge);
        }
        if (isListTypeValues(oldVal, newVal)) {
            return mergeListValue((List<Object>) oldVal, (List<Object>) newVal, getInputNamesToMerge);
        }
        if (isSameTypeValues(oldVal, newVal)) {
            return mergeScalarValue(oldVal, newVal);
        }
        return newVal;

    }

    private Map<String, Object> mergeMapValue(Map<String, Object> oldValMap, Map<String, Object> newValMap, List<String> getInputNamesToMerge) {
        mergeEntriesExistInNewValue(oldValMap, newValMap, getInputNamesToMerge);//continue the recursion
        setOldEntriesNotExistInNewValue(oldValMap, newValMap, getInputNamesToMerge);
        return newValMap;
    }

    private void mergeEntriesExistInNewValue(Map<String, Object> oldValMap, Map<String, Object> newValMap, List<String> getInputNamesToMerge) {
        for (Map.Entry<String, Object> newValEntry : newValMap.entrySet()) {
            Object oldVal = oldValMap.get(newValEntry.getKey());
            if (oldVal != null) {
                newValMap.put(newValEntry.getKey(), merge(oldVal, newValEntry.getValue(), getInputNamesToMerge));
            }
        }
    }

    private void setOldEntriesNotExistInNewValue(Map<String, Object> oldVal, Map<String, Object> newVal, List<String> getInputNamesToMerge) {
        for (Map.Entry<String, Object> oldValEntry : oldVal.entrySet()) {
            if (!isGetInputEntry(oldValEntry) || isGetInputToMerge(getInputNamesToMerge, oldValEntry)) {
                Object oldValObj = oldValEntry.getValue();
                newVal.computeIfAbsent(oldValEntry.getKey(), key -> removeUnwantedGetInputValues(oldValObj, getInputNamesToMerge));
            }
        }
    }

    private List<Object> mergeListValue(List<Object> oldVal, List<Object> newVal, List<String> getInputNamesToMerge) {
        List<Object> mergedList = mergeLists(oldVal, newVal, getInputNamesToMerge);
        copyRestOfBiggerList(oldVal, newVal, getInputNamesToMerge, mergedList);
        return mergedList;
    }

    private void copyRestOfBiggerList(List<Object> oldVal, List<Object> newVal, List<String> getInputNamesToMerge, List<Object> mergedList) {
        if (oldVal.size() == newVal.size()) {
            return;
        }
        int maxListSize = Math.max(oldVal.size(), newVal.size());
        List<Object> greaterList = newVal.size() == maxListSize ? newVal : oldVal;
        for (int i = mergedList.size(); i < maxListSize; i ++) {
            Object listVal = greaterList.get(i);
            Object listValToMerge = greaterList == oldVal ? removeUnwantedGetInputValues(listVal, getInputNamesToMerge) : listVal;
            mergedList.add(listValToMerge);
        }
    }

    private List<Object> mergeLists(List<Object> oldVal, List<Object> newVal, List<String> getInputNamesToMerge) {
        int minListSize = Math.min(oldVal.size(), newVal.size());
        List<Object> mergedList = new ArrayList<>();
        for (int i = 0; i < minListSize; i++) {
            Object mergedVal = merge(oldVal.get(i), newVal.get(i), getInputNamesToMerge);
            mergedList.add(mergedVal);
        }
        return mergedList;
    }

    Object mergeScalarValue(Object oldVal, Object newVal) {
        return isEmptyValue(newVal) ? oldVal : newVal;
    }

    @SuppressWarnings("unchecked")
    Object removeUnwantedGetInputValues(Object val, List<String> getInputNamesToMerge) {
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

    private boolean isMapTypeValues(Object oldVal, Object newVal) {
        return newVal instanceof Map && oldVal instanceof Map;
    }

    private boolean isListTypeValues(Object oldVal, Object newVal) {
        return newVal instanceof List && oldVal instanceof List;
    }

    private boolean isSameTypeValues(Object oldVal, Object newVal) {
        return oldVal.getClass().equals(newVal.getClass());
    }

    private String retrieveGetInputInputName(Object getInputValue) {
        return getInputValue instanceof List ? (String)((List) getInputValue).get(0) : (String)getInputValue;
    }

    private boolean isGetInputEntry(Map.Entry<String, Object> oldValEntry) {
        return oldValEntry.getKey().equals(ImportUtils.ToscaTagNamesEnum.GET_INPUT.getElementName());
    }

    private boolean isEmptyValue(Object val) {
        return val == null ||
               val instanceof String && StringUtils.isEmpty((String)val) ||
               val instanceof Map && ((Map) val).isEmpty() ||
               val instanceof List && ((List) val).isEmpty();


    }


}
