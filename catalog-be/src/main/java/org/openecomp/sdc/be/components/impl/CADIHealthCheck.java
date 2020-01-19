package org.openecomp.sdc.be.components.impl;

import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.stereotype.Component;

import static org.openecomp.sdc.common.api.Constants.HC_COMPONENT_CADI;
import static org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus.DOWN;

@Component
public class CADIHealthCheck {

    private static CADIHealthCheck cadiHealthCheckInstance = new CADIHealthCheck();;

    private static HealthCheckInfo.HealthCheckStatus isCADIUpOrDown = DOWN;

    private static final Logger log = Logger.getLogger(CADIHealthCheck.class.getName());

    public static CADIHealthCheck getCADIHealthCheckInstance() {
        return cadiHealthCheckInstance;
    }

    public void setIsCADIUp(HealthCheckInfo.HealthCheckStatus cadiStatus) {
        log.debug("Setting cadiHealthCheckInstance status to: {}", cadiStatus.toString());
        isCADIUpOrDown = cadiStatus;
    }

    public static HealthCheckInfo getCADIStatus() {
        log.debug("getCADIStatus: Checking whether CADI was up or down while its init.");
        String description = "OK";
        if (isCADIUpOrDown == DOWN){
            description = "CADI filter failed initialization";
        }
        return new HealthCheckInfo(HC_COMPONENT_CADI, isCADIUpOrDown, null,
                description);
    }

}
