package org.openecomp.sdc.be.components.distribution.engine.report;

import org.openecomp.sdc.be.components.distribution.engine.DistributionStatusNotification;

public interface DistributionCompleteReporter {

    void reportDistributionComplete(DistributionStatusNotification distributionStatusNotification);

}
