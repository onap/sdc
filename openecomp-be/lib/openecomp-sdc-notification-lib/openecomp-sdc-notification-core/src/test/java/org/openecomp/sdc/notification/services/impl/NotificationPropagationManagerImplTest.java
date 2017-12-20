package org.openecomp.sdc.notification.services.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.destinationprovider.DestinationProvider;
import org.openecomp.sdc.destinationprovider.impl.MulticastDestination;
import org.openecomp.sdc.destinationprovider.impl.UnicastDestination;
import org.openecomp.sdc.notification.dtos.Event;
import org.openecomp.sdc.notification.services.PropagationService;
import org.openecomp.sdc.notification.services.SubscriptionService;

import static org.mockito.Mockito.verify;


/**
 * @author avrahamg
 * @since July 13, 2017
 */
public class NotificationPropagationManagerImplTest {
    @Mock
    private PropagationService propagationServiceMock;
    @Mock
    private SubscriptionService subscriptionServiceMock;
    @Mock
    private Event eventMock;
    @Captor
    private ArgumentCaptor<DestinationProvider> destinationProviderCaptor;

    @Spy
    @InjectMocks
    private NotificationPropagationManagerImpl notificationPropagationManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCallPropagationServiceNotifyWithMulticastDestinationWhenNotifySubscribers()
        throws Exception {
        notificationPropagationManager.notifySubscribers(eventMock);
        verify(propagationServiceMock).notify(Matchers.eq(eventMock), destinationProviderCaptor
            .capture());
        Assert.assertTrue(destinationProviderCaptor.getValue() instanceof MulticastDestination);

    }

    @Test
    public void shouldCallPropagationServiceNotifyWithUnicastDestinationWhenDirectNotification()
        throws Exception {
        notificationPropagationManager.directNotification(eventMock, "aaa");
        verify(propagationServiceMock).notify(Matchers.eq(eventMock), destinationProviderCaptor
            .capture());
        Assert.assertTrue(destinationProviderCaptor.getValue() instanceof UnicastDestination);
    }
}