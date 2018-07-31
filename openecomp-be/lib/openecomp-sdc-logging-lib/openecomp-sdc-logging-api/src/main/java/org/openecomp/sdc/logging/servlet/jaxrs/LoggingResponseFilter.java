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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.LoggingContext;

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
 * @since 29 Oct 17
 *
 * @see ContainerResponseFilter
 */
@Provider
public class LoggingResponseFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingResponseFilter.class);

    private HttpServletRequest httpRequest;
    private HttpServletResponse httpResponse;

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
     * Injection of HTTP response object from JAX-RS context.
     *
     * @param httpResponse automatically injected by JAX-RS container
     */
    @Context
    public void setHttpResponse(HttpServletResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext,
            ContainerResponseContext containerResponseContext) {

        try {

//            if ((resource == null) || (resource.getResourceClass() == null)) {
//                LOGGER.debug("No matching resource, skipping audit.");
//                return;
//            }

        } finally {
            LoggingContext.clear();
        }
    }


//        Logger resourceLogger = LoggerFactory.getLogger(resource.getResourceMethod().getDeclaringClass());

}

