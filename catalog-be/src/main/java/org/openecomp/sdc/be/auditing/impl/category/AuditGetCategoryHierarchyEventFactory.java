package org.openecomp.sdc.be.auditing.impl.category;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.GetCategoryHierarchyEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditGetCategoryHierarchyEventFactory extends AuditBaseEventFactory {

    private final  GetCategoryHierarchyEvent event;

    protected static final String LOG_STR = "ACTION = \"%s\" MODIFIER = \"%s\" DETAILS = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getModifier(), event.getDetails(), event.getStatus(), event.getDesc());
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

    public AuditGetCategoryHierarchyEventFactory(AuditingActionEnum action, CommonAuditData commonFields, User modifier, String details) {
        super(action);
        event = new GetCategoryHierarchyEvent(getAction().getName(), commonFields, buildUserName(modifier), details);
    }
}
