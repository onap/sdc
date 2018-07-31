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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;

/**
 * Test the construction and invocation of combined tracker.
 *
 * @author evitaliy
 * @since 01 Aug 2018
 */
public class CombinedTrackerTest {

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenAuditNull() {
        new CombinedTracker(mock(ContextTracker.class), null);
    }

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenContextNull() {
        new CombinedTracker(null, mock(AuditTracker.class));
    }

    @Test
    public void trackersCalledWhenPreRequest() {
        ContextTracker context = mock(ContextTracker.class);
        AuditTracker audit = mock(AuditTracker.class);
        CombinedTracker tracker = new CombinedTracker(context, audit);
        tracker.preRequest(mock(HttpServletRequest.class));
        verify(audit, times(1)).preRequest(any(HttpServletRequest.class));
        verify(context, times(1)).preRequest(any(HttpServletRequest.class));
    }

    @Test
    public void trackersCalledWhenPostRequest() {
        ContextTracker context = mock(ContextTracker.class);
        AuditTracker audit = mock(AuditTracker.class);
        CombinedTracker tracker = new CombinedTracker(context, audit);
        tracker.postRequest(mock(HttpServletRequest.class), mock(HttpServletResponse.class));
        verify(audit, times(1)).postRequest(any(HttpServletRequest.class), any(HttpServletResponse.class));
        verify(context, times(1)).postRequest(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }
}