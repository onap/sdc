package org.openecomp.sdc.notification.factories.impl;

import org.openecomp.sdc.notification.factories.LastNotificationDaoFactory;
import org.openecomp.sdc.notification.factories.NotificationsDaoFactory;
import org.openecomp.sdc.notification.factories.NotificationsServiceFactory;
import org.openecomp.sdc.notification.services.NotificationsService;
import org.openecomp.sdc.notification.services.impl.NotificationsServiceImpl;

/**
 * @author Avrahamg
 * @since June 20, 2017
 */
public class NotificationsServiceFactoryImpl extends NotificationsServiceFactory {
  private static final NotificationsService INSTANCE = new NotificationsServiceImpl(
      LastNotificationDaoFactory.getInstance().createInterface(), NotificationsDaoFactory
      .getInstance().createInterface());

  @Override
  public NotificationsService createInterface() {
    return INSTANCE;
  }
}
