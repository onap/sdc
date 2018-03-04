package org.openecomp.sdc.be.auditing.impl.resourceadmin;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;
import org.openecomp.sdc.common.api.Constants;

public class AuditArtifactResourceAdminEventFactory extends AuditResourceAdminEventFactory {


    private static final String LOG_STR = "ACTION = \"%s\" RESOURCE_NAME = \"%s\" RESOURCE_TYPE = \"%s\" SERVICE_INSTANCE_ID = \"%s\"" +
            " INVARIANT_UUID = \"%s\" PREV_VERSION = \"%s\" CURR_VERSION = \"%s\" MODIFIER = \"%s\" PREV_STATE = \"%s\" CURR_STATE = \"%s\"" +
            " PREV_ARTIFACT_UUID = \"%s\" CURR_ARTIFACT_UUID = \"%s\" ARTIFACT_DATA = \"%s\" STATUS = \"%s\" DESC = \"%s\"";


    public AuditArtifactResourceAdminEventFactory(AuditingActionEnum action, CommonAuditData commonFields, ResourceAuditData prevParams, ResourceAuditData currParams,
                                                  String resourceType, String resourceName, String invariantUuid,
                                                  User modifier, String artifactData, String comment, String did) {
        super(action, commonFields, prevParams, currParams, resourceType, resourceName, invariantUuid,
                modifier, artifactData, comment, did, Constants.EMPTY_STRING);
    }

    @Override
    public String getLogMessage() {
        //TODO: check wheather or not "CONSUMER_ID =...RESOURCE_URL = " should be filled out - the info id not in the event
        return String.format(LOG_STR, buildValue(event.getAction()), buildValue(event.getResourceName()), buildValue(event.getResourceType()),
                buildValue(event.getServiceInstanceId()), buildValue(event.getInvariantUUID()), buildValue(event.getPrevVersion()),
                buildValue(event.getCurrVersion()), buildValue(event.getModifier()), buildValue(event.getPrevState()),
                buildValue(event.getCurrState()), buildValue(event.getPrevArtifactUUID()), buildValue(event.getCurrArtifactUUID()),
                buildValue(event.getArtifactData()), buildValue(event.getStatus()), buildValue(event.getDesc()));
    }
}
