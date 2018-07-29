package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;

public class AuditRegUnregDistributionEngineEventFactory extends AuditDistributionEngineEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" CONSUMER_ID = \"%s\" API_KEY = \"%s\" D_ENV = \"%s\" STATUS = \"%s\"" +
            " DESC = \"%s\" DNOTIF_TOPIC = \"%s\" DSTATUS_TOPIC = \"%s\"";

    public AuditRegUnregDistributionEngineEventFactory(AuditingActionEnum action, CommonAuditData commonFields,
                                                       DistributionTopicData distrTopicData, String consumerId, String apiKey, String envName) {
        super(action, commonFields, distrTopicData, consumerId, apiKey, envName, null);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getConsumerId(), event.getApiKey(),
                event.getEnvironmentName(), event.getStatus(), event.getDesc(),
                event.getDnotifTopic(), event.getDstatusTopic()};
    }
}
