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

package org.openecomp.sdc.destinationprovider.impl;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.notification.services.SubscriptionService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Test
    public void entityIdTest() {
        doReturn(subscribers).when(subscriptionServiceMock).getSubscribers(any());
        multicastDestination =
                new MulticastDestination("aa", subscriptionServiceMock, excludedSubscriber);
        multicastDestination.setEntityId("entityId");
        assertEquals("entityId", multicastDestination.getEntityId());
    }
}
