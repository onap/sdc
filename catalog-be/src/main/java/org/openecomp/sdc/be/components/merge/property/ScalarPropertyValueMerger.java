package org.openecomp.sdc.be.components.merge.property;

import java.util.List;

import org.springframework.stereotype.Component;

@Component("scalar-prop-value-merger")
public class ScalarPropertyValueMerger extends PropertyValueMerger {

    private final static ScalarPropertyValueMerger INSTANCE = new ScalarPropertyValueMerger();

    public static PropertyValueMerger getInstance() {
        return INSTANCE;
    }

    @Override
    Object merge(Object oldVal, Object newVal, List<String> getInputNamesToMerge) {
        return mergeScalarValue(removeUnwantedGetInputValues(oldVal, getInputNamesToMerge), newVal);
    }
}
