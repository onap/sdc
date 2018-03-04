package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public class AuditChangeLifecycleExternalApiEventFactory extends AuditExternalApiEventFactory {

    protected static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" CONSUMER_ID = \"%s\"" +
            " RESOURCE_URL = \"%s\" MODIFIER = \"%s\" PREV_VERSION = \"%s\" CURR_VERSION = \"%s\"" +
            " PREV_STATE = \"%s\" CURR_STATE = \"%s\" SERVICE_INSTANCE_ID = \"%s\" INVARIANT_UUID = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    public AuditChangeLifecycleExternalApiEventFactory(CommonAuditData commonFields, String resourceType, String resourceName,
                                                       String consumerId, String resourceUrl, ResourceAuditData prevParams, ResourceAuditData currParams,
                                                       String invariantUuid, User modifier, String artifactData) {
        super(AuditingActionEnum.CHANGE_LIFECYCLE_BY_API, commonFields, resourceType, resourceName, consumerId, resourceUrl, prevParams, currParams,
                invariantUuid, modifier, artifactData);
    }

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getResourceName(), event.getResourceType(), event.getConsumerId(),
                event.getResourceURL(), event.getModifier(), event.getPrevVersion(), event.getCurrVersion(), event.getPrevState(),
                event.getCurrState(), event.getServiceInstanceId(), event.getInvariantUuid(), event.getStatus(), event.getDesc());
    }
}
