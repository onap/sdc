package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public abstract class AuditAddRemoveKeyDistribEngineEventFactory extends AuditDistribEngineEventFactory{

    private static final String LOG_STR = "ACTION = \"%s\" D_ENV = \"%s\" TOPIC_NAME = \"%s\" ROLE = \"%s\" " +
            "API_KEY = \"%s\" STATUS = \"%s\"";

    public AuditAddRemoveKeyDistribEngineEventFactory(AuditingActionEnum action, CommonAuditData commonFields, String consumerId, String distStatusTopic, String distNotifTopic,
                                                      String apiKey, String envName, String role) {
        super(action, commonFields, consumerId, distStatusTopic, distNotifTopic, apiKey, envName, role);
    }

    @Override
    public String getLogMessage() {
        String topicName = event.getDnotifTopic() != null ? event.getDnotifTopic() : buildValue(event.getDstatusTopic());
        return String.format(LOG_STR, event.getAction(), event.getEnvironmentName(), topicName,
                event.getRole(), event.getApiKey(), event.getStatus());
    }

}
