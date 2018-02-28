package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGetUebClusterEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditGetUebClusterEventFactory extends AuditBaseEventFactory {

    private AuditingGetUebClusterEvent event;

    private static final String LOG_STR = "ACTION = \"%s\" CONSUMER_ID = \"%s\" STATUS = \"%s\" STATUS_DESC = \"%s\"";

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getConsumerId(), event.getStatus(), event.getDesc());
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

    public AuditGetUebClusterEventFactory(CommonAuditData commonFields, String consumerId) {

       super(AuditingActionEnum.GET_UEB_CLUSTER);
       this.event = new AuditingGetUebClusterEvent(getAction().getName(), commonFields, consumerId);
    }
}
