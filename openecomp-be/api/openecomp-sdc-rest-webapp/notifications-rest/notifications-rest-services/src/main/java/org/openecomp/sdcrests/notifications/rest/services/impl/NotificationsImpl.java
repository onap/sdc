package org.openecomp.sdcrests.notifications.rest.services.impl;

import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.types.LoggerServiceName;
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
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Avrahamg
 * @since June 22, 2017
 */
@Named
@Service("notifications")
@Scope(value = "prototype")
public class NotificationsImpl implements Notifications {

	private static int selectionLimit = 10;

	private static final Logger LOGGER = LoggerFactory.getLogger(NotificationsImpl.class);
	private NotificationsService notificationsService = NotificationsServiceFactory.getInstance().createInterface();

    @Override
    public Response getNotifications(String user, UUID lastDelivered, UUID endOfPage) {
        MdcUtil.initMdc(LoggerServiceName.notifications.toString());
        NotificationsStatus notificationsStatus = notificationsService
            .getNotificationsStatus(user, lastDelivered, selectionLimit, endOfPage);
        MapNotificationsStatusToDto converter = new MapNotificationsStatusToDto();
        NotificationsStatusDto notificationsStatusDto = new NotificationsStatusDto();
        converter.doMapping(notificationsStatus, notificationsStatusDto);

        return Response.ok(notificationsStatusDto).build();
    }

    @Override
    public Response updateLastSeenNotification(String notificationId, String user)
        throws InvocationTargetException, IllegalAccessException {
        UpdateNotificationResponseStatus
            updateNotificationResponseStatus = new UpdateNotificationResponseStatus();
        try {
            notificationsService.updateLastSeenNotification(user, UUID.fromString(notificationId));
        } catch (Exception ex) {
            LOGGER.error(
                String.format(Messages.FAILED_TO_UPDATE_LAST_SEEN_NOTIFICATION.getErrorMessage(),
                    user), ex);
            updateNotificationResponseStatus.addStructureError(notificationId,
                new ErrorMessage(ErrorLevel.ERROR,
                    Messages.FAILED_TO_UPDATE_LAST_SEEN_NOTIFICATION.getErrorMessage()));
        }
        return Response.ok(updateNotificationResponseStatus).build();
    }

    @Override
    public Response markAsRead(String notificationId, String user)
        throws InvocationTargetException, IllegalAccessException {

        UpdateNotificationResponseStatus
            updateNotificationResponseStatus = new UpdateNotificationResponseStatus();
        try {
            notificationsService.markAsRead(user, notificationId);
        } catch (NotificationNotExistException ex) {
            LOGGER.error(Messages.FAILED_TO_MARK_NOTIFICATION_AS_READ.getErrorMessage(), ex);
            updateNotificationResponseStatus.addStructureError(
                notificationId, new ErrorMessage(ErrorLevel.ERROR, Messages
                    .FAILED_TO_MARK_NOTIFICATION_AS_READ
                    .getErrorMessage()));
        }
        return Response.ok(updateNotificationResponseStatus).build();
    }

    @Override
    public Response getNewNotificationsByOwnerId(String user, String eventId, String limitStr) {
        MdcUtil.initMdc(LoggerServiceName.notifications.toString());

        int limit = selectionLimit;

        if (Objects.nonNull(limitStr)) {
            try {
                limit = Integer.parseInt(limitStr);
            }
	    catch (NumberFormatException f) {
                LOGGER.error("Non numeric selection list size value specified: " + limitStr);
            }
        }

        List<NotificationEntity> notifications = Objects.isNull(eventId)
            ? notificationsService.getNotificationsByOwnerId(user, limit)
            : notificationsService.getNewNotificationsByOwnerId(user, UUID.fromString(eventId), limit);

        MapNotificationsToDto converter = new MapNotificationsToDto();
        NotificationsStatusDto notificationsStatusDto = new NotificationsStatusDto();
        converter.doMapping(notifications, notificationsStatusDto);

        return Response.ok(notificationsStatusDto).build();
    }
}
