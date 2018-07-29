package org.openecomp.sdc.be.components.distribution.engine;

import com.att.aft.dme2.api.DME2Exception;
import com.att.aft.dme2.api.DME2Manager;
import com.att.aft.dme2.iterator.DME2EndpointIterator;
import com.att.aft.dme2.iterator.factory.DME2EndpointIteratorFactory;
import org.springframework.stereotype.Component;

@Component
public class DME2EndpointIteratorCreator {

    public DME2EndpointIterator create(String lookupURI) throws DME2Exception {
        // Initializing DME2Manager instance
        DME2Manager manager = DME2Manager.getDefaultInstance();
        // Returning an instance of the DME2EndpointIteratorFactory
        return (DME2EndpointIterator) DME2EndpointIteratorFactory.getInstance().getIterator(lookupURI, null, null, manager);
    }
}
