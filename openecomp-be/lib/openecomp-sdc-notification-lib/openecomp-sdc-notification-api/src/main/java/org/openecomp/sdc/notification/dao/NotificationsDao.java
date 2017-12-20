/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.notification.dao;

import org.openecomp.core.dao.BaseDao;
import org.openecomp.sdc.notification.dao.types.NotificationEntity;
import org.openecomp.sdc.notification.dtos.NotificationsStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;


public interface NotificationsDao extends BaseDao<NotificationEntity> {

    int DEFAULT_LIMIT_OF_RESULTS_FOR_OWNER_NOTIFICATIONS = 1000;

    List<NotificationEntity> getNotificationsByOwnerId(String ownerId, int limit);

    List<NotificationEntity> getNewNotificationsByOwnerId(String ownerId, UUID eventId);

    List<NotificationEntity> getNewNotificationsByOwnerId(String ownerId, UUID eventId, int limit);

    NotificationsStatus getNotificationsStatus(String ownerId, UUID lastSeenNotification, int numOfRecordsToReturn);

    NotificationsStatus getNotificationsStatus(String ownerId, UUID lastSeenNotification, int numOfRecordsToReturn, UUID prevLastNotification);

	void markNotificationAsRead(String ownerId, Collection<UUID> eventIds);

    void createBatch(List<NotificationEntity> notificationEntities);
}
