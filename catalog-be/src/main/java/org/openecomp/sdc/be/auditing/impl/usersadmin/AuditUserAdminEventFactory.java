package org.openecomp.sdc.be.auditing.impl.usersadmin;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.UserAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditUserAdminEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" MODIFIER = \"%s\" USER_BEFORE = \"%s\" USER_AFTER = \"%s\" STATUS = \"%s\" DESC = \"%s\"";
    private final UserAdminEvent event;

    public AuditUserAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, User modifier, User userBefore, User userAfter) {
        this(action, commonFields, AuditBaseEventFactory.buildUserName(modifier),
                AuditBaseEventFactory.buildUserNameExtended(userBefore),
                AuditBaseEventFactory.buildUserNameExtended(userAfter));
    }

    //Used by migration util
    public AuditUserAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, String modifier, String userBefore,
                                      String userAfter, String timestamp) {
        this(action, commonFields, modifier, userBefore, userAfter);
        this.event.setTimestamp1(timestamp);
    }

    private AuditUserAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, String modifier, String userBefore,
                                      String userAfter) {
        super(action);
        event = new UserAdminEvent(action.getName(), commonFields, modifier, userBefore, userAfter);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getModifier(), event.getUserBefore(),
                event.getUserAfter(), event.getStatus(), event.getDesc()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }


}
