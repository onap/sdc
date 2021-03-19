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

import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

public class AuditDistributionEngineEventFactoryManager {

    public static AuditEventFactory createDistributionEngineEventFactory(AuditingActionEnum action, String environmentName,
                                                                         DistributionTopicData distributionTopicData, String role, String apiKey,
                                                                         String status) {
        AuditEventFactory factory;
        switch (action) {
            case CREATE_DISTRIBUTION_TOPIC:
                factory = new AuditCreateTopicDistributionEngineEventFactory(
                    CommonAuditData.newBuilder().status(status).requestId(ThreadLocalsHolder.getUuid()).build(), distributionTopicData, apiKey,
                    environmentName, role);
                break;
            case ADD_KEY_TO_TOPIC_ACL:
            case REMOVE_KEY_FROM_TOPIC_ACL:
                factory = new AuditAddRemoveKeyDistributionEngineEventFactory(action,
                    CommonAuditData.newBuilder().status(status).requestId(ThreadLocalsHolder.getUuid()).build(), distributionTopicData, apiKey,
                    environmentName, role);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return factory;
    }
}
