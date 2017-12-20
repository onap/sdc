package org.openecomp.sdc.notification.dtos;

import org.openecomp.sdc.notification.dao.types.NotificationEntity;

import java.util.List;
import java.util.UUID;

/**
 * @author Avrahamg
 * @since June 26, 2017
 */
public interface NotificationsStatus {

  List<NotificationEntity> getNotifications();

  List<UUID> getNewEntries();

  UUID getLastScanned();

  UUID getEndOfPage();

  long getNumOfNotSeenNotifications();

}
