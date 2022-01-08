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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.notification.dao.LastNotificationDao;
import org.openecomp.sdc.notification.dao.NotificationsDao;
import org.openecomp.sdc.notification.dao.types.NotificationEntity;
import org.openecomp.sdc.notification.exceptons.NotificationNotExistException;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author avrahamg
 * @since July 13, 2017
 */
public class NotificationsServiceImplTest {
    @Mock
    private LastNotificationDao lastNotificationDao;
    @Mock
    private NotificationsDao notificationsDao;
    @Spy
    @InjectMocks
    private NotificationsServiceImpl notificationsService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    public void shouldCallNotificationsDaoIfNotificationEntityExist() throws Exception {
        doReturn(new NotificationEntity()).when(notificationsDao).get(any());
        notificationsService.markAsRead("ownerId", UUID.randomUUID().toString());
        verify(notificationsDao, times(1)).update(any());
    }

    @Test(expected = NotificationNotExistException.class)
    public void shouldThrowExceptionIfOwnerIdAndNotificationIdDontRelate() throws Exception {
        doReturn(null).when(notificationsDao).get(any());
        notificationsService.markAsRead("ownerId", UUID.randomUUID().toString());
    }
}
