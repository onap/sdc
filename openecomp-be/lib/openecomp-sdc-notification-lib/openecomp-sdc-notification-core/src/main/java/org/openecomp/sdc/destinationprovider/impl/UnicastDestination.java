package org.openecomp.sdc.destinationprovider.impl;

import org.openecomp.sdc.destinationprovider.DestinationProvider;

import java.util.Collections;
import java.util.List;

/**
 * @author avrahamg
 * @since July 09, 2017
 */
public class UnicastDestination implements DestinationProvider {

    private String originatorId;

    public UnicastDestination(String originatorId) {
        this.originatorId = originatorId;
    }

    public List<String> getSubscribers() {
        return Collections.unmodifiableList(Collections.singletonList(originatorId));
    }
}
