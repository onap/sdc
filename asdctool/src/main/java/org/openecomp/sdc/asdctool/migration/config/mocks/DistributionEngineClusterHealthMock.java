package org.openecomp.sdc.asdctool.migration.config.mocks;

import org.openecomp.sdc.be.components.distribution.engine.DistributionEngineClusterHealth;
import org.springframework.stereotype.Component;

@Component("distribution-engine-cluster-health")
public class DistributionEngineClusterHealthMock extends DistributionEngineClusterHealth {

    @Override
    protected void init() {
    }

    @Override
    protected void destroy() {
    }
}
