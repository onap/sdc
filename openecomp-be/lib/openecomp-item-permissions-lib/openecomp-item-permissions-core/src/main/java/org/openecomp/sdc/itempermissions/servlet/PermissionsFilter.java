/*
 * Copyright © 2016-2017 European Support Limited
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
package org.openecomp.sdc.itempermissions.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import org.openecomp.sdc.common.errors.ErrorCodeAndMessage;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.itempermissions.PermissionsServicesFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * Created by ayalaben on 6/27/2017.
 */
public class PermissionsFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionsFilter.class);
    private static final String IRRELEVANT_REQUEST = "Irrelevant_Request";
    private static final String EDIT_ITEM = "Edit_Item";
    private final PermissionsServices permissionsServices;

    public PermissionsFilter() {
        this(PermissionsServicesFactory.getInstance().createInterface());
    }

    PermissionsFilter(PermissionsServices permissionsServices) {
        this.permissionsServices = permissionsServices;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // required by servlet API
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
        throws IOException, ServletException {
        if ((servletRequest instanceof HttpServletRequest) && isRelevant((HttpServletRequest) servletRequest, servletResponse)) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean isRelevant(HttpServletRequest servletRequest, ServletResponse servletResponse) throws IOException {
        String method = servletRequest.getMethod();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT) || method.equals(HttpMethod.DELETE)) {
            String userId = servletRequest.getHeader("USER_ID");
            String itemId = parseItemIdFromPath(servletRequest.getPathInfo());
            if (!itemId.equals(IRRELEVANT_REQUEST) && !permissionsServices.isAllowed(itemId, userId, EDIT_ITEM)) {
                ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_FORBIDDEN);
                servletResponse.getWriter()
                    .print(buildResponse(Response.Status.FORBIDDEN, Messages.PERMISSIONS_ERROR.getErrorMessage(), Messages.PERMISSIONS_ERROR.name()));
                return false;
            }
        }
        return true;
    }

    private String parseItemIdFromPath(String pathInfo) {
        String[] tokens = pathInfo.split("/");
        if (tokens.length < 4) {
            return IRRELEVANT_REQUEST;
        } else {
            return tokens[3];
        }
    }

    @Override
    public void destroy() {
        // required by serlvet API
    }

    private String buildResponse(Response.Status status, String message, String id) {
        ErrorCode errorCode = new ErrorCode.ErrorCodeBuilder().withId(id).withMessage(message).build();
        return objectToJsonString(new ErrorCodeAndMessage(status, errorCode));
    }

    private String objectToJsonString(Object obj) {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return "An internal error has occurred. Please contact support.";
        }
    }
}
