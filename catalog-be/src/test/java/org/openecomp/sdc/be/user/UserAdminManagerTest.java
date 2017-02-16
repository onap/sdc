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

package org.openecomp.sdc.be.user;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.mockito.Mockito;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.api.IUsersDAO;
import org.openecomp.sdc.be.dao.impl.Neo4jUsersDAO;
import org.openecomp.sdc.be.resources.data.UserData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;

public class UserAdminManagerTest {

	final IUsersDAO usersDao = Mockito.mock(Neo4jUsersDAO.class);
	Gson gson;

	@Before
	public void setup() {
		Either<UserData, ActionStatus> eitherOk = Either.right(ActionStatus.OK);
		gson = new GsonBuilder().setPrettyPrinting().create();
		when(usersDao.getUserData(anyString())).thenReturn(eitherOk);
		when(usersDao.saveUserData((UserData) anyObject())).thenReturn(ActionStatus.OK);
		when(usersDao.updateUserData((UserData) anyObject())).thenReturn(ActionStatus.OK);
		when(usersDao.deleteUserData(anyString())).thenReturn(ActionStatus.OK);
	}

	// @Test
	// public void testCreateUser() {
	// String json = "{\"firstName\": \"James\",\"lastName\":
	// \"Brown\",\"userId\": \"jb1234u\",\"email\":
	// \"jb1234u@sdc.com\",\"role\": \"ADMIN\"}";
	// UserData user = gson.fromJson(json, UserData.class);
	// Either<UserData,ActionStatus> either =
	// UserAdminManager.getInstance().createUser(user);
	// assertTrue(either.isRight());
	// assertEquals(ActionStatus.OK, either.right().value());
	// }
	//
	//
	// @Test
	// public void testCreateUserInvalidEmail() {
	// String json = "{\"firstName\": \"James\",\"lastName\":
	// \"Brown\",\"userId\": \"jb1234u\",\"email\": \"@sdc.com\",\"role\":
	// \"ADMIN\"}";
	// UserData user = gson.fromJson(json, UserData.class);
	// Either<UserData,ActionStatus> either =
	// UserAdminManager.getInstance().createUser(user);
	// assertTrue(either.isRight());
	// assertEquals(ActionStatus.INVALID_EMAIL_ADDRESS, either.right().value());
	// }
	//
	// @Test
	// public void testCreateUserInvalidRole() {
	// String json = "{\"firstName\": \"James\",\"lastName\":
	// \"Brown\",\"userId\": \"jb1234u\",\"email\":
	// \"jb1234u@sdc.com\",\"role\": \"MIN\"}";
	// UserData user = gson.fromJson(json, UserData.class);
	// Either<UserData,ActionStatus> either =
	// UserAdminManager.getInstance().createUser(user);
	// assertTrue(either.isRight());
	// assertEquals(ActionStatus.INVALID_EMAIL_ADDRESS, either.right().value());
	// }

}
