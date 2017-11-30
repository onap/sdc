package org.openecomp.sdc.be.components.merge.property;

import java.util.List;

public class ComplexPropertyValueMerger extends PropertyValueMerger {

    private static final ComplexPropertyValueMerger INSTANCE = new ComplexPropertyValueMerger();

    public static PropertyValueMerger getInstance() {
        return INSTANCE;
    }

    @Override
    Object merge(Object oldVal, Object newVal, List<String> someStrings) {
        return mergeValues(oldVal, newVal, someStrings);
    }
}
