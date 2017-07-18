package org.openecomp.sdcrests.health.types;


public class HealthInfoDto {
    private MonitoredModules healthCheckComponent;
    private HealthCheckStatus healthStatus;
    private String version;
    private String description;

    public HealthInfoDto() {
    }

    public HealthInfoDto(MonitoredModules healthCheckComponent, HealthCheckStatus healthStatus, String version, String description) {
        this.healthCheckComponent = healthCheckComponent;
        this.healthStatus = healthStatus;
        this.version = version;
        this.description = description;
    }

    public MonitoredModules getHealthCheckComponent() {
        return healthCheckComponent;
    }

    public void setHealthCheckComponent(MonitoredModules healthCheckComponent) {
        this.healthCheckComponent = healthCheckComponent;
    }

    public HealthCheckStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthCheckStatus healthStatus) {
        this.healthStatus = healthStatus;
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
                ", healthStatus=" + healthStatus +
                ", version='" + version + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
