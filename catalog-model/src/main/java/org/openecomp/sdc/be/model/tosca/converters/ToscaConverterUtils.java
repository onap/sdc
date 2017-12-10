package org.openecomp.sdc.be.model.tosca.converters;

import org.openecomp.sdc.be.model.tosca.ToscaFunctions;

import com.google.gson.JsonObject;

public class ToscaConverterUtils {

    public static boolean isGetInputValue(JsonObject value) {
        return value.get(ToscaFunctions.GET_INPUT.getFunctionName()) != null;
    }

}
