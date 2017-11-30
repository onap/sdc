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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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

import com.google.gson.Gson;

import fj.data.Either;

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
		when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR))
				.thenReturn(webAppContextWrapper);
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

	/*
	 * @Test public void deactivateUserSuccessfullyTest(){ String
	 * userToDeleteUserId = "admin1"; User adminUser = new User();
	 * adminUser.setUserId(ADMIN_ATT_UID); Either<User, ActionStatus>
	 * eitherActiveUser = buildEitherUser(userToDeleteUserId, true); User
	 * userToDelete = eitherActiveUser.left().value();
	 * doReturn(eitherActiveUser).when(userAdminManager).getUser(
	 * userToDeleteUserId);
	 * 
	 * Either<User, ActionStatus> eitherInactiveUser =
	 * buildEitherUser(userToDeleteUserId, false);
	 * doReturn(eitherInactiveUser).when(userAdminManager).deActivateUser(
	 * adminUser, userToDelete.getUserId());
	 * 
	 * 
	 * Response response =
	 * target().path("/v1/user/"+userToDeleteUserId).request().delete();
	 * assertTrue(response.getStatus() == HttpStatus.OK.value());
	 * verify(userAdminManager, times(1)).deActivateUser(adminUser,
	 * userToDelete.getUserId()); }
	 * 
	 * 
	 * @Test public void forceDeleteUserSuccessfullyTest(){ String
	 * userToDeleteUserId = "admin1";
	 * when(request.getHeader(User.FORCE_DELETE_HEADER_FLAG)).thenReturn(User.
	 * FORCE_DELETE_HEADER_FLAG);
	 * 
	 * User adminUser = new User(); adminUser.setUserId(ADMIN_ATT_UID);
	 * 
	 * Either<User, ActionStatus> eitherActiveUser =
	 * buildEitherUser(userToDeleteUserId, true); User userToDelete =
	 * eitherActiveUser.left().value();
	 * doReturn(eitherActiveUser).when(userAdminManager).getUser(
	 * userToDeleteUserId);
	 * 
	 * Either<User, ActionStatus> eitherUser =
	 * buildEitherUser(userToDeleteUserId, true);
	 * doReturn(eitherUser).when(userAdminManager).deleteUser(userToDelete.
	 * getUserId());
	 * 
	 * 
	 * Response response =
	 * target().path("/v1/user/"+userToDeleteUserId).request().delete();
	 * assertTrue(response.getStatus() == HttpStatus.OK.value());
	 * verify(userAdminManager, times(0)).deActivateUser(adminUser,
	 * userToDelete.getUserId()); verify(userAdminManager,
	 * times(1)).deleteUser(userToDelete.getUserId()); }
	 */

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

	private UserAdminServlet createTestSubject() {
		return new UserAdminServlet();
	}

	
	@Test
	public void testGet() throws Exception {
		UserAdminServlet testSubject;
		String userId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetRole() throws Exception {
		UserAdminServlet testSubject;
		String userId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testUpdateUserRole() throws Exception {
		UserAdminServlet testSubject;
		String userIdUpdateUser = "";
		HttpServletRequest request = null;
		String data = "";
		String modifierUserId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testCreateUser() throws Exception {
		UserAdminServlet testSubject;
		HttpServletRequest request = null;
		String newUserData = "";
		String modifierAttId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testAuthorize() throws Exception {
		UserAdminServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		String firstName = "";
		String lastName = "";
		String email = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetAdminsUser() throws Exception {
		UserAdminServlet testSubject;
		String userId = "";
		HttpServletRequest request = null;
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetUsersList() throws Exception {
		UserAdminServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		String roles = "";
		Response result;

		// test 1
		testSubject = createTestSubject();
		roles = null;

		// test 2
		testSubject = createTestSubject();
		roles = "";
	}

	
	@Test
	public void testDeActivateUser() throws Exception {
		UserAdminServlet testSubject;
		String userId = "";
		HttpServletRequest request = null;
		String userIdHeader = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

}
