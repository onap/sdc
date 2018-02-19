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

import static org.openecomp.sdc.logging.api.StatusCode.COMPLETE;
import static org.openecomp.sdc.logging.api.StatusCode.ERROR;
import static org.openecomp.sdc.logging.ws.rs.LoggingRequestFilter.START_TIME_KEY;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.openecomp.sdc.logging.api.AuditData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.LoggingContext;
import org.openecomp.sdc.logging.api.StatusCode;

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
 *
 * @author evitaliy
 * @since 29 Oct 17
 *
 * @see ContainerResponseFilter
 */
@Provider
public class LoggingResponseFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingResponseFilter.class);

    /**
     * Tracks reporting configuration problems to the log. We want to report them only once, and not to write to log
     * upon every request, as the configuration will not change in runtime.
     */
    private boolean reportBadConfiguration = true;

    @Context
    private HttpServletRequest httpRequest;

    @Override
    public void filter(ContainerRequestContext containerRequestContext,
            ContainerResponseContext containerResponseContext) {

        try {
            writeAudit(containerRequestContext, containerResponseContext);
        } finally {
            LoggingContext.clear();
        }
    }

    private void writeAudit(ContainerRequestContext containerRequestContext,
            ContainerResponseContext containerResponseContext) {

        if (!LOGGER.isAuditEnabled()) {
            return;
        }

        long start = readStartTime(containerRequestContext);
        long end = System.currentTimeMillis();

        Response.StatusType statusInfo = containerResponseContext.getStatusInfo();
        int responseCode = statusInfo.getStatusCode();

        StatusCode statusCode = (responseCode > 199 && responseCode < 400) ? COMPLETE : ERROR;

        AuditData auditData = AuditData.builder().startTime(start).endTime(end).statusCode(statusCode)
                                       .responseCode(Integer.toString(responseCode))
                                       .responseDescription(statusInfo.getReasonPhrase())
                                       .clientIpAddress(httpRequest.getRemoteAddr()).build();
        LOGGER.audit(auditData);
    }

    private long readStartTime(ContainerRequestContext containerRequestContext) {

        Object startTime = containerRequestContext.getProperty(START_TIME_KEY);
        if (startTime == null) {

            if (reportBadConfiguration) {
                reportBadConfiguration = false;
                LOGGER.error("{} key was not found in JAX-RS request context. "
                        + "Make sure you configured a request filter", START_TIME_KEY);
            }

            return 0;
        }

        try {
            return Long.class.cast(startTime);
        } catch (ClassCastException e) {

            if (reportBadConfiguration) {
                reportBadConfiguration = false;
                LOGGER.error("{} key in JAX-RS request context contains an object of type '{}'"
                        + ", but 'java.lang.Long' is expected", START_TIME_KEY, startTime.getClass().getName());
            }

            return 0;
        }
    }
}

