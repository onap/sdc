package org.openecomp.sdc.be.auditing.impl.usersadmin;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.UserAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditUserAdminEventFactory extends AuditBaseEventFactory {

    final private UserAdminEvent event;

    protected final static String LOG_STR = "ACTION = \"%s\" MODIFIER = \"%s\" USER_BEFORE = \"%s\" USER_AFTER = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, buildValue(event.getAction()), buildValue(event.getModifier()), buildValue(event.getUserBefore()),
                buildValue(event.getUserAfter()), buildValue(event.getStatus()), buildValue(event.getDesc()));
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

    public AuditUserAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, User modifier, User userBefore, User userAfter) {
        super(action);
        event = new UserAdminEvent(getAction().getName(), commonFields, AuditBaseEventFactory.buildUserName(modifier),
                AuditBaseEventFactory.buildUserNameExtended(userBefore),
                AuditBaseEventFactory.buildUserNameExtended(userAfter));
     }
}
