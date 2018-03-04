package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditRegisterDistribEngineEventFactory extends AuditRegUnregDistribEngineEventFactory {

    public AuditRegisterDistribEngineEventFactory(CommonAuditData commonFields, String consumerId, String distStatusTopic, String distNotifTopic,
                                                  String apiKey, String envName, String role) {
        super(AuditingActionEnum.DISTRIBUTION_REGISTER, commonFields, consumerId, distStatusTopic, distNotifTopic, apiKey, envName, role);
    }
}
