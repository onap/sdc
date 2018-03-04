package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.ExternalApiEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public abstract class AuditExternalApiEventFactory extends AuditBaseEventFactory {

    protected final ExternalApiEvent event;

    public AuditExternalApiEventFactory(AuditingActionEnum action, CommonAuditData commonFields, String resourceType, String resourceName,
                                        String consumerId, String resourceUrl, ResourceAuditData prevParams, ResourceAuditData currParams,
                                        String invariantUuid, User modifier, String artifactData) {
        super(action);
        event = new ExternalApiEvent(getAction().getName(), commonFields, resourceType, resourceName, consumerId, resourceUrl,
                prevParams, currParams, AuditBaseEventFactory.buildUserName(modifier), invariantUuid, artifactData) ;
    }

    @Override
    public AuditingGenericEvent getDbEvent() { return event; }
}
