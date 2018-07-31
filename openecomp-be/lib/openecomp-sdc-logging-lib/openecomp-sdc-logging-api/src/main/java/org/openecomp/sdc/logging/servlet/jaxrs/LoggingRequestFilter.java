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

import static org.openecomp.sdc.logging.LoggingConstants.DEFAULT_PARTNER_NAME_HEADER;
import static org.openecomp.sdc.logging.LoggingConstants.DEFAULT_REQUEST_ID_HEADER;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.servlet.CombinedTracker;
import org.openecomp.sdc.logging.servlet.HttpHeader;
import org.openecomp.sdc.logging.servlet.Tracker;

/**
 * <p>Takes care of logging initialization an HTTP request hits the application. This includes populating logging
 * context and tracking the request for audit. The filter <b>works in tandem</b> with {@link LoggingResponseFilter} or
 * a similar implementation.</p>
 * <p>The filter requires a few HTTP header names to be configured. These HTTP headers are used for propagating logging
 * and tracing information between ONAP components. Sample configuration for a Spring environment:</p>
 * <pre>
 *     &lt;jaxrs:providers&gt;
 *         &lt;bean class="org.openecomp.sdc.logging.ws.rs.LoggingRequestFilter"&gt;
 *             &lt;property name="requestIdHeaders" value="X-ONAP-RequestID"/&gt;
 *             &lt;property name="partnerNameHeaders" value="X-ONAP-InstanceID"/&gt;
 *         &lt;/bean&gt;
 *     &lt;/jaxrs:providers&gt;
 * </pre>
 * <p>Keep in mind that the filters does nothing in case when a request cannot be mapped to a working JAX-RS resource
 * (implementation). For instance, when the path is invalid (404), or there is no handler for a particular method (405).
 * </p>
 *
 * @author evitaliy, katyr
 * @see ContainerRequestFilter
 * @since 29 Oct 17
 */
@Provider
public class LoggingRequestFilter implements ContainerRequestFilter {

    static final String LOGGING_TRACKER_KEY = "onap.logging.tracker";

    private static final String MULTI_VALUE_SEPARATOR = ",";
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingRequestFilter.class);

    private HttpServletRequest httpRequest;

    private HttpHeader requestIdHeader = new HttpHeader(new String[] {DEFAULT_REQUEST_ID_HEADER});
    private HttpHeader partnerNameHeader = new HttpHeader(new String[] {DEFAULT_PARTNER_NAME_HEADER});

    private ResourceInfo resource;

    /**
     * Injection of a resource that matches the request from JAX-RS context.
     *
     * @param resource automatically injected by JAX-RS container
     */
    @Context
    public void setResource(ResourceInfo resource) {
        this.resource = resource;
    }

    /**
     * Injection of HTTP request object from JAX-RS context.
     *
     * @param httpRequest automatically injected by JAX-RS container
     */
    @Context
    public void setHttpRequest(HttpServletRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    /**
     * Configuration parameter for request ID HTTP header.
     */
    public void setRequestIdHeaders(String requestIdHeaders) {
        LOGGER.debug("Valid request ID headers: {}", requestIdHeaders);
        this.requestIdHeader = new HttpHeader(requestIdHeaders.split(MULTI_VALUE_SEPARATOR));
    }

    /**
     * Configuration parameter for partner name HTTP header.
     */
    public void setPartnerNameHeaders(String partnerNameHeaders) {
        LOGGER.debug("Valid partner name headers: {}", partnerNameHeaders);
        this.partnerNameHeader = new HttpHeader(partnerNameHeaders.split(MULTI_VALUE_SEPARATOR));
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        Class<?> resourceClass = resource.getResourceMethod().getDeclaringClass();
        Tracker tracker = new CombinedTracker(resourceClass, partnerNameHeader, requestIdHeader);
        requestContext.setProperty(LOGGING_TRACKER_KEY, tracker);
        tracker.preRequest(httpRequest);
    }
}
