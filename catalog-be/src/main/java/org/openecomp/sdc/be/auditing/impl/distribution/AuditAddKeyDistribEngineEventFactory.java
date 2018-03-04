package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditAddKeyDistribEngineEventFactory extends AuditAddRemoveKeyDistribEngineEventFactory {

    public AuditAddKeyDistribEngineEventFactory(CommonAuditData commonFields, String consumerId, String distStatusTopic, String distNotifTopic,
                                                String apiKey, String envName, String role) {
        super(AuditingActionEnum.ADD_KEY_TO_TOPIC_ACL, commonFields, consumerId, distStatusTopic, distNotifTopic, apiKey, envName, role);
    }
}
