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

import static org.openecomp.sdc.logging.LoggingConstants.DEFAULT_PARTNER_NAME_HEADER;
import static org.openecomp.sdc.logging.LoggingConstants.DEFAULT_REQUEST_ID_HEADER;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.openecomp.sdc.logging.api.ContextData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.LoggingContext;
import org.openecomp.sdc.logging.servlet.jaxrs.LoggingRequestFilter;
import org.openecomp.sdc.logging.servlet.jaxrs.LoggingResponseFilter;

/**
 * <p>Places logging context information. Must be configured as a servlet filter in <i>web.xml</i>. The behavior can be
 * customized via init-params.</p>
 * <p>Example:</p>
 * <pre>
 *
 *  &lt;filter&gt;
 *      &lt;filter-name&gt;LoggingServletFilter&lt;/filter-name&gt;
 *      &lt;filter-class&gt;org.openecomp.sdc.logging.servlet.LoggingFilter&lt;/filter-class&gt;
 *      &lt;init-param&gt;
 *          &lt;param-name&gt;requestIdHeaders&lt;/param-name&gt;
 *          &lt;param-value&gt;X-ONAP-RequestID&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *      &lt;init-param&gt;
 *          &lt;param-name&gt;partnerNameHeaders&lt;/param-name&gt;
 *          &lt;param-value&gt;USER_ID&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *  &lt;/filter&gt;
 *
 *  &lt;filter-mapping&gt;
 *      &lt;filter-name&gt;LoggingServletFilter&lt;/filter-name&gt;
 *      &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *  &lt;/filter-mapping&gt;
 *
 * </pre>
 *
 * @author evitaliy
 * @since 25 Jul 2016
 * @deprecated Kept for backward compatibility. For JAX-RS application, use
 * {@link LoggingRequestFilter} and
 * {@link LoggingResponseFilter} instead.
 */
@Deprecated
public class LoggingFilter implements Filter {

    static final String MULTI_VALUE_SEPARATOR = ",";

    static final String REQUEST_ID_HEADERS_PARAM = "requestIdHeaders";
    static final String PARTNER_NAME_HEADERS_PARAM = "partnerNameHeaders";

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    private HttpHeader requestIdHeaders;
    private HttpHeader partnerNameHeaders;

    @Override
    public void init(FilterConfig config) {
        requestIdHeaders = getInitParam(config, REQUEST_ID_HEADERS_PARAM, DEFAULT_REQUEST_ID_HEADER);
        partnerNameHeaders = getInitParam(config, PARTNER_NAME_HEADERS_PARAM, DEFAULT_PARTNER_NAME_HEADER);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {

            LoggingContext.clear();

            ContextData.ContextDataBuilder contextData = ContextData.builder();

            contextData.serviceName(httpRequest.getRequestURI());

            partnerNameHeaders.getAny(httpRequest::getHeader).ifPresent(contextData::partnerName);

            String requestId = requestIdHeaders.getAny(httpRequest::getHeader).orElse(UUID.randomUUID().toString());
            contextData.requestId(requestId);

            LoggingContext.put(contextData.build());

            chain.doFilter(request, response);

        } finally {
            LoggingContext.clear();
        }
    }

    @Override
    public void destroy() {
        // forced by the interface - not implemented
    }

    private HttpHeader getInitParam(FilterConfig config, String paramName, String defaultValue) {
        String value = config.getInitParameter(paramName);
        LOGGER.debug("Logging filter configuration param '{}' value '{}'", paramName, value);
        return new HttpHeader(value == null ? new String[] { defaultValue } : value.split(MULTI_VALUE_SEPARATOR));
    }
}
