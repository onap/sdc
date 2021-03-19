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
package org.openecomp.sdc.be.auditing.impl.usersadmin;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.GetUsersListEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditGetUsersListEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" MODIFIER = \"%s\" DETAILS = \"%s\" STATUS = \"%s\" DESC = \"%s\"";
    private final GetUsersListEvent event;

    public AuditGetUsersListEventFactory(CommonAuditData commonFields, User modifier, String userDetails) {
        this(commonFields, AuditBaseEventFactory.buildUserName(modifier), userDetails);
    }

    public AuditGetUsersListEventFactory(CommonAuditData commonFields, String modifier, String userDetails, String timestamp) {
        this(commonFields, modifier, userDetails);
        this.event.setTimestamp1(timestamp);
    }

    private AuditGetUsersListEventFactory(CommonAuditData commonFields, String modifier, String userDetails) {
        super(AuditingActionEnum.GET_USERS_LIST);
        event = new GetUsersListEvent(getAction().getName(), commonFields, modifier, userDetails);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[]{event.getAction(), event.getModifier(), event.getDetails(), event.getStatus(), event.getDesc()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }
}
