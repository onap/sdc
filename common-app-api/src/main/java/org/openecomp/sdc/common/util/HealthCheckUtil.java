/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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

    public String getAggregateDescription(List<HealthCheckInfo> healthCheckInfos) {

        StringBuilder sb = new StringBuilder();
        healthCheckInfos.forEach(x -> {
            if (x.getHealthCheckStatus() == DOWN) {
                sb.append("Component ").append(x.getHealthCheckComponent()).append(" is Down, ");
            }
        });

        return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
    }

}
