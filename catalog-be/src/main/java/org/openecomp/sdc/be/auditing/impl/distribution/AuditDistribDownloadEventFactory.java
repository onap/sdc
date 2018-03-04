package org.openecomp.sdc.be.auditing.impl.distribution;

import org.openecomp.sdc.be.auditing.impl.AuditBaseEventFactory;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.AuditingGenericEvent;
import org.openecomp.sdc.be.resources.data.auditing.DistributionDownloadEvent;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.DistributionData;

public class AuditDistribDownloadEventFactory extends AuditBaseEventFactory {

    private DistributionDownloadEvent event;

    private static final String LOG_STR = "ACTION = \"%s\" CONSUMER_ID = \"%s\" RESOURCE_URL = \"%s\" STATUS = \"%s\" DESC = \"%s\"";

    public AuditDistribDownloadEventFactory(CommonAuditData commonFields, DistributionData distributionData) {

        super(AuditingActionEnum.DISTRIBUTION_ARTIFACT_DOWNLOAD);
        this.event = new DistributionDownloadEvent(getAction().getName(), commonFields, distributionData);
    }

    @Override
    public String getLogMessage() {
        return String.format(LOG_STR, event.getAction(), event.getConsumerId(), event.getResourceUrl(), event.getStatus(), event.getDesc());
    }

    @Override
    public AuditingGenericEvent getDbEvent() {
        return event;
    }


}
