package org.openecomp.sdc.destinationprovider.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.notification.services.SubscriptionService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

/**
 * @author avrahamg
 * @since July 13, 2017
 */
public class MulticastDestinationTest {
    @Mock
    private SubscriptionService subscriptionServiceMock;

    private final String excludedSubscriber = "excluded";
    private Set<String> subscribers = new HashSet<>(Arrays.asList("a", "b", excludedSubscriber));
    private MulticastDestination multicastDestination;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldReturnAllSubscribersIfNoExcludedProvided() throws Exception {
        doReturn(subscribers).when(subscriptionServiceMock).getSubscribers(any());
        multicastDestination = new MulticastDestination("aa", subscriptionServiceMock);
        assertEquals(subscribers.size(), multicastDestination.getSubscribers().size());
        List<String> actualSubscribers = multicastDestination.getSubscribers();
        assertTrue(actualSubscribers.containsAll(subscribers));
    }

    @Test
    public void shouldReturnAllSubscribersExceptExcluded() throws Exception {
        doReturn(subscribers).when(subscriptionServiceMock).getSubscribers(any());
        multicastDestination =
            new MulticastDestination("aa", subscriptionServiceMock, excludedSubscriber);
        List<String> actualSubscribers = multicastDestination.getSubscribers();
        assertNotEquals(this.subscribers.size(), actualSubscribers.size());
        assertFalse(actualSubscribers.containsAll(subscribers));
        assertFalse(actualSubscribers.contains(excludedSubscriber));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowUnsupportedOperationExceptionWhenTryingToChangeSubscribersList() throws
        Exception {
        doReturn(subscribers).when(subscriptionServiceMock).getSubscribers(any());
        multicastDestination =
            new MulticastDestination("aa", subscriptionServiceMock, excludedSubscriber);
        List<String> actualSubscribers = multicastDestination.getSubscribers();
        actualSubscribers.add("sss");
    }
}