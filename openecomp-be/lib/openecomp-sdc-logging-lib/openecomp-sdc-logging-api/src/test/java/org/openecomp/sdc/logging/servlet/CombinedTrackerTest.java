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
import org.junit.Test;

/**
 * Test the construction and invocation of combined tracker.
 *
 * @author evitaliy
 * @since 01 Aug 2018
 */
public class CombinedTrackerTest {

    @Test(expected = NullPointerException.class)
    public void throwExceptionWhenTrackersNull() {
        new CombinedTracker((Tracker[]) null);
    }

    @Test
    public void trackersCalledWhenPreRequest() {
        Tracker firstTracker = mock(Tracker.class);
        Tracker secondTracker = mock(Tracker.class);
        CombinedTracker tracker = new CombinedTracker(firstTracker, secondTracker);
        tracker.preRequest(mock(HttpServletRequest.class));
        verify(firstTracker, times(1)).preRequest(any(HttpServletRequest.class));
        verify(secondTracker, times(1)).preRequest(any(HttpServletRequest.class));
    }

    @Test
    public void trackersCalledWhenPostRequest() {
        Tracker firstTracker = mock(Tracker.class);
        Tracker secondTracker = mock(Tracker.class);
        CombinedTracker tracker = new CombinedTracker(firstTracker, secondTracker);
        tracker.postRequest(mock(RequestProcessingResult.class));
        verify(firstTracker, times(1)).postRequest(any(RequestProcessingResult.class));
        verify(secondTracker, times(1)).postRequest(any(RequestProcessingResult.class));
    }
}