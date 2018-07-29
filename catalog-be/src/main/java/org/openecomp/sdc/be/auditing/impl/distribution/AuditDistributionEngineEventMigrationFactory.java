package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;

public class AuditDistributionEngineEventMigrationFactory extends AuditDistributionEngineEventFactory {

    public AuditDistributionEngineEventMigrationFactory(AuditingActionEnum action, CommonAuditData commonFields,
                                                        DistributionTopicData distributionTopicData, String consumerId,
                                                        String apiKey, String envName, String role, String timestamp) {
        super(action, commonFields, distributionTopicData, consumerId, apiKey, envName, role, timestamp);
    }

    @Override
    public String getLogPattern() {
        return "";
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[0];
    }
}
