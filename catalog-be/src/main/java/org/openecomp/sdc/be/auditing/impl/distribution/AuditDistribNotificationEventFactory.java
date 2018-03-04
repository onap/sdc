package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionNotificationEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.OperationalEnvAuditData;

public class AuditDistribNotificationEventFactory extends AuditBaseEventFactory {

    private DistributionNotificationEvent event;

    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" " +
            "SERVICE_INSTANCE_ID = \"%s\" CURR_VERSION = \"%s\" MODIFIER = \"%s\" CURR_STATE = \"%s\" DID = \"%s\" " +
            "TOPIC_NAME = \"%s\" STATUS = \"%s\" DESC = \"%s\" TENANT = \"%s\" VNF_WORKLOAD_CONTEXT = \"%s\" ENV_ID = \"%s\"";

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getResourceName(), event.getResourceType(), event.getServiceInstanceId(),
                event.getCurrVersion(), event.getModifier(), event.getCurrState(), event.getDid(), event.getTopicName(), event.getStatus(), event.getDesc(),
                event.getTenant(), event.getVnfWorkloadContext(), event.getEnvId());
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

    public AuditDistribNotificationEventFactory(CommonAuditData commonFields, String currentState, String currentVersion,
                                                String did, User modifier, String resourceName,
                                                String resourceType, String topicName, OperationalEnvAuditData opEnvFields) {

       super(AuditingActionEnum.DISTRIBUTION_NOTIFY);
       this.event = new DistributionNotificationEvent(getAction().getName(), commonFields, did, AuditBaseEventFactory.buildUserName(modifier),
                currentState, currentVersion,resourceName, resourceType, topicName, opEnvFields);
    }
}
