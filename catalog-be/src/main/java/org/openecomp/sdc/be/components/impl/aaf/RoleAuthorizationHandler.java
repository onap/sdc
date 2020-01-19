package org.openecomp.sdc.be.components.impl.aaf;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.servlets.BeGenericServlet;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;

import javax.servlet.http.HttpServletRequest;

// aop id defined via application-context.xml. the annotations are only for test purposes
@Aspect
public class RoleAuthorizationHandler {

    private static final Logger log = Logger.getLogger(RoleAuthorizationHandler.class);

    private ConfigurationManager configurationManager = ConfigurationManager.getConfigurationManager();

    @Before("@annotation(permissions)")
    public void authorizeRole(JoinPoint joinPoint, PermissionAllowed permissions) {

        if (isPermissionAuthenticationNeeded()) {
            String methodName = joinPoint.getSignature().toShortString();
            HttpServletRequest request = ((BeGenericServlet) joinPoint.getThis()).getServletRequest();
            String[] perms = permissions.value();
            logAuth(methodName, perms, true, null);
            for (String perm : perms) {
                if (request.isUserInRole(getFullPermission(perm))) {
                    logAuth(methodName, perms, false, true);
                    return;
                }
            }
            logAuth(methodName, perms, false, false);
            throw new ByActionStatusComponentException(ActionStatus.AUTH_FAILED);
        }

    }

    private void logAuth(String methodName, String[] perms, boolean beforeAuth, Boolean success) {
        if (beforeAuth)
            log.trace("#{} - authorizing before invoking endpoint {}", methodName);
        else {
            String status = success ? "SUCCESS" : "FAILED";
            log.trace("#{} - authorizing before invoking endpoint {}, Status: {}", methodName, status);
        }
    }

    private String getFullPermission(String role) {
        return AafPermission.getEnumByString(role).getFullPermission();
    }

    private boolean isPermissionAuthenticationNeeded() {
        if (configurationManager.getConfiguration().getAafAuthNeeded() && ThreadLocalsHolder.isExternalRequest()) {
            return true;
        } else return false;
    }
}
