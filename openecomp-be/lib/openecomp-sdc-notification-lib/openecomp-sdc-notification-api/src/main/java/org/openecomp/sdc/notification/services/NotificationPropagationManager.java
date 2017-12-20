package org.openecomp.sdc.notification.services;

import org.openecomp.sdc.notification.dtos.Event;

/**
 * @author avrahamg
 * @since July 09, 2017
 */
public interface NotificationPropagationManager {
    void notifySubscribers(Event event, String ... excludedSubscribers);
    void directNotification(Event event, String destinationId);
}
