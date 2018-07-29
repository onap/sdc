package org.openecomp.sdc.be.auditing.impl;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGetUebClusterEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditGetUebClusterEventFactory extends AuditBaseEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" CONSUMER_ID = \"%s\" STATUS_TIME = \"%s\" STATUS = \"%s\" STATUS_DESC = \"%s\"";
    private final AuditingGetUebClusterEvent event;

    public AuditGetUebClusterEventFactory(CommonAuditData commonFields, String consumerId) {

        super(AuditingActionEnum.GET_UEB_CLUSTER);
        event = new AuditingGetUebClusterEvent(getAction().getName(), commonFields, consumerId);
    }

    public AuditGetUebClusterEventFactory(CommonAuditData commonFields, String consumerId, String timestamp) {
        this(commonFields, consumerId);
        this.event.setTimestamp1(timestamp);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getConsumerId(), event.getTimestamp(), event.getStatus(), event.getDesc()};
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

}
