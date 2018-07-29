package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.OperationalEnvAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public class AuditDistributionNotificationEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" " +
            "SERVICE_INSTANCE_ID = \"%s\" CURR_VERSION = \"%s\" MODIFIER = \"%s\" CURR_STATE = \"%s\" DID = \"%s\" " +
            "TOPIC_NAME = \"%s\" STATUS = \"%s\" DESC = \"%s\" TENANT = \"%s\" VNF_WORKLOAD_CONTEXT = \"%s\" ENV_ID = \"%s\"";
    private final DistributionNotificationEvent event;

    public AuditDistributionNotificationEventFactory(CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo,
                                                     ResourceVersionInfo resourceVersionInfo,
                                                     String did, User modifier, String topicName,
                                                     OperationalEnvAuditData opEnvFields) {
        this(commonAuditData, resourceCommonInfo,
                resourceVersionInfo, did, AuditBaseEventFactory.buildUserName(modifier),
                topicName, opEnvFields);
    }

    public AuditDistributionNotificationEventFactory(CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo,
                                                     ResourceVersionInfo resourceVersionInfo,
                                                     String did, String modifier, String topicName,
                                                     OperationalEnvAuditData opEnvFields, String timestamp) {
        this(commonAuditData, resourceCommonInfo, resourceVersionInfo, did, modifier, topicName, opEnvFields);
        this.event.setTimestamp1(timestamp);
    }

    private AuditDistributionNotificationEventFactory(CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo,
                                                     ResourceVersionInfo resourceVersionInfo,
                                                     String did, String modifier, String topicName,
                                                     OperationalEnvAuditData opEnvFields) {
        super(AuditingActionEnum.DISTRIBUTION_NOTIFY);
        this.event = new DistributionNotificationEvent(getAction().getName(), commonAuditData, resourceCommonInfo,
                resourceVersionInfo, did, modifier, topicName, opEnvFields);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getResourceName(), event.getResourceType(), event.getServiceInstanceId(),
                event.getCurrVersion(), event.getModifier(), event.getCurrState(), event.getDid(), event.getTopicName(),
                event.getStatus(), event.getDesc(), event.getTenant(), event.getVnfWorkloadContext(), event.getEnvId()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

}
