package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditCreateTopicDistribEngineEventFactory extends AuditDistribEngineEventFactory{

   private static final String LOG_STR = "ACTION = \"%s\" D_ENV = \"%s\" TOPIC_NAME = \"%s\" STATUS = \"%s\"";

    public AuditCreateTopicDistribEngineEventFactory(CommonAuditData commonFields, String consumerId, String distStatusTopic, String distNotifTopic,
                                                     String apiKey, String envName, String role) {
        super(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC, commonFields, consumerId, distStatusTopic, distNotifTopic, apiKey, envName, role);
    }

    @Override
    public String getLogMessage() {
        String topicName = event.getDnotifTopic() != null ? event.getDnotifTopic() : buildValue(event.getDstatusTopic());
        return String.format(LOG_STR, event.getAction(), event.getEnvironmentName(), topicName, event.getStatus());
    }

}
