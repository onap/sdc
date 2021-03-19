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
package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public abstract class AuditResourceAdminEventFactory extends AuditBaseEventFactory {

    protected final ResourceAdminEvent event;

    AuditResourceAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                   ResourceVersionInfo prevParams, ResourceVersionInfo currParams, String invariantUuid, User modifier,
                                   String artifactData, String comment, String did, String toscaNodeType) {
        this(action, commonFields, resourceCommonInfo, prevParams, currParams, invariantUuid, AuditBaseEventFactory.buildUserName(modifier),
            artifactData, AuditBaseEventFactory.replaceNullNameWithEmpty(comment), did, toscaNodeType);
    }

    AuditResourceAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                   ResourceVersionInfo prevParams, ResourceVersionInfo currParams, String invariantUuid, String modifier,
                                   String artifactData, String comment, String did, String toscaNodeType, String timestamp) {
        this(action, commonFields, resourceCommonInfo, prevParams, currParams, invariantUuid, modifier, artifactData, comment, did, toscaNodeType);
        this.event.setTimestamp1(timestamp);
    }

    private AuditResourceAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                           ResourceVersionInfo prevParams, ResourceVersionInfo currParams, String invariantUuid, String modifier,
                                           String artifactData, String comment, String did, String toscaNodeType) {
        super(action);
        this.event = new ResourceAdminEvent(getAction().getName(), commonFields, resourceCommonInfo, prevParams, currParams, invariantUuid, modifier,
            artifactData, comment, did, toscaNodeType);
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }
}
