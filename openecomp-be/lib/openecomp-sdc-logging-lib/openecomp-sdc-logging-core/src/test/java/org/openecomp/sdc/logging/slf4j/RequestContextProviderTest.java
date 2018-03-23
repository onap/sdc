/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.logging.slf4j;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.openecomp.sdc.logging.api.ContextData;
import org.testng.annotations.Test;

/**
 * Unit-test retrieving values from client-provided request data.
 *
 * @author evitaliy
 * @since 23 Mar 2018
 */
public class RequestContextProviderTest {

    @Test
    public void valuesEmptyWhenInputEmpty() {
        RequestContextProvider provider = new RequestContextProvider(ContextData.builder().build());
        assertTrue(provider.values().isEmpty());
    }

    @Test
    public void serviceNameReturnedWhenSupplied() {
        final String service = "supplied-service-name";
        RequestContextProvider provider =
                new RequestContextProvider(ContextData.builder().serviceName(service).build());
        assertEquals(provider.values().get(ContextField.SERVICE_NAME), service);
    }

    @Test
    public void partnerNameReturnedWhenSupplied() {
        final String partner = "supplied-partner-name";
        RequestContextProvider provider =
                new RequestContextProvider(ContextData.builder().partnerName(partner).build());
        assertEquals(provider.values().get(ContextField.PARTNER_NAME), partner);
    }

    @Test
    public void requestIdReturnedWhenSupplied() {
        final String request = "supplied-request-id";
        RequestContextProvider provider =
                new RequestContextProvider(ContextData.builder().requestId(request).build());
        assertEquals(provider.values().get(ContextField.REQUEST_ID), request);
    }
}
