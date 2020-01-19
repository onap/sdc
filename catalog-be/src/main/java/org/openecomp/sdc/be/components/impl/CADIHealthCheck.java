/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
