package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public class AuditImportResourceAdminEventFactory extends AuditCreateUpdateResourceAdminEventFactory {


    private static final String LOG_STR_TOSCA = LOG_STR + " TOSCA_NODE_TYPE = \"%s\"" ;


    public AuditImportResourceAdminEventFactory(CommonAuditData commonFields, ResourceAuditData prevParams, ResourceAuditData currParams,
                                                String resourceType, String resourceName, String invariantUuid,
                                                User modifier, String artifactData, String comment, String did, String toscaNodeType) {
        super(AuditingActionEnum.IMPORT_RESOURCE, commonFields, prevParams, currParams, resourceType, resourceName, invariantUuid,
                modifier, artifactData, comment, did, toscaNodeType);
    }

    public AuditImportResourceAdminEventFactory(CommonAuditData commonFields, ResourceAuditData prevParams, ResourceAuditData currParams,
                                                String resourceType, String resourceName,
                                                String invariantUuid, User modifier, String toscaNodeType) {
        super(AuditingActionEnum.IMPORT_RESOURCE, commonFields,  prevParams, currParams, resourceType, resourceName, invariantUuid,
                   modifier, null, null, null, toscaNodeType);
    }

    @Override    public String getLogMessage() {
        return String.format(LOG_STR_TOSCA, event.getAction(), buildValue(event.getResourceName()), buildValue(event.getResourceType()),
                buildValue(event.getServiceInstanceId()), buildValue(event.getInvariantUUID()), buildValue(event.getPrevVersion()),
                buildValue(event.getCurrVersion()), buildValue(event.getModifier()), buildValue(event.getPrevState()),
                buildValue(event.getCurrState()), buildValue(event.getStatus()), buildValue(event.getDesc()), buildValue(event.getToscaNodeType()));
    }
}
