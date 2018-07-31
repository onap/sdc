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
import static org.openecomp.sdc.logging.api.StatusCode.COMPLETE;
import static org.openecomp.sdc.logging.api.StatusCode.ERROR;

import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.StatusCode;

/**
 * Test initialization and of audit tracker and log invocation.
 *
 * @author evitaliy
 * @since 01 Aug 2018
 */
public class AuditTrackerTest {

    private static final StubStatusInterpreter STUB_STATUS_INTERPRETER = new StubStatusInterpreter(COMPLETE, "Done");

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenLoggerNull() {
        new AuditTracker((Logger) null, STUB_STATUS_INTERPRETER);
    }

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenResourceTypeNull() {
        new AuditTracker((Class<?>) null, STUB_STATUS_INTERPRETER);
    }

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenLoggerWhenLoggerProvidedAndStatusInterpreterNull() {
        new AuditTracker(mock(Logger.class), null);
    }

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenLoggerWhenResourceTypeProvidedAndStatusInterpreterNull() {
        new AuditTracker(this.getClass(), null);
    }

    @Test
    public void nothingHappensWhenAuditDisabled() {

        Logger logger = mock(Logger.class);
        when(logger.isAuditEnabled()).thenReturn(false);

        AuditTracker tracker = new AuditTracker(logger, STUB_STATUS_INTERPRETER);
        tracker.postRequest(mock(HttpServletRequest.class), mock(HttpServletResponse.class));

        verify(logger, never()).audit(any(AuditData.class));
    }

    @Test
    public void reportedTimePositive() {

        Logger logger = mock(Logger.class);
        when(logger.isAuditEnabled()).thenReturn(true);

        AuditTracker tracker = new AuditTracker(logger, STUB_STATUS_INTERPRETER);
        tracker.postRequest(mock(HttpServletRequest.class), mock(HttpServletResponse.class));

        ArgumentCaptor<AuditData> auditDataCaptor = ArgumentCaptor.forClass(AuditData.class);
        verify(logger).audit(auditDataCaptor.capture());
        assertTrue(auditDataCaptor.getValue().getEndTime() > 0);
    }

    @Test
    public void resultInterpretedCorrectly() {

        Logger logger = mock(Logger.class);
        when(logger.isAuditEnabled()).thenReturn(true);

        final StatusCode status = ERROR;
        final String message = "Request failed";
        StubStatusInterpreter interpreter = new StubStatusInterpreter(status, message);

        AuditTracker tracker = new AuditTracker(logger, interpreter);
        tracker.postRequest(mock(HttpServletRequest.class), mock(HttpServletResponse.class));

        ArgumentCaptor<AuditData> auditDataCaptor = ArgumentCaptor.forClass(AuditData.class);
        verify(logger).audit(auditDataCaptor.capture());

        AuditData capturedAuditData = auditDataCaptor.getValue();
        assertEquals(message, capturedAuditData.getResponseDescription());
        assertEquals(status, capturedAuditData.getStatusCode());
    }

    @Test
    public void statusLoggedCorrectly() {

        Logger logger = mock(Logger.class);
        when(logger.isAuditEnabled()).thenReturn(true);

        final int responseCode = 505;
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(responseCode);

        AuditTracker tracker = new AuditTracker(logger, STUB_STATUS_INTERPRETER);
        tracker.postRequest(mock(HttpServletRequest.class), response);

        ArgumentCaptor<AuditData> auditDataCaptor = ArgumentCaptor.forClass(AuditData.class);
        verify(logger).audit(auditDataCaptor.capture());

        AuditData capturedAuditData = auditDataCaptor.getValue();
        assertEquals(Integer.toString(responseCode), capturedAuditData.getResponseCode());
    }

    @Test
    public void clientIpAddressLoggedCorrectly() {

        Logger logger = mock(Logger.class);
        when(logger.isAuditEnabled()).thenReturn(true);

        final String address = "196.50.30.122";
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(address);

        AuditTracker tracker = new AuditTracker(logger, STUB_STATUS_INTERPRETER);
        tracker.postRequest(request, mock(HttpServletResponse.class));

        ArgumentCaptor<AuditData> auditDataCaptor = ArgumentCaptor.forClass(AuditData.class);
        verify(logger).audit(auditDataCaptor.capture());

        AuditData capturedAuditData = auditDataCaptor.getValue();
        assertEquals(address, capturedAuditData.getClientIpAddress());
    }

    private static class StubStatusInterpreter implements Function<Integer, AuditTracker.Result> {

        private final StatusCode statusCode;
        private final String message;

        private StubStatusInterpreter(StatusCode statusCode, String message) {
            this.statusCode = statusCode;
            this.message = message;
        }

        @Override
        public AuditTracker.Result apply(Integer integer) {

            return new AuditTracker.Result() {

                @Override
                public StatusCode getStatus() {
                    return statusCode;
                }

                @Override
                public String getMessage() {
                    return message;
                }
            };
        }
    }
}