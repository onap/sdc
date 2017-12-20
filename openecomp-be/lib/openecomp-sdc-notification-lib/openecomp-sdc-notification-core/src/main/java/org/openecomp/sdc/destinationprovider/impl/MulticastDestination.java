package org.openecomp.sdc.destinationprovider.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.openecomp.sdc.destinationprovider.DestinationProvider;
import org.openecomp.sdc.notification.services.SubscriptionService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author avrahamg
 * @since July 09, 2017
 */
public class MulticastDestination implements DestinationProvider {

    private String entityId;
    private SubscriptionService subscriptionService;
    private String[] excludedSubscribers;

    public MulticastDestination(String entityId, SubscriptionService subscriptionService,
                                String... excludedSubscribers) {
        this.entityId = entityId;
        this.excludedSubscribers = excludedSubscribers;
        this.subscriptionService = subscriptionService;
    }

    public List<String> getSubscribers() {
        ArrayList<String> subscribers = new ArrayList<>(subscriptionService.getSubscribers(entityId));
        if (ArrayUtils.isNotEmpty(excludedSubscribers)) {
            subscribers.removeAll(Arrays.asList(excludedSubscribers));
        }
        return Collections.unmodifiableList(subscribers);
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
