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

package org.openecomp.sdc.logging.servlet.spring;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.logging.servlet.spring.LoggingInterceptor.LOGGING_TRACKER_KEY;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus;
import org.openecomp.sdc.logging.servlet.HttpHeader;
import org.openecomp.sdc.logging.servlet.RequestProcessingResult;
import org.openecomp.sdc.logging.servlet.Tracker;

/**
 * Audit tracking via Spring interceptor.
 *
 * @author evitaliy
 * @since 05 Aug 2018
 */
public class LoggingInterceptorTest {

    @Test(expected = NullPointerException.class)
    public void exceptionThrownWhenPartnerNameHeaderNull() {
        new LoggingInterceptor(null, mock(HttpHeader.class));
    }

    @Test(expected = NullPointerException.class)
    public void exceptionThrownWhenRequestIdHeaderNull() {
        new LoggingInterceptor(mock(HttpHeader.class), null);
    }

    @Test
    public void trackerAddedWhenBeforeRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        LoggingInterceptor interceptor = new LoggingInterceptor(mock(HttpHeader.class), mock(HttpHeader.class));
        interceptor.preHandle(request, mock(HttpServletResponse.class), null);
        verify(request).setAttribute(eq(LOGGING_TRACKER_KEY), any(Tracker.class));
    }

    @Test
    public void trackerInvokedWhenPresentInRequest() {

        Tracker tracker = mock(Tracker.class);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(LOGGING_TRACKER_KEY)).thenReturn(tracker);

        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getStatus()).thenReturn(200);

        LoggingInterceptor interceptor = new LoggingInterceptor(mock(HttpHeader.class), mock(HttpHeader.class));
        interceptor.afterCompletion(request, response, null, null);
        verify(tracker).postRequest(any(RequestProcessingResult.class));
    }

    @Test
    public void errorStatusWhenInformationalCode() {
        final int status = 101;
        LoggingInterceptor.ServletResponseResult result = new LoggingInterceptor.ServletResponseResult(status);
        assertEquals(status, result.getStatus());
        assertEquals(ResponseStatus.ERROR, result.getStatusCode());
    }

    @Test
    public void errorStatusWhenClientErrorCode() {
        final int status = 404;
        LoggingInterceptor.ServletResponseResult result = new LoggingInterceptor.ServletResponseResult(status);
        assertEquals(status, result.getStatus());
        assertEquals(ResponseStatus.ERROR, result.getStatusCode());
    }

    @Test
    public void errorStatusWhenServerErrorCode() {
        final int status = 503;
        LoggingInterceptor.ServletResponseResult result = new LoggingInterceptor.ServletResponseResult(status);
        assertEquals(status, result.getStatus());
        assertEquals(ResponseStatus.ERROR, result.getStatusCode());
    }

    @Test
    public void completeStatusWhenSuccessCode() {
        final int status = 204;
        LoggingInterceptor.ServletResponseResult result = new LoggingInterceptor.ServletResponseResult(status);
        assertEquals(status, result.getStatus());
        assertEquals(ResponseStatus.COMPLETE, result.getStatusCode());
    }

    @Test
    public void completeStatusWhenRedirectionCode() {
        final int status = 307;
        LoggingInterceptor.ServletResponseResult result = new LoggingInterceptor.ServletResponseResult(status);
        assertEquals(status, result.getStatus());
        assertEquals(ResponseStatus.COMPLETE, result.getStatusCode());
    }

    @Test
    public void errorStatusWhenNonStandardInformationalCode() {
        final int status = 133;
        LoggingInterceptor.ServletResponseResult result = new LoggingInterceptor.ServletResponseResult(status);
        assertEquals(status, result.getStatus());
        assertEquals(ResponseStatus.ERROR, result.getStatusCode());
    }

    @Test
    public void errorStatusWhenNonStandardClientErrorCode() {
        final int status = 485;
        LoggingInterceptor.ServletResponseResult result = new LoggingInterceptor.ServletResponseResult(status);
        assertEquals(status, result.getStatus());
        assertEquals(ResponseStatus.ERROR, result.getStatusCode());
    }

    @Test
    public void errorStatusWhenNonStandardServerErrorCode() {
        final int status = 547;
        LoggingInterceptor.ServletResponseResult result = new LoggingInterceptor.ServletResponseResult(status);
        assertEquals(status, result.getStatus());
        assertEquals(ResponseStatus.ERROR, result.getStatusCode());
    }

    @Test
    public void completeStatusWhenNonStandardSuccessCode() {
        final int status = 277;
        LoggingInterceptor.ServletResponseResult result = new LoggingInterceptor.ServletResponseResult(status);
        assertEquals(status, result.getStatus());
        assertEquals(ResponseStatus.COMPLETE, result.getStatusCode());
    }

    @Test
    public void completeStatusWhenNonStandardRedirectionCode() {
        final int status = 364;
        LoggingInterceptor.ServletResponseResult result = new LoggingInterceptor.ServletResponseResult(status);
        assertEquals(status, result.getStatus());
        assertEquals(ResponseStatus.COMPLETE, result.getStatusCode());
    }
}
