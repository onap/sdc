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

package org.openecomp.sdcrests.notifications.rest.mapping;

import com.datastax.driver.core.utils.UUIDs;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.notification.dao.types.NotificationEntity;
import org.openecomp.sdc.notification.dtos.NotificationsStatus;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.notifications.types.NotificationEntityDto;
import org.openecomp.sdcrests.notifications.types.NotificationsStatusDto;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapNotificationsStatusToDto
    extends MappingBase<NotificationsStatus, NotificationsStatusDto> {

    private static final DateFormat formatter =
        DateFormat.getDateTimeInstance(DateFormat.LONG,
            DateFormat.SHORT);

    @Override
    public void doMapping(NotificationsStatus source, NotificationsStatusDto target) {

        target.setLastScanned(source.getLastScanned());
        target.setNewEntries(source.getNewEntries());
        target.setEndOfPage(source.getEndOfPage());
        target.setNumOfNotSeenNotifications(source.getNumOfNotSeenNotifications());
        List<NotificationEntityDto> entityDtos = new ArrayList<>();
        source.getNotifications()
            .forEach(notification -> entityDtos.add(new NotificationEntityDto(notification.isRead(),
                notification.getEventId(), notification.getEventType(),
                JsonUtil.json2Object(notification.getEventAttributes(), Map.class),
                extractDate(notification))));
        target.setNotifications(entityDtos);
    }

    private String extractDate(NotificationEntity notification) {
        return formatter.format(UUIDs.unixTimestamp
            (notification
                .getEventId()));
    }
}
