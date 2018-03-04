package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditUnregisterDistribEngineEventFactory extends AuditRegUnregDistribEngineEventFactory {

    public AuditUnregisterDistribEngineEventFactory(CommonAuditData commonFields, String consumerId, String distStatusTopic, String distNotifTopic,
                                                    String apiKey, String envName, String role) {
        super(AuditingActionEnum.DISTRIBUTION_UN_REGISTER, commonFields, consumerId, distStatusTopic, distNotifTopic, apiKey, envName, role);
    }
}
