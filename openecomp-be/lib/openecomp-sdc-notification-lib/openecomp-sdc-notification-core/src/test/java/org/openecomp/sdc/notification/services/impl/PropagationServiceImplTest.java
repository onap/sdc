package org.openecomp.sdc.notification.services.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.destinationprovider.DestinationProvider;
import org.openecomp.sdc.notification.dao.NotificationsDao;
import org.openecomp.sdc.notification.dtos.Event;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author avrahamg
 * @since July 13, 2017
 */
public class PropagationServiceImplTest {
    @Mock
    private NotificationsDao notificationsDaoMock;
    @Mock
    private Event eventMock;
    @Mock
    private DestinationProvider destinationProviderMock;
    @Captor
    private ArgumentCaptor<List> createBatchCaptor;

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @InjectMocks
    @Spy
    private PropagationServiceImpl propagationService;
    private List<String> subscribersList = Arrays.asList("A1, A2, A3");;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        initEventMock();
    }

    @Test
    public void shouldCallToNotificationsDaoWithCreateBatchWithNotificationEntitiesAsNumberOfSubscribers()
        throws Exception {
        doReturn(subscribersList).when(destinationProviderMock).getSubscribers();
        propagationService.notify(eventMock, destinationProviderMock);
        verify(notificationsDaoMock).createBatch(createBatchCaptor.capture());
        Assert.assertEquals(createBatchCaptor.getValue().size(), subscribersList.size());
    }

    @Test
    public void shouldNotCallNotificationDaoIfSubscriberIsNull() throws Exception {
        doReturn(Collections.EMPTY_LIST).when(destinationProviderMock).getSubscribers();
        verify(notificationsDaoMock,never()).createBatch(anyList());
    }

    @Test
    public void shouldThrowExceptionIfEventTypeIsNull() throws Exception {
        doReturn(null).when(eventMock).getEventType();
        callToNotify();
    }

    @Test
    public void shouldThrowExceptionIfOriginatorIdIsNull() throws Exception {
        doReturn(null).when(eventMock).getOriginatorId();
        callToNotify();
    }

    private void callToNotify() {
        thrown.expect(NullPointerException.class);
        propagationService.notify(eventMock, destinationProviderMock);
    }

    private void initEventMock() {
        doReturn("eventType").when(eventMock).getEventType();
        doReturn("originator").when(eventMock).getOriginatorId();
        doReturn("entity").when(eventMock).getEntityId();
        doReturn(new HashMap<>()).when(eventMock).getAttributes();
    }


}