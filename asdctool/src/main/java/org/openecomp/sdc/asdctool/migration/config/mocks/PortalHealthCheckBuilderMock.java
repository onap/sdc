package org.openecomp.sdc.asdctool.migration.config.mocks;

import org.openecomp.sdc.be.components.health.PortalHealthCheckBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component("portalHealthCheckBusinessLogic")
public class PortalHealthCheckBuilderMock extends PortalHealthCheckBuilder {


    @Override
    @PostConstruct
    public PortalHealthCheckBuilder init() {
        return null;
    }

    @Override
    @PreDestroy
    protected void destroy() {

    }
}
