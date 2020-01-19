/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.filters;

import org.apache.commons.collections.CollectionUtils;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.servlets.exception.ComponentExceptionMapper;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.common.datastructure.UserContext;
import org.openecomp.sdc.common.log.enums.EcompLoggerErrorCode;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Component("reqValidationFilter")
public class ReqValidationFilter implements Filter {

    private static final Logger log = Logger.getLogger(ReqValidationFilter.class);
    @Autowired
    public ComponentExceptionMapper componentExceptionMapper;

    @Override
    public void init(FilterConfig filterConfig){

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        try {
            log.debug("Validating User roles - filter");
            List<String> validRoles = Arrays.asList(UserRoleEnum.ADMIN.getName(), UserRoleEnum.DESIGNER.getName());
            UserContext userContext = ThreadLocalsHolder.getUserContext();

            if (userContext != null && CollectionUtils.isNotEmpty(userContext.getUserRoles())) {
                Set<String> userRoles = userContext.getUserRoles();
                if (!userRoles.stream().anyMatch(role -> validRoles.contains(role))) {
                    log.error(EcompLoggerErrorCode.BUSINESS_PROCESS_ERROR, "SDC", "User role is invalid: {}", userRoles);
                    throw new ByActionStatusComponentException(ActionStatus.AUTH_FAILED);
                }
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (ComponentException exp) {
            componentExceptionMapper.writeToResponse(exp, httpResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
