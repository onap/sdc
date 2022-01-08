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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ByActionStatusComponentException;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;

import java.util.LinkedList;
import java.util.List;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationsTest {

	@InjectMocks
	UserValidations testSubject;
	
	@Mock
	UserBusinessLogic userAdmin;
	
	@BeforeEach
	public void setUp() {
		//TestUtilsSdc.setFinalStatic(UserValidations.class, "log", LoggerFactory.getLogger(UserValidations.class));
		MockitoAnnotations.openMocks(this);
		new ConfigurationManager(new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be"));
	}

	@Test
	public void testValidateUserExists() {
		String userId = "mock";
		User usr = new User();
		usr.setUserId(userId);
		usr.setStatus(UserStatusEnum.INACTIVE);
		Mockito.when(userAdmin.getUser(Mockito.anyString())).thenReturn(usr);

		assertThrows(ByActionStatusComponentException.class, () -> {
			testSubject.validateUserExists(userId);
		});
		Mockito.verify(userAdmin, Mockito.times(1)).getUser(Mockito.anyString());

		// default test
		usr.setStatus(UserStatusEnum.ACTIVE);
		User result = testSubject.validateUserExists(userId);
		assertNotNull(result);
		assertEquals(usr, result);
	}

	@Test
	public void testValidateNonExistingUser2() {
		String userId = "mock";
		Mockito.when(userAdmin.getUser(Mockito.anyString())).thenThrow(new ByActionStatusComponentException(ActionStatus.USER_NOT_FOUND));

		assertThrows(ComponentException.class, () -> {
			testSubject.validateUserExists(userId);
		});
		Mockito.verify(userAdmin, Mockito.times(1)).getUser(Mockito.anyString());
	}

	@Test
	public void testValidateUserRole() {
		User user = new User();
		List<Role> roles = new LinkedList<>();
		user.setRole(Role.DESIGNER.name());

		assertThrows(ByActionStatusComponentException.class, () -> {
			testSubject.validateUserRole(user, roles);
		});

		roles.add(Role.DESIGNER);
		assertDoesNotThrow(() -> testSubject.validateUserRole(user, roles));
	}

	@Test
	public void testValidateUserExistsActionStatus() {
		String userId = "mock";
		ActionStatus result;
		User usr = new User();
		usr.setUserId(userId);
		usr.setStatus(UserStatusEnum.ACTIVE);

		Mockito.when(userAdmin.hasActiveUser(Mockito.anyString())).thenReturn(false);
		assertEquals(ActionStatus.RESTRICTED_OPERATION, testSubject.validateUserExistsActionStatus(userId));
		Mockito.verify(userAdmin, Mockito.times(1)).hasActiveUser(Mockito.anyString());

		Mockito.when(userAdmin.hasActiveUser(Mockito.anyString())).thenReturn(true);
		assertEquals(ActionStatus.OK, testSubject.validateUserExistsActionStatus(userId));
		Mockito.verify(userAdmin, Mockito.times(2)).hasActiveUser(Mockito.anyString());
	}

	@Test
	public void testValidateUserExistsActionStatus2() {
		String userId = "mock";
		Mockito.when(userAdmin.hasActiveUser(Mockito.anyString())).thenThrow(new ByActionStatusComponentException((ActionStatus.USER_NOT_FOUND)));
		
		// default test
		assertThrows(ComponentException.class, () -> {
			testSubject.validateUserExistsActionStatus(userId);
		});
		Mockito.verify(userAdmin, Mockito.times(1)).hasActiveUser(Mockito.anyString());
	}
	
	@Test
	public void testValidateUserNotEmpty() {
		User user = new User();
		user.setUserId("");
		String ecompErrorContext = "mock";
		User result;

		// default test
		assertThrows(ByActionStatusComponentException.class, () -> {
			testSubject.validateUserNotEmpty(user, ecompErrorContext);
		});

		user.setUserId("userId");
		result = testSubject.validateUserNotEmpty(user, ecompErrorContext);
		assertEquals(user, result);
	}

	@Test
	public void testValidateNonExistingUser() {
		String userId = "";

		Mockito.when(userAdmin.getUser(Mockito.anyString())).thenThrow(new ByActionStatusComponentException(ActionStatus.USER_NOT_FOUND));
		
		// default test
		assertThrows(ComponentException.class, () -> {
			testSubject.validateUserExists(userId);
		});
		Mockito.verify(userAdmin, Mockito.times(1)).getUser(Mockito.anyString());
	}
}
