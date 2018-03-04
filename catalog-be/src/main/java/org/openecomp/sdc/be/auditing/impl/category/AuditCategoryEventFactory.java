package org.openecomp.sdc.be.auditing.impl.category;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.CategoryEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public abstract class AuditCategoryEventFactory extends AuditBaseEventFactory {

    private final CategoryEvent event;

    protected static final  String LOG_STR = "ACTION = \"%s\" MODIFIER = \"%s\" CATEGORY_NAME = \"%s\" SUB_CATEGORY_NAME = \"%s\"" +
            " GROUPING_NAME = \"%s\" RESOURCE_TYPE = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getModifier(), event.getCategoryName(), event.getSubCategoryName(),
                event.getGroupingName(), event.getResourceType(), event.getStatus(), event.getDesc());
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

    public AuditCategoryEventFactory(AuditingActionEnum action, CommonAuditData commonFields, User modifier,
                                     String categoryName, String subCategoryName, String groupingName, String resourceType) {
        super(action);
        event = new CategoryEvent(getAction().getName(), commonFields, buildUserName(modifier), categoryName,
                subCategoryName, groupingName, resourceType);
    }
}
