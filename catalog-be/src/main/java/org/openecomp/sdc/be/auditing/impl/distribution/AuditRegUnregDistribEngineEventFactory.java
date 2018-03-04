package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public abstract class AuditRegUnregDistribEngineEventFactory extends AuditDistribEngineEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" CONSUMER_ID = \"%s\" API_KEY = \"%s\" D_ENV = \"%s\" STATUS = \"%s\"" +
            " DESC = \"%s\" DNOTIF_TOPIC = \"%s\" DSTATUS_TOPIC = \"%s\"";

    public AuditRegUnregDistribEngineEventFactory(AuditingActionEnum action, CommonAuditData commonFields, String consumerId,
                                                  String distStatusTopic, String distNotifTopic, String apiKey, String envName, String role) {
        super(action, commonFields, consumerId, distStatusTopic, distNotifTopic, apiKey, envName, role);
    }

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getConsumerId(), event.getApiKey(), event.getEnvironmentName(),
                event.getStatus(), event.getDesc(), event.getDnotifTopic(),event.getDstatusTopic());
    }
}
