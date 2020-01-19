package org.openecomp.sdc.be.datatypes.enums;

public enum PortMirroringEnum {
    CISCO_VENDOR_NAME("CISCO"),
    CISCO_VENDOR_MODEL_NUMBER("4500x");

    private String value;

    public String getValue() {
        return value;
    }

    PortMirroringEnum(String value) {
        this.value = value;
    }
}
