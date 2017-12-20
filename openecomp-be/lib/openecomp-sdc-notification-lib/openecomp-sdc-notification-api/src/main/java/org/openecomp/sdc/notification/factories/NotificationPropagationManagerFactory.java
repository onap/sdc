package org.openecomp.sdc.notification.factories;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;

public abstract class NotificationPropagationManagerFactory extends AbstractComponentFactory<NotificationPropagationManager> {


    public static NotificationPropagationManagerFactory getInstance() {
        return AbstractFactory.getInstance(NotificationPropagationManagerFactory.class);
    }


}
