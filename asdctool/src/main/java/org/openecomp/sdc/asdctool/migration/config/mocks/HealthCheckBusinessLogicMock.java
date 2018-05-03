package org.openecomp.sdc.asdctool.migration.config.mocks;

import javax.annotation.PostConstruct;

import org.openecomp.sdc.be.components.health.HealthCheckBusinessLogic;
import org.springframework.stereotype.Component;

@Component("healthCheckBusinessLogic")
public class HealthCheckBusinessLogicMock extends HealthCheckBusinessLogic {

    @Override
    @PostConstruct
    public void init() {

    }

    @Override
    protected void destroy() {

    }
}
