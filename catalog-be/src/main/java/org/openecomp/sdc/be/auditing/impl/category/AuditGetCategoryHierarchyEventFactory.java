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
package org.openecomp.sdc.be.auditing.impl.category;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.GetCategoryHierarchyEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditGetCategoryHierarchyEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" MODIFIER = \"%s\" DETAILS = \"%s\" STATUS = \"%s\" DESC = \"%s\"";
    private final GetCategoryHierarchyEvent event;

    public AuditGetCategoryHierarchyEventFactory(CommonAuditData commonFields, User modifier, String details) {
        this(commonFields, buildUserName(modifier), details);
    }

    public AuditGetCategoryHierarchyEventFactory(CommonAuditData commonFields, String modifier, String details, String timestamp) {
        this(commonFields, modifier, details);
        this.event.setTimestamp1(timestamp);
    }

    private AuditGetCategoryHierarchyEventFactory(CommonAuditData commonFields, String modifier, String details) {
        super(AuditingActionEnum.GET_CATEGORY_HIERARCHY);
        event = new GetCategoryHierarchyEvent(getAction().getName(), commonFields, modifier, details);
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
