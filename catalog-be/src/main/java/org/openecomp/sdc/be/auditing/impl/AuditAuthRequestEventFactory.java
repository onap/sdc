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
import org.openecomp.sdc.be.resources.data.auditing.AuthEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditAuthRequestEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" URL = \"%s\" USER = \"%s\" AUTH_STATUS = \"%s\" REALM = \"%s\"";
    private final AuthEvent event;

    public AuditAuthRequestEventFactory(CommonAuditData commonFields, String authUser, String authUrl, String realm, String authStatus) {
        super(AuditingActionEnum.AUTH_REQUEST);
        event = new AuthEvent(getAction().getName(), commonFields, authUser, authUrl, realm, authStatus);
    }

    public AuditAuthRequestEventFactory(CommonAuditData commonFields, String authUser, String authUrl, String realm, String authStatus,
                                        String timestamp) {
        this(commonFields, authUser, authUrl, realm, authStatus);
        this.event.setTimestamp1(timestamp);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[]{event.getAction(), event.getUrl(), event.getUser(), event.getAuthStatus(), event.getRealm()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }
}
