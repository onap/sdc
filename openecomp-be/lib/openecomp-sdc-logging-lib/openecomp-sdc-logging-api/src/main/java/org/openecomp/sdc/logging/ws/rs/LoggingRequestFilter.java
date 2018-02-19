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

package org.openecomp.sdc.logging.ws.rs;

import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import org.openecomp.sdc.logging.api.LoggingContext;

/**
 * <p>Takes care of logging initialization an HTTP request hits the application. This includes populating logging
 * context and storing the request processing start time, so that it can be used for audit. The filter was built
 * <b>works in tandem</b> with {@link LoggingResponseFilter} or a similar implementation.</p>
 * <p>The filter requires a few HTTP header names to be configured. These HTTP headers are used for propagating logging
 * and tracing information between ONAP components.</p>
 * <p>Sample configuration for a Spring environment:</p>
 * <pre>
 *     &lt;jaxrs:providers&gt;
 *         &lt;bean class="org.openecomp.sdc.logging.ws.rs.LoggingRequestFilter"&gt;
 *             &lt;property name="requestIdHeader" value="X-ECOMP-RequestID"/&gt;
 *             &lt;property name="partnerNameHeader" value="USER_ID"/&gt;
 *         &lt;/bean&gt;
 *     &lt;/jaxrs:providers&gt;
 * </pre>
 * <p>Keep in mind that the filters does nothing in case when a request cannot be mapped to a working JAX-RS resource
 * (implementation). For instance, when the path is invalid (404), or there is no handler for a particular method (405).
 * </p>
 *
 * @author evitaliy, katyr
 * @since 29 Oct 17
 *
 * @see ContainerRequestFilter
 */
@Provider
public class LoggingRequestFilter implements ContainerRequestFilter {

    static final String START_TIME_KEY = "audit.start.time";

    @Context
    private ResourceInfo resource;

    private String requestIdHeader;
    private String partnerNameHeader;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {

        if (resource == null) {
            // JAX-RS could not find a mapping this response, probably due to HTTP 404 (not found),
            // 405 (method not allowed), 415 (unsupported media type), etc. with a message in Web server log
            return;
        }

        String requestId = containerRequestContext.getHeaderString(requestIdHeader);
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        LoggingContext.putRequestId(requestId);

        String partnerName = containerRequestContext.getHeaderString(partnerNameHeader);
        if (partnerName != null) {
            LoggingContext.putPartnerName(partnerName);
        }

        String serviceName = resource.getResourceClass().getName() + "." + resource.getResourceMethod().getName();
        LoggingContext.putServiceName(serviceName);

        containerRequestContext.setProperty(START_TIME_KEY, System.currentTimeMillis());
    }

    /**
     * Configuration parameter for request ID HTTP header.
     */
    public void setRequestIdHeader(String requestIdHeader) {
        this.requestIdHeader = requestIdHeader;
    }

    /**
     * Configuration parameter for partner name HTTP header.
     */
    public void setPartnerNameHeader(String partnerNameHeader) {
        this.partnerNameHeader = partnerNameHeader;
    }
}
