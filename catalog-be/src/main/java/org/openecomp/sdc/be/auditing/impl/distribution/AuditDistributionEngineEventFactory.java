package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionEngineEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionTopicData;

public abstract class AuditDistributionEngineEventFactory extends AuditBaseEventFactory{

    protected final DistributionEngineEvent event;

    AuditDistributionEngineEventFactory(AuditingActionEnum action, CommonAuditData commonFields,
                                               DistributionTopicData distributionTopicData, String consumerId,
                                               String apiKey, String envName, String role) {
        super(action);
        event = new DistributionEngineEvent(getAction().getName(), commonFields, consumerId, distributionTopicData, apiKey, envName, role);
    }

    AuditDistributionEngineEventFactory(AuditingActionEnum action, CommonAuditData commonFields,
                                               DistributionTopicData distributionTopicData, String consumerId,
                                               String apiKey, String envName, String role, String timestamp) {
        this(action, commonFields, distributionTopicData, consumerId, apiKey, envName, role);
        this.event.setTimestamp1(timestamp);
    }

    protected String getTopicName() {
        return event.getDnotifTopic() != null ? event.getDnotifTopic() : event.getDstatusTopic();
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }
}
