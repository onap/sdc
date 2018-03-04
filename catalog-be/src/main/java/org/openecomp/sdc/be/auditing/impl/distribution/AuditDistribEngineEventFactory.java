package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionEngineEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;

public abstract class AuditDistribEngineEventFactory extends AuditBaseEventFactory{

    protected final DistributionEngineEvent event;

    public AuditDistribEngineEventFactory(AuditingActionEnum action, CommonAuditData commonFields, String consumerId, String distStatusTopic, String distNotifTopic,
                                          String apiKey, String envName, String role) {
        super(action);
        event = new DistributionEngineEvent(getAction().getName(), commonFields, consumerId, distStatusTopic, distNotifTopic, apiKey, envName, role);
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }
}
