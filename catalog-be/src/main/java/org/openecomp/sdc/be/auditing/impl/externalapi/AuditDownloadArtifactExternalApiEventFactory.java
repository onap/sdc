package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public class AuditDownloadArtifactExternalApiEventFactory extends AuditExternalApiEventFactory {

    private static final String LOG_STR = "CONSUMER_ID = \"%s\" RESOURCE_URL = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    public AuditDownloadArtifactExternalApiEventFactory(CommonAuditData commonFields, String resourceType, String resourceName,
                                                        String consumerId, String resourceUrl, ResourceAuditData prevParams, ResourceAuditData currParams,
                                                        String invariantUuid, User modifier, String artifactData) {
        super(AuditingActionEnum.DOWNLOAD_ARTIFACT, commonFields, resourceType, resourceName, consumerId, resourceUrl, prevParams, currParams,
                invariantUuid, modifier, artifactData);
    }

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getConsumerId(), event.getResourceURL(),event.getStatus(), event.getDesc());
    }
}
