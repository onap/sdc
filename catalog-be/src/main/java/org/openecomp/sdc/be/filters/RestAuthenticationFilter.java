/*-
 * ============LICENSE_START=======================================================
 * ONAP SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 * ===================================================================
 *
 */

package org.openecomp.sdc.be.filters;

import fj.data.Either;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openecomp.sdc.be.config.BeEcompErrorManager;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;

public class RestAuthenticationFilter implements Filter {

    private static final Logger log = Logger.getLogger(RestAuthenticationFilter.class);
    private UserBusinessLogic userBusinessLogic = getUserBusinessLogic();


    private UserBusinessLogic getUserBusinessLogic() {
        ApplicationContext ctx = ContextLoader.getCurrentWebApplicationContext();
        return (UserBusinessLogic) ctx.getBean("userBusinessLogic");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filter)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;

            String authHeader = httpServletRequest.getHeader(Constants.AUTHORIZATION_HEADER);

            if (authHeader != null) {
                boolean authenticationStatus = authenticate(authHeader);

                if (authenticationStatus) {
                    filter.doFilter(request, response);
                } else {
                    unauthorized(response);
                }
            } else {
                unauthorized(response);
            }
        }
    }

    private void unauthorized(ServletResponse response) {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private boolean authenticate(String authCredentials) {

        if (null == authCredentials) {
            return false;
        }

        final String encodedUserPassword = authCredentials.replaceFirst("Basic" + " ", "");
        String usernameAndPassword = null;
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedUserPassword);
            usernameAndPassword = new String(decodedBytes, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
        final String username = tokenizer.nextToken();

        try {
            Either<List<User>, ResponseFormat> either = userBusinessLogic.getAllAdminUsers();

            if (either.isRight()) {
                return false;
            } else {
                if (either.left().value() != null) {
                    List<User> users = either.left().value();
                    Optional<User> user = users.stream().filter(x -> x.getUserId().equals(username)).findFirst();
                    return user.isPresent();
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            BeEcompErrorManager.getInstance().logBeRestApiGeneralError("Get All Administrators");
            log.debug("get all admins failed with unexpected error: {}", e);
        }
        return false;
    }

    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
    }
}
