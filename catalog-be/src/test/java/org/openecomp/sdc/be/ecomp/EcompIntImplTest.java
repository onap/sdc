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

import org.junit.Test;
import org.mockito.Mockito;
import org.onap.portalsdk.core.onboarding.exception.PortalAPIException;
import org.onap.portalsdk.core.restful.domain.EcompRole;
import org.onap.portalsdk.core.restful.domain.EcompUser;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class EcompIntImplTest {

	private EcompIntImpl createTestSubject() {
		return new EcompIntImpl();
	}

	@Test(expected=PortalAPIException.class)
	public void testPushUser() throws Exception {
		EcompIntImpl testSubject;
		EcompUser user = null;

		// default test
		testSubject = createTestSubject();
		testSubject.pushUser(user);
	}

	@Test(expected=PortalAPIException.class)
	public void testEditUser() throws Exception {
		EcompIntImpl testSubject;
		String loginId = "";
		EcompUser user = null;

		// default test
		testSubject = createTestSubject();
		testSubject.editUser(loginId, user);
	}

	@Test(expected=PortalAPIException.class)
	public void testGetUser() throws Exception {
		EcompIntImpl testSubject;
		String loginId = "";
		EcompUser result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUser(loginId);
	}

	@Test(expected=PortalAPIException.class)
	public void testGetUsers() throws Exception {
		EcompIntImpl testSubject;
		List<EcompUser> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUsers();
	}

	@Test
	public void testGetAvailableRoles() throws Exception {
		EcompIntImpl testSubject;
		List<EcompRole> result;

		// default test
		testSubject = createTestSubject();
        result = testSubject.getAvailableRoles("Mock");
	}

	/*@Test
	public void testPushUserRole() throws Exception {
		EcompIntImpl testSubject;
		String loginId = "";
		List<EcompRole> roles = null;

		// test 1
		testSubject = createTestSubject();
		roles = null;
		testSubject.pushUserRole(loginId, roles);
	}*/

    @Test(expected= PortalAPIException.class)
	public void testGetUserRoles() throws Exception {
		EcompIntImpl testSubject;
		String loginId = "";
		List<EcompRole> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserRoles(loginId);
	}

	@Test
	public void testIsAppAuthenticated() throws Exception {
		EcompIntImpl testSubject;
		boolean result;
        HttpServletRequest httpServletRequestImpl = Mockito.mock(HttpServletRequest.class);
		// default test
		testSubject = createTestSubject();
		result = testSubject.isAppAuthenticated(httpServletRequestImpl);
	}

	@Test
	public void testGetUserId() throws Exception {
		EcompIntImpl testSubject;
        HttpServletRequest httpServletRequestImpl = Mockito.mock(HttpServletRequest.class);
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserId(httpServletRequestImpl);
	}
}
