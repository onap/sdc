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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
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

public class ThreadLocalUtils implements IUsersThreadLocalHolder {

    private static final Logger log = Logger.getLogger(ThreadLocalUtils.class);
    @Autowired
    PortalClient portalClient;
    @Autowired
    UserBusinessLogic userBusinessLogic;

    @Override
    public void setUserContext(AuthenticationCookie authenticationCookie) {
        UserContext userContext;
        userContext = new UserContext(authenticationCookie.getUserID(), authenticationCookie.getRoles(), authenticationCookie.getFirstName(),
            authenticationCookie.getLastName());
        ThreadLocalsHolder.setUserContext(userContext);
    }

    protected void setUserContext(HttpServletRequest httpRequest) {
        final String userId = httpRequest.getHeader(Constants.USER_ID_HEADER);
        if (userId != null) {
            Set<String> roles = null;
            try {
                final Optional<String> userRolesFromPortalOptional = portalClient.fetchUserRolesFromPortal(userId);
                if (userRolesFromPortalOptional.isPresent()){
                    roles = new HashSet<>(List.of(userRolesFromPortalOptional.get()));
                }
            } catch (RestrictionAccessFilterException e) {
                log.debug("Failed to fetch user ID - {} from portal", userId);
                log.debug(e.getMessage());
            }
            final UserContext userContext = new UserContext(userId, roles, null, null);
            ThreadLocalsHolder.setUserContext(userContext);
        } else {
            log.debug("user_id value in req header is null, userContext will not be initialized");
        }
    }

    protected void setUserContextFromDB(HttpServletRequest httpRequest) {
        String user_id = httpRequest.getHeader(Constants.USER_ID_HEADER);
        //there are some internal request that have no user_id header e.g. healthcheck
        if (user_id != null) {
            updateUserContext(user_id);
        } else {
            log.debug("user_id value in req header is null, userContext will not be initialized");
        }
    }

    private void updateUserContext(String user_id) {
        User user = userBusinessLogic.getUser(user_id, false);
        Set<String> roles = new HashSet<>(Arrays.asList(user.getRole()));
        UserContext userContext = new UserContext(user.getUserId(), roles, user.getFirstName(), user.getLastName());
        ThreadLocalsHolder.setUserContext(userContext);
    }
}
