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

    public NotificationPropagationManagerImpl(PropagationService propagationService,
                                              SubscriptionService subscriptionService) {
        this.propagationService = propagationService;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public void notifySubscribers(Event event, String ... excludedSubscribers) {
        propagationService.notify(event, new MulticastDestination(event.getEntityId(),
            subscriptionService, excludedSubscribers));
    }

    @Override
    public void directNotification(Event event, String destinationId) {
        propagationService.notify(event, new UnicastDestination(destinationId));
    }
}
