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

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus;
import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.Logger;

/**
 * Test initialization and of audit tracker and log invocation.
 *
 * @author evitaliy
 * @since 01 Aug 2018
 */
public class AuditTrackerTest {

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenLoggerNull() {
        new AuditTracker((Logger) null);
    }

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenResourceTypeNull() {
        new AuditTracker((Class<?>) null);
    }

    @Test(expected = IllegalStateException.class)
    public void throwExceptionWhenPreRequestCalledMoreThanOnce() {
        AuditTracker tracker = new AuditTracker(mock(Logger.class));
        HttpServletRequest request = mock(HttpServletRequest.class);
        tracker.preRequest(request);
        tracker.preRequest(request);
    }

    @Test(expected = IllegalStateException.class)
    public void throwExceptionWhenRreRequestNeverCalled() {
        AuditTracker tracker = new AuditTracker(mock(Logger.class));
        tracker.postRequest(mock(RequestProcessingResult.class));
    }

    @Test
    public void nothingHappensWhenAuditDisabled() {

        Logger logger = mock(Logger.class);
        when(logger.isAuditEnabled()).thenReturn(false);

        AuditTracker tracker = new AuditTracker(logger);
        tracker.preRequest(mock(HttpServletRequest.class));
        tracker.postRequest(mock(RequestProcessingResult.class));

        verify(logger, never()).auditExit(any(AuditData.class));
    }

    @Test
    public void reportedTimePositive() {

        Logger logger = mock(Logger.class);
        when(logger.isAuditEnabled()).thenReturn(true);

        AuditTracker tracker = new AuditTracker(logger);
        tracker.preRequest(mock(HttpServletRequest.class));
        tracker.postRequest(mock(RequestProcessingResult.class));

        ArgumentCaptor<AuditData> auditDataCaptor = ArgumentCaptor.forClass(AuditData.class);
        verify(logger).auditExit(auditDataCaptor.capture());
        assertTrue(auditDataCaptor.getValue().getEndTime() > 0);
    }

    @Test
    public void interpretedRequestResultsPassedDownToAudit() {

        Logger logger = mock(Logger.class);
        when(logger.isAuditEnabled()).thenReturn(true);

        AuditTracker tracker = new AuditTracker(logger);
        tracker.preRequest(mock(HttpServletRequest.class));

        final StubRequestProcessingResult result = new StubRequestProcessingResult();
        tracker.postRequest(result);

        ArgumentCaptor<AuditData> auditDataCaptor = ArgumentCaptor.forClass(AuditData.class);
        verify(logger).auditExit(auditDataCaptor.capture());

        AuditData capturedAuditData = auditDataCaptor.getValue();
        assertEquals(Integer.toString(result.getStatus()), capturedAuditData.getResponseCode());
        assertEquals(result.getStatusCode(), capturedAuditData.getStatusCode());
        assertEquals(result.getStatusPhrase(), capturedAuditData.getResponseDescription());
    }

    @Test
    public void clientIpAddressPassedDownToAudit() {

        Logger logger = mock(Logger.class);
        when(logger.isAuditEnabled()).thenReturn(true);

        final String address = "196.50.30.122";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(address);

        AuditTracker tracker = new AuditTracker(logger);
        tracker.preRequest(request);
        tracker.postRequest(mock(RequestProcessingResult.class));

        ArgumentCaptor<AuditData> auditDataCaptor = ArgumentCaptor.forClass(AuditData.class);
        verify(logger).auditExit(auditDataCaptor.capture());

        AuditData capturedAuditData = auditDataCaptor.getValue();
        assertEquals(address, capturedAuditData.getClientIpAddress());
    }

    private static class StubRequestProcessingResult implements RequestProcessingResult {

        @Override
        public int getStatus() {
            return 505;
        }

        @Override
        public ResponseStatus getStatusCode() {
            return ResponseStatus.ERROR;
        }

        @Override
        public String getStatusPhrase() {
            return "Test request failed";
        }
    }
}