package org.openecomp.sdc.notification.services.impl;

import org.openecomp.sdc.notification.dao.LastNotificationDao;
import org.openecomp.sdc.notification.dao.NotificationsDao;
import org.openecomp.sdc.notification.dao.types.LastSeenNotificationEntity;
import org.openecomp.sdc.notification.dao.types.NotificationEntity;
import org.openecomp.sdc.notification.dtos.NotificationsStatus;
import org.openecomp.sdc.notification.exceptons.NotificationNotExistException;
import org.openecomp.sdc.notification.services.NotificationsService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Avrahamg
 * @since June 26, 2017
 */
public class NotificationsServiceImpl implements NotificationsService {

    private LastNotificationDao lastNotificationDao;
    private NotificationsDao notificationsDao;

    public NotificationsServiceImpl(LastNotificationDao lastNotificationDao,
                                    NotificationsDao notificationsDao) {
        this.lastNotificationDao = lastNotificationDao;
        this.notificationsDao = notificationsDao;
    }

    @Override
    public LastSeenNotificationEntity getLastNotification(String ownerId) {
        return new LastSeenNotificationEntity(ownerId,
            lastNotificationDao.getOwnerLastEventId(ownerId));
    }

    @Override
    public void updateLastSeenNotification(String ownerId, UUID eventId)
    {
        lastNotificationDao.persistOwnerLastEventId(ownerId, eventId);
    }

    @Override
    public NotificationsStatus getNotificationsStatus(String ownerId, UUID lastDelivered, int numOfRecordsToReturn, UUID endOfPage) {
	if (Objects.isNull(lastDelivered)) {
		LastSeenNotificationEntity entity = getLastNotification(ownerId);
		if (Objects.nonNull(entity)) {
			lastDelivered = entity.getLastEventId();
		}
		if (Objects.isNull(lastDelivered)) {
			lastDelivered = UUID.fromString("00000000-0000-1000-8080-808080808080"); // Lowest time UUID value
		}
	}
	if (Objects.isNull(endOfPage)) {
		// First page
	        return notificationsDao.getNotificationsStatus(ownerId, lastDelivered, numOfRecordsToReturn);
	}
	else {
		// Next page
            return notificationsDao.getNotificationsStatus(ownerId, lastDelivered, numOfRecordsToReturn, endOfPage);
	}
    }

    @Override
    public void markAsRead(String ownerId, String notificationId) throws
        NotificationNotExistException {
        NotificationEntity notificationEntity =
            notificationsDao.get(new NotificationEntity(ownerId, UUID.fromString(notificationId)));
        if (Objects.isNull(notificationEntity)) {
            throw new NotificationNotExistException(
                "Notification '" + notificationId + "' is not related to ownerId" +
                    " '" + ownerId + "'");
        }
        notificationEntity.setRead(true);
        notificationsDao.update(notificationEntity);
    }

    @Override
    public List<NotificationEntity> getNotificationsByOwnerId(String ownerId, int limit) {
	return notificationsDao.getNotificationsByOwnerId(ownerId, limit);
    }

    @Override
    public List<NotificationEntity> getNewNotificationsByOwnerId(String ownerId, UUID eventId) {
	return notificationsDao.getNewNotificationsByOwnerId(ownerId, eventId);
    }

    @Override
    public List<NotificationEntity> getNewNotificationsByOwnerId(String ownerId, UUID eventId, int limit) {
	return notificationsDao.getNewNotificationsByOwnerId(ownerId, eventId, limit);
    }

}
