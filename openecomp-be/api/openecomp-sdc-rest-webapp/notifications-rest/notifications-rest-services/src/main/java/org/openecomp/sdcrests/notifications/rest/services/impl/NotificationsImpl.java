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
package org.openecomp.sdcrests.notifications.rest.services.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.inject.Named;

import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.notification.dao.types.NotificationEntity;
import org.openecomp.sdc.notification.dtos.NotificationsStatus;
import org.openecomp.sdc.notification.exceptons.NotificationNotExistException;
import org.openecomp.sdc.notification.factories.NotificationsServiceFactory;
import org.openecomp.sdc.notification.services.NotificationsService;
import org.openecomp.sdcrests.notifications.rest.mapping.MapNotificationsStatusToDto;
import org.openecomp.sdcrests.notifications.rest.mapping.MapNotificationsToDto;
import org.openecomp.sdcrests.notifications.rest.services.Notifications;
import org.openecomp.sdcrests.notifications.types.NotificationsStatusDto;
import org.openecomp.sdcrests.notifications.types.UpdateNotificationResponseStatus;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * @author Avrahamg
 * @since June 22, 2017
 */
@Named
@Service("notifications")
@Scope(value = "prototype")
public class NotificationsImpl implements Notifications {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationsImpl.class);
    private static int selectionLimit = 10;
    private NotificationsService notificationsService = NotificationsServiceFactory.getInstance().createInterface();

    @Override
    public ResponseEntity getNotifications(String user, UUID lastDelivered, UUID endOfPage) {
        NotificationsStatus notificationsStatus = notificationsService.getNotificationsStatus(user, lastDelivered, selectionLimit, endOfPage);
        MapNotificationsStatusToDto converter = new MapNotificationsStatusToDto();
        NotificationsStatusDto notificationsStatusDto = new NotificationsStatusDto();
        converter.doMapping(notificationsStatus, notificationsStatusDto);
        return ResponseEntity.ok(notificationsStatusDto);
    }

    @Override
    public ResponseEntity updateLastSeenNotification(String notificationId, String user) throws InvocationTargetException, IllegalAccessException {
        UpdateNotificationResponseStatus updateNotificationResponseStatus = new UpdateNotificationResponseStatus();
        try {
            notificationsService.updateLastSeenNotification(user, UUID.fromString(notificationId));
        } catch (Exception ex) {
            LOGGER.error(String.format(Messages.FAILED_TO_UPDATE_LAST_SEEN_NOTIFICATION.getErrorMessage(), user), ex);
            updateNotificationResponseStatus.addStructureError(notificationId,
                new ErrorMessage(ErrorLevel.ERROR, Messages.FAILED_TO_UPDATE_LAST_SEEN_NOTIFICATION.getErrorMessage()));
        }
        return ResponseEntity.ok(updateNotificationResponseStatus);
    }

    @Override
    public ResponseEntity markAsRead(String notificationId, String user) throws InvocationTargetException, IllegalAccessException {
        UpdateNotificationResponseStatus updateNotificationResponseStatus = new UpdateNotificationResponseStatus();
        try {
            notificationsService.markAsRead(user, notificationId);
        } catch (NotificationNotExistException ex) {
            LOGGER.error(Messages.FAILED_TO_MARK_NOTIFICATION_AS_READ.getErrorMessage(), ex);
            updateNotificationResponseStatus.addStructureError(notificationId,
                new ErrorMessage(ErrorLevel.ERROR, Messages.FAILED_TO_MARK_NOTIFICATION_AS_READ.getErrorMessage()));
        }
        return ResponseEntity.ok(updateNotificationResponseStatus);
    }

   @Override
public ResponseEntity getNewNotificationsByOwnerId(String user, String eventId, String limitStr) {
    LOGGER.info("===> Received request: user={}, eventId={}, limitStr={}", user, eventId, limitStr);
    int limit = selectionLimit;
    if (Objects.nonNull(limitStr)) {
        try {
            limit = Integer.parseInt(limitStr);
        } catch (NumberFormatException f) {
            LOGGER.error("Non numeric selection list size value specified: {}", limitStr, f);
        }
    }

    try {
        List<NotificationEntity> notifications;
        if (Objects.isNull(eventId)) {
            LOGGER.info("Calling notificationsService.getNotificationsByOwnerId()");
            notifications = notificationsService.getNotificationsByOwnerId(user, limit);
        } else {
            UUID uuid = UUID.fromString(eventId); 
            LOGGER.info("Calling notificationsService.getNewNotificationsByOwnerId()");
            notifications = notificationsService.getNewNotificationsByOwnerId(user, uuid, limit);
        }

        MapNotificationsToDto converter = new MapNotificationsToDto();
        NotificationsStatusDto notificationsStatusDto = new NotificationsStatusDto();
        converter.doMapping(notifications, notificationsStatusDto);

        return ResponseEntity.ok(notificationsStatusDto);
    } catch (Exception e) {
        LOGGER.error("Exception occurred in getNewNotificationsByOwnerId", e);
        return ResponseEntity.ok("Exception: ");
    }
}

}
