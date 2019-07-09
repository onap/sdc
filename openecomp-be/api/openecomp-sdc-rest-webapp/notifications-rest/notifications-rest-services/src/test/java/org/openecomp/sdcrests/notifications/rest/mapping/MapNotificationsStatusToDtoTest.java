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

/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdcrests.notifications.rest.mapping;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.notification.dtos.NotificationsStatus;
import org.openecomp.sdcrests.notifications.types.NotificationsStatusDto;

public class MapNotificationsStatusToDtoTest {

    @Test()
    public void testConversion() {

        final NotificationsStatus source = Mockito.mock(NotificationsStatus.class);

        final UUID lastScanned = UUID.randomUUID();
        Mockito.when(source.getLastScanned()).thenReturn(lastScanned);

        final List<UUID> newEntries = Collections.singletonList(UUID.randomUUID());
        Mockito.when(source.getNewEntries()).thenReturn(newEntries);

        final UUID endOfPage = UUID.randomUUID();
        Mockito.when(source.getEndOfPage()).thenReturn(endOfPage);

        final long numOfNotSeenNotifications = 499436903074L;
        Mockito.when(source.getNumOfNotSeenNotifications()).thenReturn(numOfNotSeenNotifications);

        final NotificationsStatusDto target = new NotificationsStatusDto();
        final MapNotificationsStatusToDto mapper = new MapNotificationsStatusToDto();
        mapper.doMapping(source, target);

        assertEquals(lastScanned, target.getLastScanned());
        assertEquals(newEntries, target.getNewEntries());
        assertEquals(endOfPage, target.getEndOfPage());
        assertEquals(numOfNotSeenNotifications, target.getNumOfNotSeenNotifications());
    }
}
