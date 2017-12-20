package org.openecomp.sdc.notification.factories.impl;

import org.openecomp.sdc.notification.factories.NotificationPropagationManagerFactory;
import org.openecomp.sdc.notification.factories.PropagationServiceFactory;
import org.openecomp.sdc.notification.factories.SubscriptionServiceFactory;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.notification.services.impl.NotificationPropagationManagerImpl;

public class NotificationPropagationManagerFactoryImpl extends
    NotificationPropagationManagerFactory {
    private static final NotificationPropagationManager INSTANCE = new NotificationPropagationManagerImpl(
            PropagationServiceFactory.getInstance().createInterface(), SubscriptionServiceFactory
        .getInstance().createInterface());

    @Override
    public NotificationPropagationManager createInterface() {
        return INSTANCE;
    }
}
