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

import java.io.IOException;
import java.util.UUID;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.api.LoggingContext;

/**
 *
 * <p>Places logging context information. Must be configured as a servlet filter in <i>web.xml</i>. The
 * behavior can be customized via init-params.</p>
 *
 * <p>Example:</p>
 *
 * <pre>
 *
 *  &lt;filter&gt;
 *      &lt;filter-name&gt;LoggingServletFilter&lt;/filter-name&gt;
 *      &lt;filter-class&gt;org.openecomp.sdc.logging.servlet.LoggingFilter&lt;/filter-class&gt;
 *      &lt;init-param&gt;
 *          &lt;param-name&gt;requestIdHeader&lt;/param-name&gt;
 *          &lt;param-value&gt;X-ONAP-RequestID&lt;/param-value&gt;
 *      &lt;/init-param&gt;
 *      &lt;init-param&gt;
 *          &lt;param-name&gt;partnerNameHeader&lt;/param-name&gt;
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
 *
 * @author evitaliy
 * @since 25 Jul 2016
 *
 * @deprecated Kept for backward compatibility. For JAX-RS application, use
 *      {@link org.openecomp.sdc.logging.ws.rs.LoggingRequestFilter} and
 *      {@link org.openecomp.sdc.logging.ws.rs.LoggingResponseFilter} instead.
 */
@Deprecated
public class LoggingFilter implements Filter {

    static final String REQUEST_ID_HEADER_PARAM = "requestIdHeader";
    static final String DEFAULT_REQUEST_ID_HEADER = "X-ECOMP-RequestID";

    static final String PARTNER_NAME_HEADER_PARAM = "partnerNameHeader";
    static final String DEFAULT_PARTNER_NAME_HEADER = "USER_ID";

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingFilter.class);

    private String requestIdHeader;
    private String partnerNameHeader;

    @Override
    public void destroy() {
        // forced by the interface - not implemented
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = HttpServletRequest.class.cast(request);

        try {

            LoggingContext.clear();

            LoggingContext.putServiceName(httpRequest.getRequestURI());

            String requestId = httpRequest.getHeader(requestIdHeader);
            LoggingContext.putRequestId(requestId == null ? UUID.randomUUID().toString() : requestId);

            String partner = httpRequest.getHeader(partnerNameHeader);
            if (partner != null) {
                LoggingContext.putPartnerName(partner);
            }

            chain.doFilter(request, response);

        } finally {
            LoggingContext.clear();
        }
    }

    @Override
    public void init(FilterConfig config) {
        requestIdHeader = getInitParam(config, REQUEST_ID_HEADER_PARAM, DEFAULT_REQUEST_ID_HEADER);
        partnerNameHeader = getInitParam(config, PARTNER_NAME_HEADER_PARAM, DEFAULT_PARTNER_NAME_HEADER);
    }

    private String getInitParam(FilterConfig config, String paramName, String defaultValue) {
        String value = config.getInitParameter(paramName);
        LOGGER.debug("Logging filter configuration param '{}' value '{}'", paramName, value);
        return value == null ? defaultValue : value;
    }
}
