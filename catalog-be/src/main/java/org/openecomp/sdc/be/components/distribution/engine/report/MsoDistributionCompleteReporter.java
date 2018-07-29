package org.openecomp.sdc.be.components.distribution.engine.report;

import org.openecomp.sdc.be.components.distribution.engine.DistributionStatusNotification;
import org.openecomp.sdc.be.components.distribution.engine.rest.MSORestClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MsoDistributionCompleteReporter implements DistributionCompleteReporter {

    @Resource
    private MSORestClient msoClient;

    @Override
    public void reportDistributionComplete(DistributionStatusNotification distributionStatusNotification) {
        msoClient.notifyDistributionComplete(distributionStatusNotification.getDistributionID(), distributionStatusNotification.getStatus(), distributionStatusNotification.getErrorReason());
    }
}
