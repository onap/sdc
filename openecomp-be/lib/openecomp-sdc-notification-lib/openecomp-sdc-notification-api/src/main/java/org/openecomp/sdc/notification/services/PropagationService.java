package org.openecomp.sdc.notification.services;

import org.openecomp.sdc.destinationprovider.DestinationProvider;
import org.openecomp.sdc.notification.dtos.Event;

public interface PropagationService {

    void notify(Event event, DestinationProvider destinationProvider);
}
