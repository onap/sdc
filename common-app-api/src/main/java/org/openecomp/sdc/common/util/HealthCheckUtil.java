package org.openecomp.sdc.common.util;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.common.api.HealthCheckInfo;
import org.openecomp.sdc.common.log.wrappers.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus.DOWN;

public class HealthCheckUtil {

    private static Logger log = Logger.getLogger(HealthCheckUtil.class.getName());
    public boolean getAggregateStatus(List<HealthCheckInfo> healthCheckInfos, Collection<String> excludes) {
        boolean status = true;
        excludes = CollectionUtils.isEmpty(excludes) ? new ArrayList<>() : excludes;
        for (HealthCheckInfo healthCheckInfo : healthCheckInfos) {
            if (!excludes.contains(healthCheckInfo.getHealthCheckComponent()) && healthCheckInfo.getHealthCheckStatus().equals(DOWN)) {
                log.debug("Component {} is reported as DOWN - Aggregated HC will be DOWN", healthCheckInfo.getHealthCheckComponent());
                status = false;
                break;
            }
        }

        return status;
    }

    public String getAggregateDescription(List<HealthCheckInfo> healthCheckInfos, String parentDescription) {

        StringBuilder sb = new StringBuilder();
        healthCheckInfos.forEach(x -> {
            if (x.getHealthCheckStatus() == DOWN) {
                sb.append("Component ").append(x.getHealthCheckComponent()).append(" is Down, ");
            }
        });

        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
    }

}
