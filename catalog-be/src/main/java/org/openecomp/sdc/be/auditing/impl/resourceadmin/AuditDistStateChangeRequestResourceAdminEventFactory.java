package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.api.Constants;

public class AuditDistStateChangeRequestResourceAdminEventFactory extends AuditResourceAdminEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" SERVICE_INSTANCE_ID = \"%s\"" +
            " CURR_VERSION = \"%s\" MODIFIER = \"%s\" CURR_STATE = \"%s\"" +
            " DPREV_STATUS = \"%s\" DCURR_STATUS = \"%s\" DID = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    public AuditDistStateChangeRequestResourceAdminEventFactory(CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                                                ResourceVersionInfo prevParams, ResourceVersionInfo currParams,
                                                                String invariantUuid, User modifier, String artifactData, String comment, String did) {
        super(AuditingActionEnum.DISTRIBUTION_STATE_CHANGE_REQUEST, commonFields, resourceCommonInfo, prevParams, currParams, invariantUuid,
                modifier, artifactData, comment, did, Constants.EMPTY_STRING);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getResourceName(), event.getResourceType(),
                event.getServiceInstanceId(), event.getCurrVersion(), event.getModifier(),
                event.getCurrState(), event.getDprevStatus(), event.getDcurrStatus(),
                event.getDid(), event.getStatus(), event.getDesc()};
    }
}
