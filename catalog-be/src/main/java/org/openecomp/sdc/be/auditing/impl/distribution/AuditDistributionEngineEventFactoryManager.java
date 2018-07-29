package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

public class AuditDistributionEngineEventFactoryManager {

    public static AuditEventFactory createDistributionEngineEventFactory(AuditingActionEnum action, String environmentName,
                                                                         DistributionTopicData distributionTopicData, String role, String apiKey, String status) {
        AuditEventFactory factory;

        switch (action) {
            case CREATE_DISTRIBUTION_TOPIC:
                factory = new AuditCreateTopicDistributionEngineEventFactory(
                        CommonAuditData.newBuilder()
                                .status(status)
                                .requestId(ThreadLocalsHolder.getUuid())
                                .build(),
                        distributionTopicData, apiKey, environmentName, role);
                break;
            case ADD_KEY_TO_TOPIC_ACL:
            case REMOVE_KEY_FROM_TOPIC_ACL:
                factory = new AuditAddRemoveKeyDistributionEngineEventFactory(action,
                        CommonAuditData.newBuilder()
                                .status(status)
                                .requestId(ThreadLocalsHolder.getUuid())
                                .build(),
                        distributionTopicData, apiKey, environmentName, role);
                break;
            default:
                throw new UnsupportedOperationException();

        }
        return factory;

    }
}
