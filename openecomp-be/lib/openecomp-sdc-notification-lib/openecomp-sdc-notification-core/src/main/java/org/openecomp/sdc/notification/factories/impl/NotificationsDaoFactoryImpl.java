package org.openecomp.sdc.notification.factories.impl;

import org.openecomp.sdc.notification.dao.NotificationsDao;
import org.openecomp.sdc.notification.dao.impl.NotificationsDaoCassandraImpl;
import org.openecomp.sdc.notification.factories.NotificationsDaoFactory;

/**
 * @author Avrahamg
 * @since June 20, 2017
 */
public class NotificationsDaoFactoryImpl extends NotificationsDaoFactory {
  private static final NotificationsDao INSTANCE = new NotificationsDaoCassandraImpl();

  @Override
  public NotificationsDao createInterface() {
    return INSTANCE;
  }
}
