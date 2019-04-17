package org.openecomp.sdc.ci.tests.datatypes.enums;

public enum XnfTypeEnum {

    VNF ("VNF"),
    PNF ("PNF");

    private String value;

    public String getValue() {
        return value;
    }

    private XnfTypeEnum(String value) {
        this.value = value;
    }
}
