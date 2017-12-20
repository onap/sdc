package org.openecomp.sdc.notification.factories.impl;

import org.openecomp.sdc.notification.dao.LastNotificationDao;
import org.openecomp.sdc.notification.dao.impl.LastNotificationDaoCassandraImpl;
import org.openecomp.sdc.notification.factories.LastNotificationDaoFactory;

/**
 * @author itzikpa
 * @since June 23, 2017
 */

public class LastNotificationDaoFactoryImpl extends LastNotificationDaoFactory {
  private static final LastNotificationDao INSTANCE = new LastNotificationDaoCassandraImpl();

  @Override
  public LastNotificationDao createInterface() {
    return INSTANCE;
  }
}
