package org.openecomp.sdc.notification.factories.impl;

import org.openecomp.sdc.notification.factories.NotificationsDaoFactory;
import org.openecomp.sdc.notification.factories.PropagationServiceFactory;
import org.openecomp.sdc.notification.services.PropagationService;
import org.openecomp.sdc.notification.services.impl.PropagationServiceImpl;

public class PropagationServiceFactoryImpl extends PropagationServiceFactory {
    private static final PropagationService INSTANCE = new PropagationServiceImpl(
            NotificationsDaoFactory.getInstance().createInterface());

    @Override
    public PropagationService createInterface() {
        return INSTANCE;
    }
}
