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

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public class AuditImportResourceAdminEventFactory extends AuditCreateUpdateResourceAdminEventFactory {

    private static final String LOG_STR_TOSCA = LOG_STR + " TOSCA_NODE_TYPE = \"%s\"";

    public AuditImportResourceAdminEventFactory(CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo,
                                                ResourceVersionInfo prevParams, ResourceVersionInfo currParams, String invariantUuid, User modifier,
                                                String artifactData, String comment, String did, String toscaNodeType) {
        super(AuditingActionEnum.IMPORT_RESOURCE, commonAuditData, resourceCommonInfo, prevParams, currParams, invariantUuid, modifier, artifactData,
            comment, did, toscaNodeType);
    }

    public AuditImportResourceAdminEventFactory(CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo,
                                                ResourceVersionInfo prevParams, ResourceVersionInfo currParams, String invariantUuid, User modifier,
                                                String toscaNodeType) {
        super(AuditingActionEnum.IMPORT_RESOURCE, commonAuditData, resourceCommonInfo, prevParams, currParams, invariantUuid, modifier, null, null,
            null, toscaNodeType);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR_TOSCA;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[]{event.getAction(), event.getResourceName(), event.getResourceType(), event.getServiceInstanceId(),
            event.getInvariantUUID(), event.getPrevVersion(), event.getCurrVersion(), event.getModifier(), event.getPrevState(), event.getCurrState(),
            event.getStatus(), event.getDesc(), event.getToscaNodeType()};
    }
}
