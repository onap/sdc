package org.openecomp.sdc.be.model.tosca;

/**
 * tosca functions supported by sdc
 */
public enum ToscaFunctions {

    GET_INPUT("get_input"),
    GET_PROPERTY("get_property");

    private String functionName;

    ToscaFunctions(String functionName) {
        this.functionName = functionName;
    }

    public String getFunctionName() {
        return functionName;
    }
}
