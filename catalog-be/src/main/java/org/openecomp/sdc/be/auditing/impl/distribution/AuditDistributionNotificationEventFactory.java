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
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.OperationalEnvAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public class AuditDistributionNotificationEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" "
        + "SERVICE_INSTANCE_ID = \"%s\" CURR_VERSION = \"%s\" MODIFIER = \"%s\" CURR_STATE = \"%s\" DID = \"%s\" "
        + "TOPIC_NAME = \"%s\" STATUS = \"%s\" DESC = \"%s\" TENANT = \"%s\" VNF_WORKLOAD_CONTEXT = \"%s\" ENV_ID = \"%s\"";
    private final DistributionNotificationEvent event;

    public AuditDistributionNotificationEventFactory(CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo,
                                                     ResourceVersionInfo resourceVersionInfo, String did, User modifier, String topicName,
                                                     OperationalEnvAuditData opEnvFields) {
        this(commonAuditData, resourceCommonInfo, resourceVersionInfo, did, AuditBaseEventFactory.buildUserName(modifier), topicName, opEnvFields);
    }

    public AuditDistributionNotificationEventFactory(CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo,
                                                     ResourceVersionInfo resourceVersionInfo, String did, String modifier, String topicName,
                                                     OperationalEnvAuditData opEnvFields, String timestamp) {
        this(commonAuditData, resourceCommonInfo, resourceVersionInfo, did, modifier, topicName, opEnvFields);
        this.event.setTimestamp1(timestamp);
    }

    private AuditDistributionNotificationEventFactory(CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo,
                                                      ResourceVersionInfo resourceVersionInfo, String did, String modifier, String topicName,
                                                      OperationalEnvAuditData opEnvFields) {
        super(AuditingActionEnum.DISTRIBUTION_NOTIFY);
        this.event = new DistributionNotificationEvent(getAction().getName(), commonAuditData, resourceCommonInfo, resourceVersionInfo, did, modifier,
            topicName, opEnvFields);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[]{event.getAction(), event.getResourceName(), event.getResourceType(), event.getServiceInstanceId(), event.getCurrVersion(),
            event.getModifier(), event.getCurrState(), event.getDid(), event.getTopicName(), event.getStatus(), event.getDesc(), event.getTenant(),
            event.getVnfWorkloadContext(), event.getEnvId()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }
}
