package org.openecomp.sdc.be.filters;

import org.onap.sdc.security.AuthenticationCookie;

import org.onap.sdc.security.IUsersThreadLocalHolder;
import org.onap.sdc.security.PortalClient;
import org.onap.sdc.security.RestrictionAccessFilterException;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.datastructure.UserContext;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.common.util.ThreadLocalsHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ThreadLocalUtils implements IUsersThreadLocalHolder {

    @Autowired
    PortalClient portalClient;

    @Autowired
    UserBusinessLogic userBusinessLogic;

    private static final Logger log = Logger.getLogger(ThreadLocalUtils.class);

    @Override
    public void setUserContext(AuthenticationCookie authenticationCookie) {
        UserContext userContext;
        userContext = new UserContext(authenticationCookie.getUserID(), authenticationCookie.getRoles(), authenticationCookie.getFirstName(), authenticationCookie.getLastName());
        ThreadLocalsHolder.setUserContext(userContext);
    }


    protected void setUserContext(HttpServletRequest httpRequest) {

        String user_id = httpRequest.getHeader(Constants.USER_ID_HEADER);
        if (user_id != null) {
            String userRolesFromPortal = null;
            Set<String> roles = null;
            try {
                userRolesFromPortal = portalClient.fetchUserRolesFromPortal(user_id);
                roles = new HashSet<>(Arrays.asList(userRolesFromPortal));
            } catch (RestrictionAccessFilterException e) {
                log.debug("Failed to fetch user ID - {} from portal", user_id);
                log.debug(e.getMessage());
            }
            UserContext userContext = new UserContext(user_id, roles, null, null);
            ThreadLocalsHolder.setUserContext(userContext);
        } else log.debug("user_id value in req header is null, userContext will not be initialized");
    }

    protected void setUserContextFromDB(HttpServletRequest httpRequest) {
        String user_id = httpRequest.getHeader(Constants.USER_ID_HEADER);
        //there are some internal request that have no user_id header e.g. healthcheck
        if (user_id != null) {
            updateUserContext(user_id);
        } else log.debug("user_id value in req header is null, userContext will not be initialized");
    }

    protected void setUserContextFromDB(AuthenticationCookie authenticationCookie) {
        String user_id = authenticationCookie.getUserID();
        updateUserContext(user_id);
    }

    private void updateUserContext(String user_id) {
        User user = userBusinessLogic.getUser(user_id, false);
        Set<String> roles = new HashSet<>(Arrays.asList(user.getRole()));
        UserContext userContext = new UserContext(user_id, roles, user.getFirstName(), user.getLastName());
        ThreadLocalsHolder.setUserContext(userContext);
    }

}
