package org.openecomp.sdc.be.model.tosca.converters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openecomp.sdc.be.model.tosca.ToscaFunctions;

public class ToscaConverterUtils {

    public static boolean isGetInputValue(JsonObject value) {
        return value.get(ToscaFunctions.GET_INPUT.getFunctionName()) != null;
    }

}
