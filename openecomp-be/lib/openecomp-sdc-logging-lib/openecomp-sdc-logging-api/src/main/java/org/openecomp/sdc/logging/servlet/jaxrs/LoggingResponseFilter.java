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

import static javax.ws.rs.core.Response.Status.Family.REDIRECTION;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus.*;
import static org.openecomp.sdc.logging.servlet.jaxrs.LoggingRequestFilter.LOGGING_TRACKER_KEY;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.onap.logging.ref.slf4j.ONAPLogConstants.ResponseStatus;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.servlet.RequestProcessingResult;
import org.openecomp.sdc.logging.servlet.Tracker;

/**
 * <p>Takes care of logging when an HTTP request leaves the application. This includes writing to audit and clearing
 * logging context. This filter <b>only works properly in tandem</b> with {@link LoggingRequestFilter} or a similar
 * implementation.</p>
 * <p>Sample configuration for a Spring environment:</p>
 * <pre>
 *     &lt;jaxrs:providers&gt;
 *         &lt;bean class="org.openecomp.sdc.logging.ws.rs.LoggingResponseFilter"/&gt;
 *     &lt;/jaxrs:providers&gt;
 * </pre>
 * <p><i>It is highly recommended to configure a custom JAX-RS exception mapper so that this filter will not be bypassed
 * due to unhandled application or container exceptions.</i></p>
 *
 * @author evitaliy
 * @see ContainerResponseFilter
 * @since 29 Oct 17
 */
@Provider
public class LoggingResponseFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingResponseFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        Tracker tracker = (Tracker) requestContext.getProperty(LOGGING_TRACKER_KEY);

        if (tracker == null) {
            LOGGER.debug("No logging tracker received");
            return;
        }

        tracker.postRequest(new ContainerResponseResult(responseContext.getStatusInfo()));
    }

    private static class ContainerResponseResult implements RequestProcessingResult {

        private final Response.StatusType statusInfo;

        private ContainerResponseResult(Response.StatusType statusInfo) {
            this.statusInfo = statusInfo;
        }

        @Override
        public int getStatus() {
            return statusInfo.getStatusCode();
        }

        @Override
        public ResponseStatus getStatusCode() {
            Response.Status.Family family = statusInfo.getFamily();
            return family.equals(SUCCESSFUL) || family.equals(REDIRECTION) ? COMPLETE : ERROR;
        }

        @Override
        public String getStatusPhrase() {
            return statusInfo.getReasonPhrase();
        }
    }
}

