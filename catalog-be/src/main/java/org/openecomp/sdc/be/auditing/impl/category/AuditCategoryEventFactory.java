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
import org.openecomp.sdc.be.resources.data.auditing.CategoryEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditCategoryEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" MODIFIER = \"%s\" CATEGORY_NAME = \"%s\" SUB_CATEGORY_NAME = \"%s\""
        + " GROUPING_NAME = \"%s\" RESOURCE_TYPE = \"%s\" STATUS = \"%s\" DESC = \"%s\"";
    private final CategoryEvent event;

    private AuditCategoryEventFactory(AuditingActionEnum action, CommonAuditData commonFields, String modifier, String categoryName,
                                      String subCategoryName, String groupingName, String resourceType) {
        super(action);
        event = new CategoryEvent(getAction().getName(), commonFields, modifier, categoryName, subCategoryName, groupingName, resourceType);
    }

    public AuditCategoryEventFactory(AuditingActionEnum action, CommonAuditData commonFields, User modifier, String categoryName,
                                     String subCategoryName, String groupingName, String resourceType) {
        this(action, commonFields, buildUserName(modifier), categoryName, subCategoryName, groupingName, resourceType);
    }

    public AuditCategoryEventFactory(AuditingActionEnum action, CommonAuditData commonFields, String modifier, String categoryName,
                                     String subCategoryName, String groupingName, String resourceType, String timestamp) {
        this(action, commonFields, modifier, categoryName, subCategoryName, groupingName, resourceType);
        event.setTimestamp1(timestamp);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[]{event.getAction(), event.getModifier(), event.getCategoryName(), event.getSubCategoryName(), event.getGroupingName(),
            event.getResourceType(), event.getStatus(), event.getDesc()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }
}
