package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDeployEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditDistribDeployEventFactory extends AuditBaseEventFactory {

    private DistributionDeployEvent event;

    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" " +
            "SERVICE_INSTANCE_ID = \"%s\" CURR_VERSION = \"%s\" MODIFIER = \"%s\" DID = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getResourceName(), event.getResourceType(), event.getServiceInstanceId(),
                event.getCurrVersion(), event.getModifier(), event.getDid(), event.getStatus(), event.getDesc());
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

    public AuditDistribDeployEventFactory(CommonAuditData commonFields, String currentVersion,
                                          String did, User modifier, String resourceName, String resourceType) {

       super(AuditingActionEnum.DISTRIBUTION_DEPLOY);
       this.event = new DistributionDeployEvent(getAction().getName(), commonFields, did, buildUserName(modifier),
                                currentVersion, resourceName, resourceType);
    }
}
