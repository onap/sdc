package org.openecomp.sdc.notification.factories;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;
import org.openecomp.sdc.notification.services.PropagationService;

public abstract class PropagationServiceFactory extends AbstractComponentFactory<PropagationService> {


    public static PropagationServiceFactory getInstance() {
        return AbstractFactory.getInstance(PropagationServiceFactory.class);
    }


}
