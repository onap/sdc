package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public class AuditCrudExternalApiArtifactEventFactory extends AuditExternalApiEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" CONSUMER_ID = \"%s\"" +
            " RESOURCE_URL = \"%s\" MODIFIER = \"%s\" PREV_ARTIFACT_UUID = \"%s\" CURR_ARTIFACT_UUID = \"%s\" ARTIFACT_DATA = \"%s\"" +
            " STATUS = \"%s\" DESC = \"%s\"";

    public AuditCrudExternalApiArtifactEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                                    DistributionData distributionData, ResourceVersionInfo prevParams, ResourceVersionInfo currParams,
                                                    String invariantUuid, User modifier, String artifactData) {
        super(action, commonFields, resourceCommonInfo, distributionData, prevParams, currParams,
                invariantUuid, modifier, artifactData);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getResourceName(), event.getResourceType(),
                event.getConsumerId(), event.getResourceURL(), event.getModifier(),
                event.getPrevArtifactUuid(), event.getCurrArtifactUuid(), event.getArtifactData(),
                event.getStatus(), event.getDesc()};
    }
}
