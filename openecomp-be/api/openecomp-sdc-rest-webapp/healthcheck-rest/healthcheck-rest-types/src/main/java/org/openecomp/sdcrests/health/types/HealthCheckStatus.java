package org.openecomp.sdcrests.health.types;


public enum HealthCheckStatus {
    UP("UP"),
    DOWN("DOWN");

    private String name;

    HealthCheckStatus(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }

    public static final HealthCheckStatus toValue(String inVal){
        for (HealthCheckStatus val : values()){
            if (val.toString().equals(inVal)){
                return val;
            }
        }
        return null;
    }
}
