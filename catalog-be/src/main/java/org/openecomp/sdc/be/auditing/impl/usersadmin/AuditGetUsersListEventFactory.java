package org.openecomp.sdc.be.auditing.impl.usersadmin;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.GetUsersListEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditGetUsersListEventFactory extends AuditBaseEventFactory {
    private final GetUsersListEvent event;

    private static final String LOG_STR = "ACTION = \"%s\" MODIFIER = \"%s\" DETAILS = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    public AuditGetUsersListEventFactory(CommonAuditData commonFields, User modifier,
                                         String userDetails) {
        super(AuditingActionEnum.GET_USERS_LIST);
        event = new GetUsersListEvent(getAction().getName(), commonFields, AuditBaseEventFactory.buildUserName(modifier), userDetails);
    }

   @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getModifier(), event.getDetails(), event.getStatus(), event.getDesc());
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

}
