package org.openecomp.sdc.be.components.utils;

import org.openecomp.sdc.be.model.Service;

public class ServiceBuilder extends ComponentBuilder<Service, ServiceBuilder> {

    private Service service;

    public ServiceBuilder(Service component) {
        super(component);
    }

    public ServiceBuilder() {
        super();
    }

    @Override
    protected Service component() {
        service = new Service();
        return service;
    }

    @Override
    protected ComponentBuilder<Service, ServiceBuilder> self() {
        return this;
    }


}
