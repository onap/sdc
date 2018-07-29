package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.api.Constants;

public class AuditCertificationResourceAdminEventFactory extends AuditResourceAdminEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" SERVICE_INSTANCE_ID = \"%s\"" +
            " INVARIANT_UUID = \"%s\" PREV_VERSION = \"%s\" CURR_VERSION = \"%s\" MODIFIER = \"%s\" PREV_STATE = \"%s\" CURR_STATE = \"%s\"" +
            " COMMENT = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    public AuditCertificationResourceAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo, ResourceVersionInfo prevParams, ResourceVersionInfo currParams,
                                                       String invariantUuid, User modifier, String artifactData, String comment, String did) {
        super(action, commonFields, resourceCommonInfo, prevParams, currParams, invariantUuid,
                modifier, artifactData, comment, did, Constants.EMPTY_STRING);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getResourceName(), event.getResourceType(),
                event.getServiceInstanceId(), event.getInvariantUUID(), event.getPrevVersion(),
                event.getCurrVersion(), event.getModifier(), event.getPrevState(),
                event.getCurrState(), event.getComment(), event.getStatus(), event.getDesc()};
    }
}
