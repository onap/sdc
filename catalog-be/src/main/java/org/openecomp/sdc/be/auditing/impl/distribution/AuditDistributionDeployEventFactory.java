package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;

public class AuditDistributionDeployEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" " +
            "SERVICE_INSTANCE_ID = \"%s\" CURR_VERSION = \"%s\" MODIFIER = \"%s\" DID = \"%s\" STATUS = \"%s\" DESC = \"%s\"";
    private final DistributionDeployEvent event;

    public AuditDistributionDeployEventFactory(CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                               String did, User modifier, String currentVersion) {
        this(commonFields, resourceCommonInfo, did, buildUserName(modifier), currentVersion);
    }

    public AuditDistributionDeployEventFactory(CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                               String did, String modifier, String currentVersion, String timestamp) {
        this(commonFields, resourceCommonInfo, did, modifier, currentVersion);
        this.event.setTimestamp1(timestamp);
    }

    private AuditDistributionDeployEventFactory(CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                               String did, String modifier, String currentVersion) {
        super(AuditingActionEnum.DISTRIBUTION_DEPLOY);
        event = new DistributionDeployEvent(getAction().getName(), commonFields, resourceCommonInfo, did, modifier,
                currentVersion);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getResourceName(), event.getResourceType(), event.getServiceInstanceId(),
                event.getCurrVersion(), event.getModifier(), event.getDid(), event.getStatus(), event.getDesc()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

}
