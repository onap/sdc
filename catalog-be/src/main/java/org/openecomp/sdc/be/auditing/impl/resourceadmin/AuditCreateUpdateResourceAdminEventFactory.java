package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public class AuditCreateUpdateResourceAdminEventFactory extends AuditResourceAdminEventFactory {


    protected static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" SERVICE_INSTANCE_ID = \"%s\"" +
            " INVARIANT_UUID = \"%s\" PREV_VERSION = \"%s\" CURR_VERSION = \"%s\" MODIFIER = \"%s\" PREV_STATE = \"%s\" CURR_STATE = \"%s\"" +
            " STATUS = \"%s\" DESC = \"%s\"";


    public AuditCreateUpdateResourceAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceAuditData prevParams, ResourceAuditData currParams,
                                                      String resourceType, String resourceName, String invariantUuid,
                                                      User modifier, String artifactData, String comment, String did, String toscaNodeType) {
        super(action, commonFields, prevParams, currParams, resourceType, resourceName, invariantUuid,
                modifier, artifactData, comment, did, toscaNodeType);
    }

   @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), buildValue(event.getResourceName()), buildValue(event.getResourceType()),
                buildValue(event.getServiceInstanceId()), buildValue(event.getInvariantUUID()), buildValue(event.getPrevVersion()),
                buildValue(event.getCurrVersion()), buildValue(event.getModifier()), buildValue(event.getPrevState()),
                buildValue(event.getCurrState()), event.getStatus(), event.getDesc());
    }
}
