package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public abstract class AuditAssetListExternalApiEventFactory extends AuditExternalApiEventFactory {

    protected static final String LOG_STR = "ACTION = \"%s\" CONSUMER_ID = \"%s\" RESOURCE_URL = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

     public AuditAssetListExternalApiEventFactory(AuditingActionEnum action, CommonAuditData commonFields, String resourceType, String resourceName,
                                                  String consumerId, String resourceUrl, ResourceAuditData prevParams, ResourceAuditData currParams,
                                                  String invariantUuid, User modifier, String artifactData) {
        super(action, commonFields, resourceType, resourceName, consumerId, resourceUrl, prevParams, currParams,
                invariantUuid, modifier, artifactData);
    }

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getConsumerId(), event.getResourceURL(),
                event.getStatus(), event.getDesc());
    }
}
