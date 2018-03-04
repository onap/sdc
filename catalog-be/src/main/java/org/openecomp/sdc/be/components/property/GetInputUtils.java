package org.openecomp.sdc.be.components.property;

import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;

public class GetInputUtils {

    private GetInputUtils() {
    }

    public static boolean isGetInputValueForInput(GetInputValueDataDefinition inputData, String inputId) {
        return inputData.getInputId().equals(inputId) || (inputData.getGetInputIndex() != null && inputData.getGetInputIndex().getInputId().equals(inputId));
    }
}
