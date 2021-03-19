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

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionEngineEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;

public abstract class AuditDistributionEngineEventFactory extends AuditBaseEventFactory {

    protected final DistributionEngineEvent event;

    AuditDistributionEngineEventFactory(AuditingActionEnum action, CommonAuditData commonFields, DistributionTopicData distributionTopicData,
                                        String consumerId, String apiKey, String envName, String role) {
        super(action);
        event = new DistributionEngineEvent(getAction().getName(), commonFields, consumerId, distributionTopicData, apiKey, envName, role);
    }

    AuditDistributionEngineEventFactory(AuditingActionEnum action, CommonAuditData commonFields, DistributionTopicData distributionTopicData,
                                        String consumerId, String apiKey, String envName, String role, String timestamp) {
        this(action, commonFields, distributionTopicData, consumerId, apiKey, envName, role);
        this.event.setTimestamp1(timestamp);
    }

    protected String getTopicName() {
        return event.getDnotifTopic() != null ? event.getDnotifTopic() : event.getDstatusTopic();
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }
}
