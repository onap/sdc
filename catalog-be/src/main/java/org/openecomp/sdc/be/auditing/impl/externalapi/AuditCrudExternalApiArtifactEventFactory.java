package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public abstract class AuditCrudExternalApiArtifactEventFactory extends AuditExternalApiEventFactory {

    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" CONSUMER_ID = \"%s\"" +
            " RESOURCE_URL = \"%s\" MODIFIER = \"%s\" PREV_ARTIFACT_UUID = \"%s\" CURR_ARTIFACT_UUID = \"%s\" ARTIFACT_DATA = \"%s\"" +
            " STATUS = \"%s\" DESC = \"%s\"";

    public AuditCrudExternalApiArtifactEventFactory(AuditingActionEnum action, CommonAuditData commonFields, String resourceType, String resourceName,
                                                    String consumerId, String resourceUrl, ResourceAuditData prevParams, ResourceAuditData currParams,
                                                    String invariantUuid, User modifier, String artifactData) {
        super(action, commonFields, resourceType, resourceName, consumerId, resourceUrl, prevParams, currParams,
                invariantUuid, modifier, artifactData);
    }

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getResourceName(), event.getResourceType(), event.getConsumerId(),
                event.getResourceURL(), event.getModifier(), event.getPrevArtifactUuid(), event.getCurrArtifactUuid(),
                event.getArtifactData(), event.getStatus(), event.getDesc());
    }
}
