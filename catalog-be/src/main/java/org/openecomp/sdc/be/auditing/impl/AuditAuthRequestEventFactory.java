package org.openecomp.sdc.be.auditing.impl;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuthEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditAuthRequestEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR =  "ACTION = \"%s\" URL = \"%s\" USER = \"%s\" AUTH_STATUS = \"%s\" REALM = \"%s\"";
    private final AuthEvent event;

    public AuditAuthRequestEventFactory(CommonAuditData commonFields, String authUser, String authUrl, String realm, String authStatus) {
        super(AuditingActionEnum.AUTH_REQUEST);
        event = new AuthEvent(getAction().getName(), commonFields, authUser, authUrl, realm, authStatus);
    }

    public AuditAuthRequestEventFactory(CommonAuditData commonFields, String authUser, String authUrl, String realm,
                                        String authStatus, String timestamp) {
       this(commonFields, authUser, authUrl, realm, authStatus);
       this.event.setTimestamp1(timestamp);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getUrl(), event.getUser(),
                event.getAuthStatus(), event.getRealm()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() { return event; }
}
