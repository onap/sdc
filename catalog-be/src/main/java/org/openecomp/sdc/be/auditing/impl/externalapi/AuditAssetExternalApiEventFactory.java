package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public class AuditAssetExternalApiEventFactory extends AuditExternalApiEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" CONSUMER_ID = \"%s\" RESOURCE_URL = \"%s\" RESOURCE_NAME = \"%s\" " +
            "RESOURCE_TYPE = \"%s\" SERVICE_INSTANCE_ID = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    public AuditAssetExternalApiEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                             DistributionData distributionData) {
        super(action, commonFields, resourceCommonInfo, distributionData,
                ResourceVersionInfo.newBuilder()
                        .build(),
                ResourceVersionInfo.newBuilder()
                        .build(),
                null, null, null);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getConsumerId(), event.getResourceURL(), event.getResourceName(),
                event.getResourceType(), event.getServiceInstanceId(), event.getStatus(), event.getDesc()};
    }
}
