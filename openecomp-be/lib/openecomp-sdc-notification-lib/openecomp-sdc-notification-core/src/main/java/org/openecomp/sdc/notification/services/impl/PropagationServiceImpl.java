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

package org.openecomp.sdc.notification.services.impl;

import com.datastax.driver.core.utils.UUIDs;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.destinationprovider.DestinationProvider;
import org.openecomp.sdc.notification.dao.NotificationsDao;
import org.openecomp.sdc.notification.dao.types.NotificationEntity;
import org.openecomp.sdc.notification.dtos.Event;
import org.openecomp.sdc.notification.services.PropagationService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class PropagationServiceImpl implements PropagationService {

    private NotificationsDao notificationsDao;

    public PropagationServiceImpl(NotificationsDao notificationsDao) {
        this.notificationsDao = notificationsDao;
    }


    @Override
    public void notify(Event event, DestinationProvider destinationProvider) {
        requireNonNull(event.getEventType());
        requireNonNull(event.getOriginatorId());
        List<String> subscribers = destinationProvider.getSubscribers();
        if (CollectionUtils.isEmpty(subscribers)) {
            return;
        }
        List<NotificationEntity> notificationEntities = subscribers.stream().map(
            subscriber -> {
                UUID eventId = UUIDs.timeBased();
                return createNotificationEntity(event.getEventType(), subscriber,
                    event.getOriginatorId(), event.getAttributes(), eventId);
            }).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(notificationEntities)) {
            notificationsDao.createBatch(notificationEntities);
        }
    }

    private NotificationEntity createNotificationEntity(String eventType, String subscriber,
                                                        String originatorId,
                                                        Map<String, Object> attributes,
                                                        UUID eventId) {
        NotificationEntity notificationEntity =
            new NotificationEntity(subscriber, eventId, eventType, originatorId);
        if (attributes != null && !attributes.isEmpty()) {
            notificationEntity.setEventAttributes(JsonUtil.object2Json(attributes));
        }
        return notificationEntity;
    }
}