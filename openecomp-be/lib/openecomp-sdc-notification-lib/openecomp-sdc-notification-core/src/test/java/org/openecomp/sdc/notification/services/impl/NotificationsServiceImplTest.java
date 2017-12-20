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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        MockitoAnnotations.initMocks(this);
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