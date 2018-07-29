package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public class AuditCreateResourceExternalApiEventFactory extends AuditExternalApiEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" CONSUMER_ID = \"%s\"" +
            " RESOURCE_URL = \"%s\" MODIFIER = \"%s\" CURR_VERSION = \"%s\" CURR_STATE = \"%s\" CURR_ARTIFACT_UUID = \"%s\" " +
            "STATUS = \"%s\" SERVICE_INSTANCE_ID = \"%s\" INVARIANT_UUID = \"%s\" DESC = \"%s\"";

    public AuditCreateResourceExternalApiEventFactory(CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                                      DistributionData distributionData, ResourceVersionInfo currResourceVersionInfo,
                                                      String invariantUuid, User modifier) {
        super(AuditingActionEnum.CREATE_RESOURCE_BY_API, commonFields, resourceCommonInfo, distributionData,
                ResourceVersionInfo.newBuilder()
                        .build(),
                currResourceVersionInfo,
                invariantUuid, modifier, null);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getResourceName(), event.getResourceType(),
                event.getConsumerId(), event.getResourceURL(), event.getModifier(),
                event.getCurrVersion(), event.getCurrState(), event.getCurrArtifactUuid(),
                event.getStatus(), event.getServiceInstanceId(), event.getInvariantUuid(),
                event.getDesc()};
    }
}
