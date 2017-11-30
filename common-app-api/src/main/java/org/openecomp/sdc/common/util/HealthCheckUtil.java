package org.openecomp.sdc.common.util;

import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HealthCheckUtil {

    private static Logger log = LoggerFactory.getLogger(HealthCheckUtil.class.getName());

    public static boolean getAggregateStatus(List<HealthCheckInfo> healthCheckInfos) {

        boolean status = true;

        for (HealthCheckInfo healthCheckInfo : healthCheckInfos) {
            if (healthCheckInfo.getHealthCheckStatus().equals(HealthCheckInfo.HealthCheckStatus.DOWN)) {
                log.debug("Component {} is reported as DOWN - Aggregated HC will be DOWN", healthCheckInfo.getHealthCheckComponent());
                status = false;
                break;
            }
        }

        return status;
    }

    public static String getAggregateDescription(List<HealthCheckInfo> healthCheckInfos, String parentDescription) {

        StringBuilder sb = new StringBuilder();
        healthCheckInfos.forEach(x -> {
            if (x.getHealthCheckStatus() == HealthCheckInfo.HealthCheckStatus.DOWN) {
                sb.append("Component ").append(x.getHealthCheckComponent()).append(" is Down, ");
            }
        });

        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";

//        return description;
    }

}
