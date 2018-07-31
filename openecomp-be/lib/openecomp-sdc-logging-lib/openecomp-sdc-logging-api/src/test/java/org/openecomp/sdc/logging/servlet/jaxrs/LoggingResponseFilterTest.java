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

package org.openecomp.sdc.logging.servlet.jaxrs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.logging.servlet.jaxrs.LoggingRequestFilter.LOGGING_TRACKER_KEY;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import org.junit.Test;
import org.openecomp.sdc.logging.servlet.Tracker;

/**
 * Unit testing JAX-RS response filter.
 *
 * @author evitaliy
 * @since 19 Mar 2018
 */
public class LoggingResponseFilterTest {

    @Test
    public void noExceptionsWhenTrackerNotPassed() {
        new LoggingResponseFilter().filter(mock(ContainerRequestContext.class), mock(ContainerResponseContext.class));
    }

    @Test
    public void postRequestWhenTrackerPassed() {

        Tracker tracker = mock(Tracker.class);

        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getProperty(LOGGING_TRACKER_KEY)).thenReturn(tracker);

        LoggingResponseFilter responseFilter = new LoggingResponseFilter();
        responseFilter.setHttpRequest(mock(HttpServletRequest.class));
        responseFilter.setHttpResponse(mock(HttpServletResponse.class));

        responseFilter.filter(requestContext, mock(ContainerResponseContext.class));

        verify(tracker, times(1)).postRequest(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
}