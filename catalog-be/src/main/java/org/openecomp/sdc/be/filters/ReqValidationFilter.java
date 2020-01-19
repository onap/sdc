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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

import javax.servlet.*;
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
