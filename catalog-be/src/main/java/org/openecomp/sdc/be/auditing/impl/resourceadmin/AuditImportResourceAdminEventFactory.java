package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;

public class AuditImportResourceAdminEventFactory extends AuditCreateUpdateResourceAdminEventFactory {

    private static final String LOG_STR_TOSCA = LOG_STR + " TOSCA_NODE_TYPE = \"%s\"" ;

    public AuditImportResourceAdminEventFactory(CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo, ResourceVersionInfo prevParams, ResourceVersionInfo currParams,
                                                String invariantUuid, User modifier, String artifactData, String comment, String did, String toscaNodeType) {
        super(AuditingActionEnum.IMPORT_RESOURCE, commonAuditData, resourceCommonInfo, prevParams, currParams, invariantUuid,
                modifier, artifactData, comment, did, toscaNodeType);
    }

    public AuditImportResourceAdminEventFactory(CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo, ResourceVersionInfo prevParams, ResourceVersionInfo currParams,
                                                String invariantUuid, User modifier, String toscaNodeType) {
        super(AuditingActionEnum.IMPORT_RESOURCE, commonAuditData, resourceCommonInfo, prevParams, currParams, invariantUuid,
                   modifier, null, null, null, toscaNodeType);
    }

    @Override
    public String getLogPattern() {
        return LOG_STR_TOSCA;
    }

    @Override
    public String[] getLogMessageParams() {
        return new String[] {event.getAction(), event.getResourceName(), event.getResourceType(),
                event.getServiceInstanceId(), event.getInvariantUUID(), event.getPrevVersion(),
                event.getCurrVersion(), event.getModifier(), event.getPrevState(),
                event.getCurrState(), event.getStatus(), event.getDesc(), event.getToscaNodeType()};
    }
}
