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

package org.openecomp.sdc.logging.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import org.openecomp.sdc.logging.api.ContextData;

/**
 * Populating context from request data.
 *
 * @author evitaliy
 * @since 01 Aug 2018
 */
public class ContextTrackerTest {

    private static final String X_REQUEST_ID = "X-REQUEST-ID";
    private static final HttpHeader REQUEST_ID_HEADER = new HttpHeader(X_REQUEST_ID);

    private static final String X_PARTNER_NAME = "X-PARTNER-NAME";
    private static final HttpHeader PARTNER_NAME_HEADER = new HttpHeader(X_PARTNER_NAME);

    private static final String PROVIDER_PATH = "org.openecomp.sdc.logging.servlet.TestLogginContextService";

    private ContextData lastContext = ContextData.builder().build();

    private final Supplier<Void> loggingContextClear = () -> {
        lastContext = ContextData.builder().build();
        return null;
    };

    private final Consumer<ContextData> loggingContextPut = (contextData) -> lastContext = contextData;

    private final ContextTracker tracker = new ContextTracker(PARTNER_NAME_HEADER,
        REQUEST_ID_HEADER,
        loggingContextClear,
        loggingContextPut);

    @Test
    public void throwExceptionWhenPartnerNamesNull() {
        assertThrows(NullPointerException.class,
            () -> new ContextTracker(null, REQUEST_ID_HEADER));
    }

    @Test
    public void throwExceptionWhenRequestIdsNull() {
        assertThrows(NullPointerException.class,
            () -> new ContextTracker(PARTNER_NAME_HEADER, null));
    }

    @Test
    public void requestIdCopiedWhenGiven() {

        final String requestId = "request-id-for-unit-testing";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(X_REQUEST_ID)).thenReturn(requestId);

        tracker.preRequest(request);

        assertEquals(requestId, lastContext.getRequestId());
    }

    @Test
    public void requestIdGeneratedWhenNotGiven() {

        tracker.preRequest(mock(HttpServletRequest.class));

        String requestId = lastContext.getRequestId();
        assertNotNull(requestId);
        assertFalse(requestId.isEmpty());
    }

    @Test
    public void partnerNameCopiedWhenGiven() {

        final String partner = "partner-name-for-unit-testing";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(X_PARTNER_NAME)).thenReturn(partner);

        tracker.preRequest(request);

        assertEquals(partner, lastContext.getPartnerName());
    }

    @Test
    public void partnerNameIsUnknownWhenNotGiven() {

        tracker.preRequest(mock(HttpServletRequest.class));

        assertEquals("UNKNOWN", lastContext.getPartnerName());
    }

    @Test
    public void serviceNameGenerated() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/testUri");
        tracker.preRequest(request);

        assertEquals("GET: /testUri", lastContext   .getServiceName());
    }

    @Test
    public void contextClearedWhenRequestFinished() {

        tracker.preRequest(mock(HttpServletRequest.class));

        assertNotNull(lastContext.getRequestId());

        tracker.postRequest(mock(RequestProcessingResult.class));

        assertEquals(ContextData.builder().build(), lastContext);
    }
}
