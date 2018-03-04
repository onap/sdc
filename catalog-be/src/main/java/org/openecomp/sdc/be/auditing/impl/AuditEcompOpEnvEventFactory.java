package org.openecomp.sdc.be.auditing.impl;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.EcompOperationalEnvironmentEvent;

public class AuditEcompOpEnvEventFactory extends AuditBaseEventFactory {
    private static final String LOG_STR =  "ACTION = \"%s\" OPERATIONAL_ENVIRONMENT_ACTION = \"%s\" OPERATIONAL_ENVIRONMENT_ID = \"%s\"" +
            " OPERATIONAL_ENVIRONMENT_NAME = \"%s\" OPERATIONAL_ENVIRONMENT_TYPE = \"%s\" TENANT_CONTEXT = \"%s\"";

    private final EcompOperationalEnvironmentEvent event;

    public AuditEcompOpEnvEventFactory(AuditingActionEnum action, String operationalEnvironmentId, String operationalEnvironmentName,
                                       String operationalEnvironmentType, String operationalEnvironmentAction, String tenantContext) {
        super(action);
        event = new EcompOperationalEnvironmentEvent(getAction().getName(), operationalEnvironmentId, operationalEnvironmentName,
                                                            operationalEnvironmentType, operationalEnvironmentAction, tenantContext);
    }

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, buildValue(event.getAction()), buildValue(event.getOperationalEnvironmentAction()),
                buildValue(event.getOperationalEnvironmentId()), buildValue(event.getOperationalEnvironmentName()),
                buildValue(event.getOperationalEnvironmentType()), buildValue(event.getTenantContext()));
    }

   @Override
    public AuditingGenericEvent getDbEvent() { return event; }
}
