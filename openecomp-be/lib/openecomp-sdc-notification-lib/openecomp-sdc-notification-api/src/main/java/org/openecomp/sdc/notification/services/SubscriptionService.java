package org.openecomp.sdc.notification.services;

import java.util.Set;

/**
 * @author avrahamg
 * @since July 09, 2017
 */
public interface SubscriptionService {
    void subscribe(String ownerId, String entityId);

    void unsubscribe(String ownerId, String entityId);

    Set<String> getSubscribers(String entityId);
}
