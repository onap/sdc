package org.openecomp.sdc.notification.services;

import org.openecomp.sdc.notification.dao.types.LastSeenNotificationEntity;
import org.openecomp.sdc.notification.dao.types.NotificationEntity;
import org.openecomp.sdc.notification.dtos.NotificationsStatus;
import org.openecomp.sdc.notification.exceptons.NotificationNotExistException;

import java.util.List;
import java.util.UUID;

/**
 * @author Avrahamg
 * @since June 22, 2017
 */
public interface NotificationsService {

	LastSeenNotificationEntity getLastNotification(String ownerId);

	NotificationsStatus getNotificationsStatus(String ownerId, UUID lastDelivered, int numOfRecordsToReturn, UUID endOfPage);

	void updateLastSeenNotification(String ownerId, UUID eventId);

	void markAsRead(String ownerId, String notificationId) throws NotificationNotExistException;

    List<NotificationEntity> getNotificationsByOwnerId(String ownerId, int limit);

    List<NotificationEntity> getNewNotificationsByOwnerId(String ownerId, UUID eventId);

    List<NotificationEntity> getNewNotificationsByOwnerId(String ownerId, UUID eventId, int limit);

}
