package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public class AuditRemoveKeyDistribEngineEventFactory extends AuditAddRemoveKeyDistribEngineEventFactory {

    public AuditRemoveKeyDistribEngineEventFactory(CommonAuditData commonFields, String consumerId, String distStatusTopic, String distNotifTopic,
                                                   String apiKey, String envName, String role) {
        super(AuditingActionEnum.REMOVE_KEY_FROM_TOPIC_ACL, commonFields, consumerId, distStatusTopic, distNotifTopic, apiKey, envName, role);
    }
}
