/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.notification.services.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.openecomp.sdc.notification.dao.LastNotificationDao;
import org.openecomp.sdc.notification.dao.NotificationsDao;
import org.openecomp.sdc.notification.dao.types.LastSeenNotificationEntity;
import org.openecomp.sdc.notification.dao.types.NotificationEntity;
import org.openecomp.sdc.notification.dtos.NotificationsStatus;
import org.openecomp.sdc.notification.exceptons.NotificationNotExistException;
import org.openecomp.sdc.notification.services.NotificationsService;

/**
 * @author Avrahamg
 * @since June 26, 2017
 */
public class NotificationsServiceImpl implements NotificationsService {

    private LastNotificationDao lastNotificationDao;
    private NotificationsDao notificationsDao;

    public NotificationsServiceImpl(LastNotificationDao lastNotificationDao, NotificationsDao notificationsDao) {
        this.lastNotificationDao = lastNotificationDao;
        this.notificationsDao = notificationsDao;
    }

    @Override
    public LastSeenNotificationEntity getLastNotification(String ownerId) {
        return new LastSeenNotificationEntity(ownerId, lastNotificationDao.getOwnerLastEventId(ownerId));
    }

    @Override
    public void updateLastSeenNotification(String ownerId, UUID eventId) {
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
        } else {
            // Next page
            return notificationsDao.getNotificationsStatus(ownerId, lastDelivered, numOfRecordsToReturn, endOfPage);
        }
    }

    @Override
    public void markAsRead(String ownerId, String notificationId) throws NotificationNotExistException {
        NotificationEntity notificationEntity = notificationsDao.get(new NotificationEntity(ownerId, UUID.fromString(notificationId)));
        if (Objects.isNull(notificationEntity)) {
            throw new NotificationNotExistException("Notification '" + notificationId + "' is not related to ownerId" + " '" + ownerId + "'");
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
