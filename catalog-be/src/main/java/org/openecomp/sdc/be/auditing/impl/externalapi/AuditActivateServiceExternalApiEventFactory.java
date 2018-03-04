package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public class AuditActivateServiceExternalApiEventFactory extends AuditExternalApiEventFactory {

    protected static final String LOG_STR = "ACTION = \"%s\" RESOURCE_TYPE = \"%s\" CONSUMER_ID = \"%s\"" +
            " RESOURCE_URL = \"%s\" MODIFIER = \"%s\" STATUS = \"%s\" SERVICE_INSTANCE_ID = \"%s\" INVARIANT_UUID = \"%s\" DESC = \"%s\"";

    public AuditActivateServiceExternalApiEventFactory(CommonAuditData commonFields, String resourceType, String resourceName,
                                                       String consumerId, String resourceUrl, ResourceAuditData prevParams, ResourceAuditData currParams,
                                                       String invariantUuid, User modifier, String artifactData) {
        super(AuditingActionEnum.ACTIVATE_SERVICE_BY_API, commonFields, resourceType, resourceName, consumerId, resourceUrl, prevParams, currParams,
                invariantUuid, modifier, artifactData);
    }

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getResourceType(), event.getConsumerId(),
                event.getResourceURL(), event.getModifier(), event.getStatus(), event.getServiceInstanceId(),
                event.getInvariantUuid(), event.getDesc());
    }
}
