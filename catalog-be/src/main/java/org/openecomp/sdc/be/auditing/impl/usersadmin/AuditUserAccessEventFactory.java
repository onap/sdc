package org.openecomp.sdc.be.auditing.impl.usersadmin;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.UserAccessEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditUserAccessEventFactory extends AuditBaseEventFactory {

    private final static String LOG_STR = "ACTION = \"%s\" USER = \"%s\" STATUS = \"%s\" DESC = \"%s\"";
    private final UserAccessEvent event;

    public AuditUserAccessEventFactory(CommonAuditData commonFields, User user) {
        this(commonFields, AuditBaseEventFactory.buildUserName(user));
    }

    public AuditUserAccessEventFactory(CommonAuditData commonFields, String user, String timestamp) {
        this(commonFields, user);
        this.event.setTimestamp1(timestamp);
    }

    private AuditUserAccessEventFactory(CommonAuditData commonFields, String user) {
        super(AuditingActionEnum.USER_ACCESS);
        event = new UserAccessEvent(getAction().getName(), commonFields, user);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getUserUid(), event.getStatus(), event.getDesc()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

}
