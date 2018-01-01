package org.openecomp.sdc.be.datatypes.enums;

public enum EnvironmentStatusEnum {
    UNKNOWN("unknown"),
    IN_PROGRESS("in_progress"),
    FAILED("failed"),
    COMPLETED("completed");

    private final String name;

    private EnvironmentStatusEnum(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
    public static EnvironmentStatusEnum getByName(final String name){
        switch(name){
            case ("in_progress") : return IN_PROGRESS;
            case ("failed") : return FAILED;
            case ("completed") : return COMPLETED;
            default: return UNKNOWN;
        }
    }

}
