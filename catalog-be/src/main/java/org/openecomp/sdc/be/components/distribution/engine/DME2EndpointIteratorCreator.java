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

package org.openecomp.sdc.be.components.distribution.engine;

import com.att.aft.dme2.api.DME2Exception;
import com.att.aft.dme2.api.DME2Manager;
import com.att.aft.dme2.iterator.DME2EndpointIterator;
import com.att.aft.dme2.iterator.factory.DME2EndpointIteratorFactory;
import org.springframework.stereotype.Component;

@Component
public class DME2EndpointIteratorCreator {

    public DME2EndpointIterator create(String lookupURI) throws DME2Exception {
        // Returning an instance of the DME2EndpointIteratorFactory
        return (DME2EndpointIterator) DME2EndpointIteratorFactory.getInstance().getIterator(lookupURI, null, null);
    }
}
