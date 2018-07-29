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

    public AuditGetUsersListEventFactory(CommonAuditData commonFields, User modifier,
                                         String userDetails) {
        this(commonFields, AuditBaseEventFactory.buildUserName(modifier), userDetails);
    }

    public AuditGetUsersListEventFactory(CommonAuditData commonFields, String modifier,
                                         String userDetails, String timestamp) {
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
        return new String[] {event.getAction(), event.getModifier(), event.getDetails(), event.getStatus(), event.getDesc()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

}
