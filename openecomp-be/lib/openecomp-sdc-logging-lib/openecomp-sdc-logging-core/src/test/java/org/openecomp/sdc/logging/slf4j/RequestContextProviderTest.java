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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import org.junit.Test;
import org.openecomp.sdc.logging.api.ContextData;

import static org.junit.Assert.*;

/**
 * Unit-test retrieving values from client-provided request data.
 *
 * @author evitaliy
 * @since 23 Mar 2018
 */
public class RequestContextProviderTest {

    @Test
    public void valuesEmptyWhenInputDataEmpty() {
        RequestContextProvider provider = RequestContextProvider.from(ContextData.builder().build());
        assertTrue(provider.values().isEmpty());
    }

    @Test
    public void serviceNameReturnedWhenSuppliedInData() {
        final String service = "supplied-service-name";
        RequestContextProvider provider =
                RequestContextProvider.from(ContextData.builder().serviceName(service).build());
        assertEquals(service, provider.values().get(ContextField.SERVICE_NAME));
    }

    @Test
    public void partnerNameReturnedWhenSuppliedInData() {
        final String partner = "supplied-partner-name";
        RequestContextProvider provider =
                RequestContextProvider.from(ContextData.builder().partnerName(partner).build());
        assertEquals(partner, provider.values().get(ContextField.PARTNER_NAME));
    }

    @Test
    public void requestIdReturnedWhenSuppliedInData() {
        final String request = "supplied-request-id";
        RequestContextProvider provider =
                RequestContextProvider.from(ContextData.builder().requestId(request).build());
        assertEquals(request, provider.values().get(ContextField.REQUEST_ID));
    }

    @Test
    public void dataEmptyWhenValuesEmpty() {
        ContextData data = RequestContextProvider.to(Collections.emptyMap());
        assertNull(data.getPartnerName());
        assertNull(data.getRequestId());
        assertNull(data.getServiceName());
    }

    @Test
    public void serviceNameInDataWhenSuppliedInValues() {
        final String service = "values-service-name";
        Map<ContextField, String> values = new EnumMap<>(ContextField.class);
        values.put(ContextField.SERVICE_NAME, service);
        ContextData data = RequestContextProvider.to(values);
        assertEquals(data.getServiceName(), service);
    }

    @Test
    public void partnerNameInDataWhenSuppliedInValues() {
        final String partner = "values-partner-name";
        Map<ContextField, String> values = new EnumMap<>(ContextField.class);
        values.put(ContextField.PARTNER_NAME, partner);
        ContextData data = RequestContextProvider.to(values);
        assertEquals(data.getPartnerName(), partner);
    }

    @Test
    public void requestIdInDataWhenSuppliedInValues() {
        final String request = "values-request-id";
        Map<ContextField, String> values = new EnumMap<>(ContextField.class);
        values.put(ContextField.REQUEST_ID, request);
        ContextData data = RequestContextProvider.to(values);
        assertEquals(data.getRequestId(), request);
    }
}
