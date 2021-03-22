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

import org.openecomp.sdc.destinationprovider.impl.MulticastDestination;
import org.openecomp.sdc.destinationprovider.impl.UnicastDestination;
import org.openecomp.sdc.notification.dtos.Event;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.notification.services.PropagationService;
import org.openecomp.sdc.notification.services.SubscriptionService;

/**
 * @author avrahamg
 * @since July 10, 2017
 */
public class NotificationPropagationManagerImpl implements NotificationPropagationManager {

    private PropagationService propagationService;
    private SubscriptionService subscriptionService;

    public NotificationPropagationManagerImpl(PropagationService propagationService, SubscriptionService subscriptionService) {
        this.propagationService = propagationService;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public void notifySubscribers(Event event, String... excludedSubscribers) {
        propagationService.notify(event, new MulticastDestination(event.getEntityId(), subscriptionService, excludedSubscribers));
    }

    @Override
    public void directNotification(Event event, String destinationId) {
        propagationService.notify(event, new UnicastDestination(destinationId));
    }
}
