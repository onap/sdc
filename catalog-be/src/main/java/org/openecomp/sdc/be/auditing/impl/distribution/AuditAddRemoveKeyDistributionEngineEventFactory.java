package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;

public class AuditAddRemoveKeyDistributionEngineEventFactory extends AuditDistributionEngineEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" D_ENV = \"%s\" TOPIC_NAME = \"%s\" ROLE = \"%s\" " +
            "API_KEY = \"%s\" STATUS = \"%s\"";

    AuditAddRemoveKeyDistributionEngineEventFactory(AuditingActionEnum action, CommonAuditData commonFields, DistributionTopicData distributionTopicData,
                                                           String apiKey, String envName, String role) {
        super(action, commonFields, distributionTopicData, null, apiKey, envName, role);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        String topicName = event.getDnotifTopic() != null ? event.getDnotifTopic() : event.getDstatusTopic();
        return new String[] {event.getAction(), event.getEnvironmentName(), topicName,
                event.getRole(), event.getApiKey(), event.getStatus()};
    }

}
