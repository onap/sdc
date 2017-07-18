package org.openecomp.sdc.health.data;

/**
 * {
   "sdcVersion": "<SERVER_FULL_VERSION>"
   "componentsInfo": [
     {
       "healthCheckComponent": "<COMPONENT_NAME>",
       "healthCheckStatus": "<UP_OR_DOWN>",
       "version": "<COMPONENT_VERSION>",
       "description": "<OK_OR_ERROR_VERSION>"
     }
   ]
 }
 */
public class HealthInfo {
    private MonitoredModules healthCheckComponent;
    private HealthCheckStatus healthCheckStatus;
    private String version;
    private String description;

    public HealthInfo() {
    }

    public HealthInfo(MonitoredModules healthCheckComponent, HealthCheckStatus healthCheckStatus, String version, String description) {
        this.healthCheckComponent = healthCheckComponent;
        this.healthCheckStatus = healthCheckStatus;
        this.version = version;
        this.description = description;
    }

    public MonitoredModules getHealthCheckComponent() {
        return healthCheckComponent;
    }

    public void setHealthCheckComponent(MonitoredModules healthCheckComponent) {
        this.healthCheckComponent = healthCheckComponent;
    }

    public HealthCheckStatus getHealthCheckStatus() {
        return healthCheckStatus;
    }

    public void setHealthCheckStatus(HealthCheckStatus healthCheckStatus) {
        this.healthCheckStatus = healthCheckStatus;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "HealthInfo{" +
                "healthCheckComponent='" + healthCheckComponent + '\'' +
                ", healthCheckStatus=" + healthCheckStatus +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
