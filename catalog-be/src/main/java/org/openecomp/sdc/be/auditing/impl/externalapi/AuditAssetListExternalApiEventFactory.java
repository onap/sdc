package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public class AuditAssetListExternalApiEventFactory extends AuditExternalApiEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" CONSUMER_ID = \"%s\" RESOURCE_URL = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    public AuditAssetListExternalApiEventFactory(AuditingActionEnum action, CommonAuditData commonFields,
                                                  DistributionData distributionData) {
         super(action, commonFields, new ResourceCommonInfo(), distributionData,
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
        return new String[] {event.getAction(), event.getConsumerId(), event.getResourceURL(),
                event.getStatus(), event.getDesc()};
    }
}
