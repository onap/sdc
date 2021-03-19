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
package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;

public class AuditDistributionEngineEventMigrationFactory extends AuditDistributionEngineEventFactory {

    public AuditDistributionEngineEventMigrationFactory(AuditingActionEnum action, CommonAuditData commonFields,
                                                        DistributionTopicData distributionTopicData, String consumerId, String apiKey, String envName,
                                                        String role, String timestamp) {
        super(action, commonFields, distributionTopicData, consumerId, apiKey, envName, role, timestamp);
    }

    @Override
    public String getLogPattern() {
        return "";
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[0];
    }
}
