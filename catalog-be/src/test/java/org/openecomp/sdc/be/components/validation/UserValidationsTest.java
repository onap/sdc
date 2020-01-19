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

package org.openecomp.sdc.be.components.validation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class UserValidationsTest {

	@InjectMocks
	UserValidations testSubject;
	
	@Mock
	UserBusinessLogic userAdmin;
	
	@Mock
    ComponentsUtils componentsUtils;
	
	@Before
	public void setUp() {
		//TestUtilsSdc.setFinalStatic(UserValidations.class, "log", LoggerFactory.getLogger(UserValidations.class));
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateUserExists() {
		String userId = "mock";
		User usr = new User();
		usr.setUserId(userId);
		usr.setStatus(UserStatusEnum.ACTIVE);
		Mockito.when(userAdmin.getUser(Mockito.anyString())).thenReturn(usr);
		// default test
		testSubject.validateUserExists(userId);
	}
	
	@Test
	public void testValidateNonExistingUser2() {
		String userId = "mock";
		String ecompErrorContext = "mock";
		boolean inTransaction = false;
		User result;


		Mockito.when(userAdmin.getUser(Mockito.anyString())).thenThrow(new ByActionStatusComponentException(ActionStatus.USER_NOT_FOUND));

		Throwable thrown = catchThrowable(() -> testSubject.validateUserExists(userId) );
		assertThat(thrown).isInstanceOf(ComponentException.class).hasFieldOrPropertyWithValue("actionStatus" , ActionStatus.USER_NOT_FOUND);

	}

	@Test
	public void testValidateUserRole() {
		User user = new User();
		List<Role> roles = new LinkedList<>();
		roles.add(Role.DESIGNER);

		user.setRole(Role.DESIGNER.name());

		// test 1
		testSubject.validateUserRole(user, roles);
	}

	@Test
	public void testValidateUserExistsActionStatus() {
		String userId = "mock";
		String ecompErrorContext = "mock";
		ActionStatus result;
		User usr = new User();
		
		Mockito.when(userAdmin.getUser(Mockito.anyString())).thenReturn(usr);
		
		// default test
		result = testSubject.validateUserExistsActionStatus(userId);
	}

	@Test
	public void testValidateUserExistsActionStatus2() {
		String userId = "mock";
		String ecompErrorContext = "mock";
		ActionStatus result;
		User usr = new User();
		
		Mockito.when(userAdmin.getUser(Mockito.anyString())).thenThrow(new ByActionStatusComponentException((ActionStatus.USER_NOT_FOUND)));
		
		// default test
		result = testSubject.validateUserExistsActionStatus(userId);
	}
	
	@Test
	public void testValidateUserNotEmpty() {
		User user = new User();
		user.setUserId("userId");
		String ecompErrorContext = "mock";
		User result;

		// default test
		result = testSubject.validateUserNotEmpty(user, ecompErrorContext);
	}

	@Test
	public void testValidateNonExistingUser() {
		String userId = "";
		String ecompErrorContext = "";

		Mockito.when(userAdmin.getUser(Mockito.anyString())).thenThrow(new ByActionStatusComponentException(ActionStatus.USER_NOT_FOUND));
		
		// default test
		Throwable thrown = catchThrowable(() -> testSubject.validateUserExists(userId) );
		assertThat(thrown).isInstanceOf(ComponentException.class).hasFieldOrPropertyWithValue("actionStatus" , ActionStatus.USER_NOT_FOUND);
	}
}
