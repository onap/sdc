package org.openecomp.sdc.be.auditing.impl.usersadmin;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.UserAccessEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditUserAccessEventFactory extends AuditBaseEventFactory {

    protected final static String LOG_STR = "ACTION = \"%s\" USER = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    private final UserAccessEvent event;


    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getUserUid(), event.getStatus(), event.getDesc());
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }


    public AuditUserAccessEventFactory(CommonAuditData commonFields, User user) {
        super(AuditingActionEnum.USER_ACCESS);
        event = new UserAccessEvent(getAction().getName(), commonFields, AuditBaseEventFactory.buildUserName(user));
    }


}
