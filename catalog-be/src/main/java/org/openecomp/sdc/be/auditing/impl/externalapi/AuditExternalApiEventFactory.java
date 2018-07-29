package org.openecomp.sdc.be.auditing.impl.externalapi;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.ExternalApiEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

public abstract class AuditExternalApiEventFactory extends AuditBaseEventFactory {

    protected final ExternalApiEvent event;

    public AuditExternalApiEventFactory(AuditingActionEnum action, CommonAuditData commonAuditData, ResourceCommonInfo resourceCommonInfo,
                                        DistributionData distributionData, ResourceVersionInfo prevParams, ResourceVersionInfo currParams,
                                        String invariantUuid, User modifier, String artifactData) {
        super(action);
        if (commonAuditData.getRequestId() == null) {
            commonAuditData.setRequestId(ThreadLocalsHolder.getUuid());
        }
        event = new ExternalApiEvent(getAction().getName(), commonAuditData, resourceCommonInfo, distributionData,
                prevParams, currParams, AuditBaseEventFactory.buildUserName(modifier), invariantUuid, artifactData) ;
    }

    @Override
    public AuditingGenericEvent getDbEvent() { return event; }
}
