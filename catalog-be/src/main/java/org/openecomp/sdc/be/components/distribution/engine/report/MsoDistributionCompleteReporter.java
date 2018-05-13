package org.openecomp.sdc.be.components.distribution.engine.report;

import javax.annotation.Resource;

import org.openecomp.sdc.be.components.distribution.engine.DistributionStatusNotification;
import org.openecomp.sdc.be.components.distribution.engine.rest.MSORestClient;
import org.springframework.stereotype.Component;

@Component
public class MsoDistributionCompleteReporter implements DistributionCompleteReporter {

    @Resource
    private MSORestClient msoClient;

    @Override
    public void reportDistributionComplete(DistributionStatusNotification distributionStatusNotification) {
        msoClient.notifyDistributionComplete(distributionStatusNotification.getDistributionID(), distributionStatusNotification.getStatus(), distributionStatusNotification.getErrorReason());
    }
}
