package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionStatusEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;

public class AuditDistributionStatusEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" DID = \"%s\" CONSUMER_ID = \"%s\" " +
            "TOPIC_NAME = \"%s\" RESOURCE_URL = \"%s\" STATUS_TIME = \"%s\" STATUS = \"%s\" DESC = \"%s\"";
    private final DistributionStatusEvent event;

    public AuditDistributionStatusEventFactory(CommonAuditData commonFields, DistributionData distributionData,
                                               String did, String topicName, String statusTime) {
        super(AuditingActionEnum.DISTRIBUTION_STATUS);
        this.event = new DistributionStatusEvent(getAction().getName(), commonFields, distributionData, did,
                topicName, statusTime);
    }

    public AuditDistributionStatusEventFactory(CommonAuditData commonFields, DistributionData distributionData,
                                               String did, String topicName, String statusTime, String timestamp) {
        this(commonFields, distributionData, did, topicName, statusTime);
        this.event.setTimestamp1(timestamp);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getDid(), event.getConsumerId(), event.getTopicName(),
                event.getResoureURL(), event.getStatusTime(), event.getStatus(), event.getDesc()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }
}
