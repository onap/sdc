package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public class AuditChangeLifecycleExternalApiEventFactory extends AuditExternalApiEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" CONSUMER_ID = \"%s\"" +
            " RESOURCE_URL = \"%s\" MODIFIER = \"%s\" PREV_VERSION = \"%s\" CURR_VERSION = \"%s\"" +
            " PREV_STATE = \"%s\" CURR_STATE = \"%s\" SERVICE_INSTANCE_ID = \"%s\" INVARIANT_UUID = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    public AuditChangeLifecycleExternalApiEventFactory(CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                                       DistributionData distributionData, ResourceVersionInfo prevParams, ResourceVersionInfo currParams,
                                                       String invariantUuid, User modifier) {
        super(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API, commonFields, resourceCommonInfo, distributionData, prevParams, currParams,
                invariantUuid, modifier, null);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getResourceName(), event.getResourceType(), event.getConsumerId(),
                event.getResourceURL(), event.getModifier(), event.getPrevVersion(), event.getCurrVersion(),
                event.getPrevState(), event.getCurrState(), event.getServiceInstanceId(), event.getInvariantUuid(),
                event.getStatus(), event.getDesc()};
    }
}
