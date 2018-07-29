package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.ResourceAdminEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public abstract class AuditResourceAdminEventFactory extends AuditBaseEventFactory {

    protected final ResourceAdminEvent event;

    AuditResourceAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo, ResourceVersionInfo prevParams,
                                          ResourceVersionInfo currParams, String invariantUuid,
                                          User modifier, String artifactData, String comment, String did, String toscaNodeType) {
        this(action, commonFields, resourceCommonInfo, prevParams, currParams, invariantUuid, AuditBaseEventFactory.buildUserName(modifier),
                artifactData, AuditBaseEventFactory.replaceNullNameWithEmpty(comment), did, toscaNodeType);
    }

    AuditResourceAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                          ResourceVersionInfo prevParams, ResourceVersionInfo currParams, String invariantUuid,
                                          String modifier, String artifactData, String comment, String did,
                                          String toscaNodeType, String timestamp) {
        this(action, commonFields, resourceCommonInfo, prevParams, currParams, invariantUuid, modifier,
                artifactData, comment, did, toscaNodeType);
        this.event.setTimestamp1(timestamp);
    }

    private AuditResourceAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceCommonInfo resourceCommonInfo,
                                          ResourceVersionInfo prevParams, ResourceVersionInfo currParams, String invariantUuid,
                                          String modifier, String artifactData, String comment, String did,
                                          String toscaNodeType) {
        super(action);
        this.event = new ResourceAdminEvent(getAction().getName(), commonFields, resourceCommonInfo, prevParams, currParams, invariantUuid, modifier,
                artifactData, comment, did, toscaNodeType);
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }

}
