/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.servlets;

import com.google.gson.Gson;
import fj.data.Either;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Application;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class UserAdminServletTest extends JerseyTest {

    final static HttpServletRequest request = mock(HttpServletRequest.class);
    final static HttpSession session = mock(HttpSession.class);
    final static ServletContext servletContext = mock(ServletContext.class);
    final static WebAppContextWrapper webAppContextWrapper = mock(WebAppContextWrapper.class);
    final static WebApplicationContext webApplicationContext = mock(WebApplicationContext.class);
    final static UserBusinessLogic userAdminManager = spy(UserBusinessLogic.class);
    final static AuditingManager auditingManager = mock(AuditingManager.class);
    final static ComponentsUtils componentsUtils = mock(ComponentsUtils.class);
    final static ResponseFormat okResponseFormat = mock(ResponseFormat.class);

    final static String ADMIN_ATT_UID = "jh0003";
    Gson gson = new Gson();

    @BeforeClass
    public static void setup() {
        ExternalConfiguration.setAppName("catalog-be");

        when(session.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
        when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webApplicationContext);

        when(webApplicationContext.getBean(UserBusinessLogic.class)).thenReturn(userAdminManager);
        when(webApplicationContext.getBean(ComponentsUtils.class)).thenReturn(componentsUtils);
        when(componentsUtils.getAuditingManager()).thenReturn(auditingManager);
        when(componentsUtils.getResponseFormat(ActionStatus.OK)).thenReturn(okResponseFormat);
        when(okResponseFormat.getStatus()).thenReturn(HttpStatus.OK.value());

    }

    @Before
    public void beforeTest() {
        reset(userAdminManager);
        doReturn(buildEitherUser(ADMIN_ATT_UID, true)).when(userAdminManager).getUser(ADMIN_ATT_UID, false);

        reset(request);
        when(request.getSession()).thenReturn(session);
        when(request.getHeader("USER_ID")).thenReturn(ADMIN_ATT_UID);
    }

   @Override
    protected Application configure() {

        ResourceConfig resourceConfig = new ResourceConfig(UserAdminServlet.class);

        resourceConfig.register(new AbstractBinder() {

            @Override
            protected void configure() {
                bind(request).to(HttpServletRequest.class);
            }
        });

        return resourceConfig;
    }

    private static Either<User, ActionStatus> buildEitherUser(String userId, boolean isActive) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(UserRoleEnum.ADMIN.getName());
        if (!isActive) {
            user.setStatus(UserStatusEnum.INACTIVE);
        }
        return Either.left(user);
    }

}
