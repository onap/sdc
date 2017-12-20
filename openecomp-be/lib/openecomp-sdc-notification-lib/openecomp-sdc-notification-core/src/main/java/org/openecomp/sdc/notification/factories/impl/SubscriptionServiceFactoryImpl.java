package org.openecomp.sdc.notification.factories.impl;

import org.openecomp.sdc.notification.factories.SubscribersDaoFactory;
import org.openecomp.sdc.notification.factories.SubscriptionServiceFactory;
import org.openecomp.sdc.notification.services.SubscriptionService;
import org.openecomp.sdc.notification.services.impl.SubscriptionServiceImpl;


public class SubscriptionServiceFactoryImpl extends SubscriptionServiceFactory {
    private static final SubscriptionService INSTANCE = new SubscriptionServiceImpl
        (SubscribersDaoFactory.getInstance().createInterface());

    @Override
    public SubscriptionService createInterface() {
        return INSTANCE;
    }
}