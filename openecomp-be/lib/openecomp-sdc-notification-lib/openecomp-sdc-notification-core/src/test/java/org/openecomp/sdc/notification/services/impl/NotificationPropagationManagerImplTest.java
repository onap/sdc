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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
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
        MockitoAnnotations.openMocks(this);
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
