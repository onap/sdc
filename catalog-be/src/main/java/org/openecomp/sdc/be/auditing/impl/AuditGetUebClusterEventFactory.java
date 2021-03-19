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
package org.openecomp.sdc.be.auditing.impl;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGetUebClusterEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditGetUebClusterEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" CONSUMER_ID = \"%s\" STATUS_TIME = \"%s\" STATUS = \"%s\" STATUS_DESC = \"%s\"";
    private final AuditingGetUebClusterEvent event;

    public AuditGetUebClusterEventFactory(CommonAuditData commonFields, String consumerId) {
        super(AuditingActionEnum.GET_UEB_CLUSTER);
        event = new AuditingGetUebClusterEvent(getAction().getName(), commonFields, consumerId);
    }

    public AuditGetUebClusterEventFactory(CommonAuditData commonFields, String consumerId, String timestamp) {
        this(commonFields, consumerId);
        this.event.setTimestamp1(timestamp);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[]{event.getAction(), event.getConsumerId(), event.getTimestamp(), event.getStatus(), event.getDesc()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }
}
