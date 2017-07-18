package org.openecomp.sdc.health.data;


public enum SiteMode {
    Active("active"), NOT_ACTIVE("NotActive");

    private String name;

    SiteMode(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }

    public static final SiteMode toValue(String inVal) {
        for (SiteMode val : values()) {
            if (val.toString().equals(inVal)) {
                return val;
            }
        }
        return null;
    }
}
