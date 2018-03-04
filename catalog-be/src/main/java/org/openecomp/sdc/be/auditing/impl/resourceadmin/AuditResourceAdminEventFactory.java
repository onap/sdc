package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public abstract class AuditResourceAdminEventFactory extends AuditBaseEventFactory {

    protected final ResourceAdminEvent event;

    public AuditResourceAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceAuditData prevParams,
                                          ResourceAuditData currParams, String resourceType, String resourceName, String invariantUuid,
                                          User modifier, String artifactData, String comment, String did, String toscaNodeType) {
        super(action);

        this.event = new ResourceAdminEvent(action.getName(), commonFields, prevParams, currParams, resourceType,
                resourceName, invariantUuid, AuditBaseEventFactory.buildUserName(modifier),
                artifactData, AuditBaseEventFactory.replaceNullNameWithEmpty(comment), did, toscaNodeType);
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

}
