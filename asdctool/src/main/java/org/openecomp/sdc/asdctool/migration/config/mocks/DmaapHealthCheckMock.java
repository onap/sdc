package org.openecomp.sdc.asdctool.migration.config.mocks;

import org.openecomp.sdc.be.components.distribution.engine.DmaapHealth;
import org.springframework.stereotype.Component;

@Component("dmaapHealth")
public class DmaapHealthCheckMock extends DmaapHealth {
    @Override
    public DmaapHealth init() {
        return null;
    }

    @Override
    protected void destroy() {
    }
}
