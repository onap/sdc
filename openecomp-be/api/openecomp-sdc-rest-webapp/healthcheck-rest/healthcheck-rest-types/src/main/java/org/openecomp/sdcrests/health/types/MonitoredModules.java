package org.openecomp.sdcrests.health.types;


public enum MonitoredModules {
    BE("BE"),
    CAS("Cassandra"),
    ZU("Zusammen");

    private String name;

    MonitoredModules(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return name;
    }

    public static final MonitoredModules toValue(String inVal){
        for (MonitoredModules val : values()){
            if (val.toString().equals(inVal)){
                return val;
            }
        }
        return null;
    }
}
