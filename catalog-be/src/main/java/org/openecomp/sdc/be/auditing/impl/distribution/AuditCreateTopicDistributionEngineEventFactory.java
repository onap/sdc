package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;

public class AuditCreateTopicDistributionEngineEventFactory extends AuditDistributionEngineEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" D_ENV = \"%s\" TOPIC_NAME = \"%s\" STATUS = \"%s\"";

    AuditCreateTopicDistributionEngineEventFactory(CommonAuditData commonFields, DistributionTopicData distributionTopicData,
                                                          String apiKey, String envName, String role) {
        super(AuditingActionEnum.CREATE_DISTRIBUTION_TOPIC, commonFields, distributionTopicData,null, apiKey, envName, role);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getEnvironmentName(), getTopicName(), event.getStatus()};
    }

}
