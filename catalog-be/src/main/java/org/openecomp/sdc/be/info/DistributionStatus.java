/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.info;

import java.util.Arrays;
import java.util.Optional;
import org.openecomp.sdc.common.log.wrappers.Logger;

public enum DistributionStatus {
    DEPLOYED("Deployed", "DEPLOYED");

    private String name;
    private String auditingStatus;

    private static final Logger log = Logger.getLogger(DistributionStatus.class);

    DistributionStatus(String name, String auditingStatus) {
        this.name = name;
        this.auditingStatus = auditingStatus;
    }

    public String getName() {
        return name;
    }

    public String getAuditingStatus() {
        return auditingStatus;
    }

    public static DistributionStatus getStatusByAuditingStatusName(String auditingStatus) {
        Optional<DistributionStatus> distributionStatus = Arrays.stream(values())
            .filter(value -> value.getAuditingStatus().equals(auditingStatus)).findAny();
        if (!distributionStatus.isPresent()){
            log.debug("No DistributionStatus is mapped to name {}", auditingStatus);
        }
        // it should be replaced by some exception handling. Keeping it only for the purpose of backward compatibility
        return distributionStatus.orElse(null);
    }

}
