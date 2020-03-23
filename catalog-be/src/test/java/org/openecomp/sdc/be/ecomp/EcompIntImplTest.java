/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.ecomp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.onap.portalsdk.core.onboarding.exception.PortalAPIException;
import org.onap.portalsdk.core.restful.domain.EcompRole;
import org.onap.portalsdk.core.restful.domain.EcompUser;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.api.Constants;

public class EcompIntImplTest {

	private EcompIntImpl createTestSubject() {
		return new EcompIntImpl();
	}

	@Test
	public void testPushUser()  {
		EcompIntImpl testSubject;
		EcompUser user = null;

		// default test
		testSubject = createTestSubject();
		Assertions.assertThrows(PortalAPIException.class, () -> {
			testSubject.pushUser(user);
		});
	}

	@Test
	public void testEditUser() {
		EcompIntImpl testSubject;
		String loginId = "";
		EcompUser user = null;

		// default test
		testSubject = createTestSubject();
		Assertions.assertThrows(PortalAPIException.class, () -> {
			testSubject.editUser(loginId, user);
		});
	}

	@Test
	public void testGetUser() {
		EcompIntImpl testSubject;
		String loginId = "";

		// default test
		testSubject = createTestSubject();
		Assertions.assertThrows(PortalAPIException.class, () -> {
			EcompUser result = testSubject.getUser(loginId);
		});
	}

	@Test
	public void testGetUsers() {
		EcompIntImpl testSubject;

		// default test
		testSubject = createTestSubject();
		Assertions.assertThrows(PortalAPIException.class, () -> {
			List<EcompUser> result = testSubject.getUsers();
		});

	}

	@Test
	public void testGetAvailableRoles() throws PortalAPIException{
		EcompIntImpl testSubject;

		// default test
		testSubject = createTestSubject();
		List<EcompRole> result = testSubject.getAvailableRoles("mock-id");
		assertThat(result).hasSameSizeAs(Role.values());
	}

	@Test
	public void testGetUserRoles()  {
		EcompIntImpl testSubject;
		String loginId = "";

		// default test
		testSubject = createTestSubject();
		Assertions.assertThrows(PortalAPIException.class, () -> {
			List<EcompRole> result = testSubject.getUserRoles(loginId);
		});
	}

	@Test
	public void testIsAppAuthenticated() throws Exception {
		EcompIntImpl testSubject;
		boolean result;
        HttpServletRequest httpServletRequestImpl = Mockito.mock(HttpServletRequest.class);
        when(httpServletRequestImpl.getHeader("username")).thenReturn("mock-user");
        when(httpServletRequestImpl.getHeader("password")).thenReturn("mock-password");

		// default test
		testSubject  = createTestSubject();
        result = testSubject.isAppAuthenticated(httpServletRequestImpl, null);
        assertThat(result).isFalse();
	}

	@Test
	public void testGetUserId() throws Exception {
		EcompIntImpl testSubject;
		String userId = "mock-user-id";
		HttpServletRequest httpServletRequestImpl = Mockito.mock(HttpServletRequest.class);
		when(httpServletRequestImpl.getHeader(Constants.USER_ID_HEADER)).thenReturn(userId);

		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserId(httpServletRequestImpl);
		assertThat(result).isEqualTo(userId);
	}
}
