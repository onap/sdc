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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.openecomp.sdc.logging.api.ContextData;
import org.openecomp.sdc.logging.api.LoggingContext;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * Populating context from request data.
 *
 * @author evitaliy
 * @since 01 Aug 2018
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(LoggingContext.class)
public class ContextTrackerTest {

    private static final String X_REQUEST_ID = "X-REQUEST-ID";
    private static final HttpHeader REQUEST_ID_HEADER = new HttpHeader(X_REQUEST_ID);

    private static final String X_PARTNER_NAME = "X-PARTNER-NAME";
    private static final HttpHeader PARTNER_NAME_HEADER = new HttpHeader(X_PARTNER_NAME);

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenPartnerNamesNull() {
        new ContextTracker(null, REQUEST_ID_HEADER);
    }

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenRequestIdsNull() {
        new ContextTracker(PARTNER_NAME_HEADER, null);
    }

    @Test
    public void requestIdCopiedWhenGiven() {

        mockStatic(LoggingContext.class);

        final String requestId = "request-id-for-unit-testing";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(X_REQUEST_ID)).thenReturn(requestId);

        ContextTracker tracker = new ContextTracker(PARTNER_NAME_HEADER, REQUEST_ID_HEADER);
        tracker.preRequest(request);

        ArgumentCaptor<ContextData> contextDataCaptor = ArgumentCaptor.forClass(ContextData.class);
        verifyStatic(LoggingContext.class);

        LoggingContext.put(contextDataCaptor.capture());

        assertEquals(requestId, contextDataCaptor.getValue().getRequestId());
    }

    @Test
    public void requestIdGeneratedWhenNotGiven() {

        mockStatic(LoggingContext.class);

        ContextTracker tracker = new ContextTracker(PARTNER_NAME_HEADER, REQUEST_ID_HEADER);
        tracker.preRequest(mock(HttpServletRequest.class));

        ArgumentCaptor<ContextData> contextDataCaptor = ArgumentCaptor.forClass(ContextData.class);
        verifyStatic(LoggingContext.class);

        LoggingContext.put(contextDataCaptor.capture());

        String requestId = contextDataCaptor.getValue().getRequestId();
        assertNotNull(requestId);
        assertFalse(requestId.isEmpty());
    }

    @Test
    public void partnerNameCopiedWhenGiven() {

        mockStatic(LoggingContext.class);

        final String partner = "partner-name-for-unit-testing";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(X_PARTNER_NAME)).thenReturn(partner);

        ContextTracker tracker = new ContextTracker(PARTNER_NAME_HEADER, REQUEST_ID_HEADER);
        tracker.preRequest(request);

        ArgumentCaptor<ContextData> contextDataCaptor = ArgumentCaptor.forClass(ContextData.class);
        verifyStatic(LoggingContext.class);

        LoggingContext.put(contextDataCaptor.capture());

        assertEquals(partner, contextDataCaptor.getValue().getPartnerName());
    }

    @Test
    public void partnerNameIsUnknownWhenNotGiven() {

        mockStatic(LoggingContext.class);

        ContextTracker tracker = new ContextTracker(PARTNER_NAME_HEADER, REQUEST_ID_HEADER);
        tracker.preRequest(mock(HttpServletRequest.class));

        ArgumentCaptor<ContextData> contextDataCaptor = ArgumentCaptor.forClass(ContextData.class);
        verifyStatic(LoggingContext.class);

        LoggingContext.put(contextDataCaptor.capture());

        assertEquals(contextDataCaptor.getValue().getPartnerName(), "UNKNOWN");
    }

    @Test
    public void serviceNameGenerated() {

        mockStatic(LoggingContext.class);

        ContextTracker tracker = new ContextTracker(PARTNER_NAME_HEADER, REQUEST_ID_HEADER);
        tracker.preRequest(mock(HttpServletRequest.class));

        ArgumentCaptor<ContextData> contextDataCaptor = ArgumentCaptor.forClass(ContextData.class);
        verifyStatic(LoggingContext.class);

        LoggingContext.put(contextDataCaptor.capture());

        assertNotNull(contextDataCaptor.getValue().getServiceName());
    }

    @Test
    public void contextClearedWhenRequestFinished() {

        mockStatic(LoggingContext.class);

        ContextTracker tracker = new ContextTracker(PARTNER_NAME_HEADER, REQUEST_ID_HEADER);
        tracker.postRequest(mock(RequestProcessingResult.class));

        verifyStatic(LoggingContext.class);
        LoggingContext.clear();
    }
}

