package org.openecomp.sdc.destinationprovider;

import java.util.List;

/**
 * @author avrahamg
 * @since July 09, 2017
 */
public interface DestinationProvider {
    List<String> getSubscribers();
}
