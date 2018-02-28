package org.openecomp.sdc.be.auditing.impl;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuthEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditAuthRequestEventFactory extends AuditBaseEventFactory {

    private final static String LOG_STR =  "ACTION = \"%s\" URL = \"%s\" USER = \"%s\" AUTH_STATUS = \"%s\" REALM = \"%s\"";

    final private AuthEvent event;

    public AuditAuthRequestEventFactory(CommonAuditData commonFields, String authUser, String authUrl, String realm, String authStatus) {
        super(AuditingActionEnum.AUTH_REQUEST);
        event = new AuthEvent(getAction().getName(), commonFields, authUser, authUrl, realm, authStatus);
    }

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getUrl(), event.getUser(), event.getAuthStatus(), event.getRealm());
    }



    @Override
    public AuditingGenericEvent getDbEvent() { return event; }
}
