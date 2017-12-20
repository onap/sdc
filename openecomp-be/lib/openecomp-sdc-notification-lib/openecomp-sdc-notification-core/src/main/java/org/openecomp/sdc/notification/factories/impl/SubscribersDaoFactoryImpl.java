package org.openecomp.sdc.notification.factories.impl;

import org.openecomp.sdc.notification.dao.SubscribersDao;
import org.openecomp.sdc.notification.dao.impl.SubscribersDaoCassandraImpl;
import org.openecomp.sdc.notification.factories.SubscribersDaoFactory;


public class SubscribersDaoFactoryImpl extends SubscribersDaoFactory {
    private static final SubscribersDao INSTANCE = new SubscribersDaoCassandraImpl();

    @Override
    public SubscribersDao createInterface() {
        return INSTANCE;
    }
}
