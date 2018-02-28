package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionStatusEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditDistribStatusEventFactory extends AuditBaseEventFactory {

    private DistributionStatusEvent event;

    private static final String LOG_STR = "ACTION = \"%s\" DID = \"%s\" CONSUMER_ID = \"%s\" " +
            "TOPIC_NAME = \"%s\" RESOURCE_URL = \"%s\" STATUS_TIME = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getDid(), event.getConsumerId(), event.getTopicName(), event.getResoureURL(),
                event.getStatusTime(), event.getStatus(), event.getDesc());
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

    public AuditDistribStatusEventFactory(CommonAuditData commonFields, String did, String consumerId, String topicName,
                                          String resourceURL, String statusTime) {

       super(AuditingActionEnum.DISTRIBUTION_STATUS);
       this.event = new DistributionStatusEvent(getAction().getName(), commonFields, did, consumerId, topicName, resourceURL, statusTime);
    }
}
