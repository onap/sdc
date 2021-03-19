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
import org.openecomp.sdc.be.resources.data.auditing.EcompOperationalEnvironmentEvent;

public class AuditEcompOpEnvEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" OPERATIONAL_ENVIRONMENT_ACTION = \"%s\" OPERATIONAL_ENVIRONMENT_ID = \"%s\""
        + " OPERATIONAL_ENVIRONMENT_NAME = \"%s\" OPERATIONAL_ENVIRONMENT_TYPE = \"%s\" TENANT_CONTEXT = \"%s\"";
    private final EcompOperationalEnvironmentEvent event;

    public AuditEcompOpEnvEventFactory(AuditingActionEnum action, String operationalEnvironmentId, String operationalEnvironmentName,
                                       String operationalEnvironmentType, String operationalEnvironmentAction, String tenantContext) {
        super(action);
        event = new EcompOperationalEnvironmentEvent(getAction().getName(), operationalEnvironmentId, operationalEnvironmentName,
            operationalEnvironmentType, operationalEnvironmentAction, tenantContext);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[]{event.getAction(), event.getOperationalEnvironmentAction(), event.getOperationalEnvironmentId(),
            event.getOperationalEnvironmentName(), event.getOperationalEnvironmentType(), event.getTenantContext()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }
}
