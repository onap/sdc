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

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.UserData;
import org.openecomp.sdc.common.api.UserRoleEnum;

import fj.data.Either;

public class UserAdminOperationTest extends ModelTestBase {
	private static final TitanGenericDao titanGenericDao = mock(TitanGenericDao.class);
	@InjectMocks
	private static final UserAdminOperation userAdminOperation = new UserAdminOperation(titanGenericDao);
	private static final String ADMIN = "admin";

	@BeforeClass
	public static void setup() {
		ModelTestBase.init();
	}

	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		Mockito.reset(titanGenericDao);
		mockTitanUpdate();
		mockTitanDelete();

	}

	@Test
	public void testDeActivateUserDataSuccess() {
		UserData userData = mockTitanGet(ADMIN, UserRoleEnum.ADMIN, true);

		Either<User, StorageOperationStatus> eitherUser = userAdminOperation.deActivateUser(userAdminOperation.convertToUser(userData));

		verify(titanGenericDao, times(1)).updateNode(Mockito.eq(userData), Mockito.eq(UserData.class));
		verify(titanGenericDao, times(0)).deleteNode(Mockito.any(UserData.class), Mockito.eq(UserData.class));
		assertTrue(eitherUser.isLeft());
		User user = eitherUser.left().value();
		assertTrue(user.getStatus() == UserStatusEnum.INACTIVE);
	}

	@Test
	public void testDeleteUserWithoutResources() {
		UserData userData = mockTitanGet(ADMIN, UserRoleEnum.ADMIN, true);

		List<Edge> edgesList = new ArrayList<Edge>();

		Either<List<Edge>, TitanOperationStatus> eitherResult = Either.left(edgesList);
		when(titanGenericDao.getEdgesForNode(userData, Direction.BOTH)).thenReturn(eitherResult);

		Either<User, ActionStatus> eitherUser = userAdminOperation.deleteUserData(ADMIN);
		verify(titanGenericDao, times(0)).updateNode(Mockito.any(UserData.class), Mockito.eq(UserData.class));
		verify(titanGenericDao, times(1)).deleteNode(userData, UserData.class);
		assertTrue(eitherUser.isLeft());

	}

	@Test
	public void testDeleteUserWithResources() {
		UserData userData = mockTitanGet(ADMIN, UserRoleEnum.ADMIN, true);

		List<Edge> edgesList = new ArrayList<Edge>();
		edgesList.add(getEmptyEdgeImpl());

		Either<List<Edge>, TitanOperationStatus> eitherResult = Either.left(edgesList);
		when(titanGenericDao.getEdgesForNode(userData, Direction.BOTH)).thenReturn(eitherResult);

		Either<User, ActionStatus> eitherUser = userAdminOperation.deleteUserData(ADMIN);
		verify(titanGenericDao, times(0)).updateNode(Mockito.any(UserData.class), Mockito.eq(UserData.class));
		verify(titanGenericDao, times(0)).deleteNode(Mockito.any(UserData.class), Mockito.eq(UserData.class));
		assertTrue(eitherUser.isRight());
		assertTrue(eitherUser.right().value() == ActionStatus.USER_HAS_ACTIVE_ELEMENTS);

	}

	private Edge getEmptyEdgeImpl() {
		return new Edge() {

			@Override
			public Object id() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String label() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Graph graph() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <V> Property<V> property(String key, V value) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub

			}

			@Override
			public Iterator<Vertex> vertices(Direction direction) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <V> Iterator<Property<V>> properties(String... propertyKeys) {
				// TODO Auto-generated method stub
				return null;
			}

		};
	}

	private UserData mockTitanGet(String userId, UserRoleEnum role, boolean isActive) {
		UserData userData = buildUserData(userId, role, isActive);
		Either<UserData, TitanOperationStatus> eitherUserData = Either.left(userData);
		when(titanGenericDao.getNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), userId, UserData.class)).thenReturn(eitherUserData);
		return userData;
	}

	private static void mockTitanUpdate() {
		doAnswer(new Answer<Either<UserData, TitanOperationStatus>>() {
			public Either<UserData, TitanOperationStatus> answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				UserData retValue = (UserData) args[0];
				Either<UserData, TitanOperationStatus> result = Either.left(retValue);
				return result;
			}

		}).when(titanGenericDao).updateNode(Mockito.any(UserData.class), Mockito.eq(UserData.class));
	}

	private static void mockTitanDelete() {
		doAnswer(new Answer<Either<UserData, TitanOperationStatus>>() {
			public Either<UserData, TitanOperationStatus> answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				UserData retValue = (UserData) args[0];
				Either<UserData, TitanOperationStatus> result = Either.left(retValue);
				return result;
			}

		}).when(titanGenericDao).deleteNode(Mockito.any(UserData.class), Mockito.eq(UserData.class));
	}

	private void assertUserEquals(UserData expected, User actual) {
		assertEquals(expected.getEmail(), actual.getEmail());
		assertEquals(expected.getFirstName(), actual.getFirstName());
		assertEquals(expected.getLastName(), actual.getLastName());
		assertEquals(expected.getRole(), actual.getRole());
		assertEquals(expected.getStatus(), actual.getStatus().name());
		assertEquals(expected.getUserId(), actual.getUserId());

	}

	private static UserData buildUserData(String userId, UserRoleEnum role, boolean isActive) {
		UserData userData = new UserData();
		userData.setUserId(userId);
		userData.setRole(role.getName());
		userData.setEmail("someEmail@somePlace.com");
		userData.setFirstName("israel");
		userData.setLastName("israeli");
		userData.setLastLoginTime(Instant.MIN.getEpochSecond());
		userData.setStatus(isActive ? UserStatusEnum.ACTIVE.name() : UserStatusEnum.INACTIVE.name());
		return userData;
	}
}
