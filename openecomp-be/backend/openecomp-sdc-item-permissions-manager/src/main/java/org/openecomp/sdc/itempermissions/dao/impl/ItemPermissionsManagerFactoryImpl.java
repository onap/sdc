package org.openecomp.sdc.itempermissions.dao.impl;

import org.openecomp.sdc.itempermissions.ItemPermissionsManager;
import org.openecomp.sdc.itempermissions.ItemPermissionsManagerFactory;
import org.openecomp.sdc.itempermissions.PermissionsServicesFactory;
import org.openecomp.sdc.notification.factories.NotificationPropagationManagerFactory;
import org.openecomp.sdc.notification.factories.SubscriptionServiceFactory;
import org.openecomp.sdc.versioning.ItemManagerFactory;

/**
 * Created by ayalaben on 6/18/2017
 */
public class ItemPermissionsManagerFactoryImpl extends ItemPermissionsManagerFactory {

    private static final ItemPermissionsManager INSTANCE =
        new ItemPermissionsManagerImpl(PermissionsServicesFactory.getInstance().createInterface(),
            ItemManagerFactory.getInstance().createInterface(),
            NotificationPropagationManagerFactory.getInstance().createInterface(),
            SubscriptionServiceFactory.getInstance().createInterface());

    @Override
    public ItemPermissionsManager createInterface() {
        return INSTANCE;
    }
}
