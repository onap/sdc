package org.openecomp.sdc.health.data;

import java.util.Collection;

/**
 * {
 "sdcVersion": "1702.0.83.37.1",

 "componentsInfo": [
 {
 "healthCheckComponent": “<component name>",
 "healthCheckStatus": “<UP / DOWN>",
 "version": “<component version>",
 "description": “<OK or error description>"
 },
 …..
 ]
 }

 */
public class HealthCheckResult {
    String sdcVersion;
    Collection<HealthInfo>  componentsInfo;

    public HealthCheckResult() {
    }

    public String getSdcVersion() {
        return sdcVersion;
    }

    public void setSdcVersion(String sdcVersion) {
        this.sdcVersion = sdcVersion;
    }

    public Collection<HealthInfo> getComponentsInfo() {
        return componentsInfo;
    }

    public void setComponentsInfo(Collection<HealthInfo> componentsInfo) {
        this.componentsInfo = componentsInfo;
    }


}
