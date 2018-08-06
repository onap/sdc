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

import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import javax.servlet.http.HttpServletRequest;

/**
 * Tracker for all the elements of ONAP logging and tracing at an entry point to an application.
 * The order of invocations is important, and on {@link #preRequest(HttpServletRequest)} it respects the order of
 * trackers passed to the constructor. On {@link #postRequest(RequestProcessingResult)}, the invocation will be in the
 * <b>reverse</b> order.
 *
 * @author evitaliy
 * @since 01 Aug 2018
 */
public class CombinedTracker implements Tracker {

    private final List<Tracker> trackers;

    public CombinedTracker(Tracker... trackers) {
        this.trackers = Arrays.asList(trackers);
    }

    @Override
    public void preRequest(HttpServletRequest request) {

        for (Tracker t : trackers) {
            t.preRequest(request);
        }
    }

    @Override
    public void postRequest(RequestProcessingResult result) {

        ListIterator<Tracker> iterator = trackers.listIterator(trackers.size());
        while (iterator.hasPrevious()) {
            iterator.previous().postRequest(result);
        }
    }
}
