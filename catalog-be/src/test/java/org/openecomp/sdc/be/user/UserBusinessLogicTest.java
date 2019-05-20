package org.openecomp.sdc.be.user;

import fj.data.Either;
import mockit.Deencapsulation;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.util.detached.DetachedEdge;
import org.javatuples.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphGenericDao;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IUserAdminOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.UserRoleEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class UserBusinessLogicTest {

	@InjectMocks
	UserBusinessLogic testSubject;
	@Mock
	private IUserAdminOperation userAdminOperation;
	@Mock
	private ComponentsUtils componentsUtils;
	@Mock
	private JanusGraphGenericDao janusGraphDao;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetUser() throws Exception {
		String userId = "";
		boolean inTransaction = false;
		Either<User, ActionStatus> result;

		// default test
		result = testSubject.getUser(userId, inTransaction);
	}

	@Test
	public void testCreateUser() throws Exception {
		User modifier = new User();
		User newUser = new User();
		Either<User, ResponseFormat> result;

		// default test
		result = testSubject.createUser(modifier, newUser);
	}

	@Test
	public void testCreateUserErrorGetUser() throws Exception {
		User modifier = new User();
		User newUser = new User();
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.right(ActionStatus.USER_NOT_FOUND);
		Mockito.when(userAdminOperation.getUserData(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(value);

		// default test
		modifier.setUserId("mock");
		result = testSubject.createUser(modifier, newUser);
	}

	@Test
	public void testCreateUserErrorUserNotAdmin() throws Exception {
		User modifier = new User();
		User newUser = new User();
		Either<User, ResponseFormat> result;

		User userFromDb = new User();
		userFromDb.setRole(UserRoleEnum.DESIGNER.getName());
		Either<User, ActionStatus> value = Either.left(userFromDb);
		Mockito.when(userAdminOperation.getUserData(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(value);

		// default test
		modifier.setUserId("mock");
		result = testSubject.createUser(modifier, newUser);
	}

	@Test
	public void testCreateErrorCheckingNewUser() throws Exception {
		User modifier = new User();
		User newUser = new User();
		Either<User, ResponseFormat> result;

		User userFromDb = new User();
		userFromDb.setRole(UserRoleEnum.ADMIN.getName());
		Either<User, ActionStatus> value = Either.left(userFromDb);
		Either<User, ActionStatus> value2 = Either.right(ActionStatus.AUTH_REQUIRED);
		Mockito.when(userAdminOperation.getUserData("mockModif", false)).thenReturn(value);
		Mockito.when(userAdminOperation.getUserData("mockNewUs", false)).thenReturn(value2);

		// default test
		modifier.setUserId("mockModif");
		newUser.setUserId("mockNewUs");
		result = testSubject.createUser(modifier, newUser);
	}

	@Test
	public void testCreateErrorCheckingNewUser2() throws Exception {
		User modifier = new User();
		User newUser = new User();
		Either<User, ResponseFormat> result;

		User userFromDb = new User();
		userFromDb.setRole(UserRoleEnum.ADMIN.getName());
		Either<User, ActionStatus> value = Either.left(userFromDb);
		Either<User, ActionStatus> value2 = Either.right(ActionStatus.USER_ALREADY_EXIST);
		Mockito.when(userAdminOperation.getUserData("mockModif", false)).thenReturn(value);
		Mockito.when(userAdminOperation.getUserData("mockNewUs", false)).thenReturn(value2);

		// default test
		modifier.setUserId("mockModif");
		newUser.setUserId("mockNewUs");
		result = testSubject.createUser(modifier, newUser);
	}

	@Test
	public void testCreate2() throws Exception {
		User modifier = new User();
		User newUser = new User();
		Either<User, ResponseFormat> result;

		modifier.setUserId("mockModif");
		newUser.setUserId("mockNewUs");

		User userFromDb = new User();
		userFromDb.setRole(UserRoleEnum.ADMIN.getName());
		Either<User, ActionStatus> value = Either.left(userFromDb);

		User userFromDb2 = new User();
		Either<User, ActionStatus> value2 = Either.left(userFromDb2);
		Mockito.when(userAdminOperation.getUserData("mockModif", false)).thenReturn(value);
		Mockito.when(userAdminOperation.getUserData("mockNewUs", false)).thenReturn(value2);

		// default test
		result = testSubject.createUser(modifier, newUser);
	}

	@Test
	public void testCreateInvalidMail() throws Exception {
		User modifier = new User();
		User newUser = new User();
		Either<User, ResponseFormat> result;

		modifier.setUserId("mockModif");
		newUser.setUserId("mockNewUs");
		newUser.setEmail("mock");

		User userFromDbAdmin = new User();
		userFromDbAdmin.setRole(UserRoleEnum.ADMIN.getName());
		Either<User, ActionStatus> value = Either.left(userFromDbAdmin);

		User userFromDbNew = new User();
		userFromDbNew.setStatus(UserStatusEnum.INACTIVE);
		Either<User, ActionStatus> value2 = Either.left(userFromDbNew);
		Mockito.when(userAdminOperation.getUserData("mockModif", false)).thenReturn(value);
		Mockito.when(userAdminOperation.getUserData("mockNewUs", false)).thenReturn(value2);

		// default test
		result = testSubject.createUser(modifier, newUser);
	}

	@Test
	public void testCreateInvalidRole() throws Exception {
		User modifier = new User();
		User newUser = new User();
		Either<User, ResponseFormat> result;

		modifier.setUserId("mockModif");
		newUser.setUserId("mockNewUs");
		newUser.setEmail("mock@mock.mock");
		newUser.setRole("mock");

		User userFromDbAdmin = new User();
		userFromDbAdmin.setRole(UserRoleEnum.ADMIN.getName());
		Either<User, ActionStatus> value = Either.left(userFromDbAdmin);

		User userFromDbNew = new User();
		userFromDbNew.setStatus(UserStatusEnum.INACTIVE);
		Either<User, ActionStatus> value2 = Either.left(userFromDbNew);
		Mockito.when(userAdminOperation.getUserData("mockModif", false)).thenReturn(value);
		Mockito.when(userAdminOperation.getUserData("mockNewUs", false)).thenReturn(value2);

		// default test
		result = testSubject.createUser(modifier, newUser);
	}

	@Test
	public void testUpdateUserRoleNoId() throws Exception {
		User modifier = new User();
		String userIdToUpdate = "";
		String userRole = "";
		Either<User, ResponseFormat> result;

		// default test
		result = testSubject.updateUserRole(modifier, userIdToUpdate, userRole);
	}

	@Test
	public void testUpdateUserRoleNotFound() throws Exception {
		User modifier = new User();
		modifier.setUserId("mock");
		String userIdToUpdate = "";
		String userRole = "";
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.right(ActionStatus.USER_NOT_FOUND);
		Mockito.when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(value);

		// default test
		result = testSubject.updateUserRole(modifier, userIdToUpdate, userRole);
	}

	@Test
	public void testUpdateUserRoleModifWrongRole() throws Exception {
		User modifier = new User();
		modifier.setUserId("mock");
		modifier.setRole(UserRoleEnum.DESIGNER.getName());
		String userIdToUpdate = "";
		String userRole = "";
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(modifier);
		Mockito.when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(value);

		// default test
		result = testSubject.updateUserRole(modifier, userIdToUpdate, userRole);
	}

	@Test
	public void testUpdateUserRoleSameId() throws Exception {
		User modifier = new User();
		modifier.setUserId("mock");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userIdToUpdate = "mock";
		String userRole = "";
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(modifier);
		Mockito.when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(value);

		// default test
		result = testSubject.updateUserRole(modifier, userIdToUpdate, userRole);
	}

	@Test
	public void testUpdateUserRoleUpdatedNotFound() throws Exception {
		User modifier = new User();
		modifier.setUserId("mock");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userIdToUpdate = "mock1";
		String userRole = "";
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(modifier);
		Either<User, ActionStatus> value2 = Either.right(ActionStatus.ECOMP_USER_NOT_FOUND);
		Mockito.when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(value);
		Mockito.when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(value2);

		// default test
		result = testSubject.updateUserRole(modifier, userIdToUpdate, userRole);
	}

	@Test
	public void testUpdateUserRoleUpdatedToInvalidRole() throws Exception {
		User modifier = new User();
		modifier.setUserId("mock");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userIdToUpdate = "mock1";
		String userRole = "";
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(modifier);
		Mockito.when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(value);
		Mockito.when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(value);

		// default test
		result = testSubject.updateUserRole(modifier, userIdToUpdate, userRole);
	}

	@Test
	public void testUpdateUserRolePendingTaskFailed() throws Exception {
		User modifier = new User();
		modifier.setUserId("mock");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userIdToUpdate = "mock1";
		String userRole = UserRoleEnum.DESIGNER.getName();
		Either<User, ResponseFormat> result;

		User updatedUser = new User();
		updatedUser.setUserId(userIdToUpdate);
		updatedUser.setRole(UserRoleEnum.TESTER.getName());

		Either<User, ActionStatus> value = Either.left(modifier);
		Either<User, ActionStatus> value2 = Either.left(updatedUser);
		Mockito.when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(value);
		Mockito.when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(value2);

		Either<List<Edge>, StorageOperationStatus> value3 = Either.right(StorageOperationStatus.INCONSISTENCY);
		Mockito.when(userAdminOperation.getUserPendingTasksList(Mockito.any(), Mockito.any())).thenReturn(value3);

		// default test
		result = testSubject.updateUserRole(modifier, userIdToUpdate, userRole);
	}

	@Test
	public void testUpdateUserRoleListOfTasksNotEmpty() throws Exception {
		User modifier = new User();
		modifier.setUserId("mock");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userIdToUpdate = "mock1";
		String userRole = UserRoleEnum.DESIGNER.getName();
		Either<User, ResponseFormat> result;

		User updatedUser = new User();
		updatedUser.setUserId(userIdToUpdate);
		updatedUser.setRole(UserRoleEnum.TESTER.getName());

		Either<User, ActionStatus> value = Either.left(modifier);
		Either<User, ActionStatus> value2 = Either.left(updatedUser);
		Mockito.when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(value);
		Mockito.when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(value2);
		List<Edge> list = new LinkedList<>();

		list.add(new DetachedEdge("sdas", "fdfs", new HashMap<>(),"sadas","sadasd",
				"sadas","sadasd" ));
		Either<List<Edge>, StorageOperationStatus> value3 = Either.left(list);
		Mockito.when(userAdminOperation.getUserPendingTasksList(Mockito.any(), Mockito.any())).thenReturn(value3);

		// default test
		result = testSubject.updateUserRole(modifier, userIdToUpdate, userRole);
	}

	@Test
	public void testUpdateUserRoleFailToUpdate() throws Exception {
		User modifier = new User();
		modifier.setUserId("mock");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userIdToUpdate = "mock1";
		String userRole = UserRoleEnum.DESIGNER.getName();
		Either<User, ResponseFormat> result;

		User updatedUser = new User();
		updatedUser.setUserId(userIdToUpdate);
		updatedUser.setRole(UserRoleEnum.TESTER.getName());

		Either<User, ActionStatus> value = Either.left(modifier);
		Either<User, ActionStatus> value2 = Either.left(updatedUser);
		Mockito.when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(value);
		Mockito.when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(value2);
		List<Edge> list = new LinkedList<>();

		Either<List<Edge>, StorageOperationStatus> value3 = Either.left(list);
		Mockito.when(userAdminOperation.getUserPendingTasksList(Mockito.any(), Mockito.any())).thenReturn(value3);
		Either<User, StorageOperationStatus> value4 = Either.right(StorageOperationStatus.INCONSISTENCY);
		Mockito.when(userAdminOperation.updateUserData(Mockito.any())).thenReturn(value4);
		// default test
		result = testSubject.updateUserRole(modifier, userIdToUpdate, userRole);
	}

	@Test
	public void testUpdateUserRole() throws Exception {
		User modifier = new User();
		modifier.setUserId("mock");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userIdToUpdate = "mock1";
		String userRole = UserRoleEnum.DESIGNER.getName();
		Either<User, ResponseFormat> result;

		User updatedUser = new User();
		updatedUser.setUserId(userIdToUpdate);
		updatedUser.setRole(UserRoleEnum.TESTER.getName());

		Either<User, ActionStatus> value = Either.left(modifier);
		Either<User, ActionStatus> value2 = Either.left(updatedUser);
		Mockito.when(userAdminOperation.getUserData(modifier.getUserId(), false)).thenReturn(value);
		Mockito.when(userAdminOperation.getUserData(userIdToUpdate, false)).thenReturn(value2);
		List<Edge> list = new LinkedList<>();

		Either<List<Edge>, StorageOperationStatus> value3 = Either.left(list);
		Mockito.when(userAdminOperation.getUserPendingTasksList(Mockito.any(), Mockito.any())).thenReturn(value3);
		Either<User, StorageOperationStatus> value4 = Either.left(updatedUser);
		Mockito.when(userAdminOperation.updateUserData(Mockito.any())).thenReturn(value4);
		// default test
		result = testSubject.updateUserRole(modifier, userIdToUpdate, userRole);
	}

	@Test
	public void testGetAllAdminUsers() throws Exception {
		ServletContext context = null;
		Either<List<User>, ResponseFormat> result;

		Either<List<User>, ActionStatus> response = Either.left(new LinkedList<>());
		Mockito.when(userAdminOperation.getAllUsersWithRole(Mockito.anyString(), Mockito.nullable(String.class)))
				.thenReturn(response);
		// default test
		result = testSubject.getAllAdminUsers();
	}

	@Test
	public void testGetAllAdminUsersFail() throws Exception {
		ServletContext context = null;
		Either<List<User>, ResponseFormat> result;

		Either<List<User>, ActionStatus> response = Either.right(ActionStatus.NOT_ALLOWED);
		Mockito.when(userAdminOperation.getAllUsersWithRole(Mockito.anyString(), Mockito.nullable(String.class)))
				.thenReturn(response);
		// default test
		result = testSubject.getAllAdminUsers();
	}

	@Test
	public void testGetUsersList1() throws Exception {
		String modifierAttId = "";
		List<String> roles = null;
		String rolesStr = "";
		Either<List<User>, ResponseFormat> result;

		// test 1
		modifierAttId = null;
		result = testSubject.getUsersList(modifierAttId, roles, rolesStr);
	}

	@Test
	public void testGetUsersListFail() throws Exception {
		String modifierAttId = "mockMod";
		List<String> roles = null;
		String rolesStr = "";
		Either<List<User>, ResponseFormat> result;

		Either<User, ActionStatus> value3 = Either.right(ActionStatus.ILLEGAL_COMPONENT_STATE);
		Mockito.when(userAdminOperation.getUserData(modifierAttId, false)).thenReturn(value3);

		result = testSubject.getUsersList(modifierAttId, roles, rolesStr);
	}

	@Test
	public void testGetUsersListFail2() throws Exception {
		String modifierAttId = "mockMod";
		List<String> roles = null;
		String rolesStr = "";
		Either<List<User>, ResponseFormat> result;

		Either<User, ActionStatus> value3 = Either.right(ActionStatus.USER_NOT_FOUND);
		Mockito.when(userAdminOperation.getUserData(modifierAttId, false)).thenReturn(value3);

		result = testSubject.getUsersList(modifierAttId, roles, rolesStr);
	}

	
	@Test
	public void testGetUsersList() throws Exception {
		String modifierAttId = "mockMod";
		List<String> roles = new LinkedList<>();
		String rolesStr = "";
		Either<List<User>, ResponseFormat> result;

		User a = new User();
		Either<User, ActionStatus> value3 = Either.left(a);
		Mockito.when(userAdminOperation.getUserData(modifierAttId, false)).thenReturn(value3);
		Either<List<User>, ActionStatus> value = Either.left(new LinkedList<>());
		Mockito.when(userAdminOperation.getAllUsersWithRole(Mockito.nullable(String.class), Mockito.anyString()))
				.thenReturn(value);

		result = testSubject.getUsersList(modifierAttId, roles, rolesStr);
	}

	@Test
	public void testGetUsersListInvalidRole() throws Exception {
		String modifierAttId = "mockMod";
		List<String> roles = new LinkedList<>();
		roles.add("mock");
		String rolesStr = "";
		Either<List<User>, ResponseFormat> result;

		User a = new User();
		Either<User, ActionStatus> value3 = Either.left(a);
		Mockito.when(userAdminOperation.getUserData(modifierAttId, false)).thenReturn(value3);
		Either<List<User>, ActionStatus> value = Either.left(new LinkedList<>());
		Mockito.when(userAdminOperation.getAllUsersWithRole(Mockito.nullable(String.class), Mockito.anyString()))
				.thenReturn(value);

		result = testSubject.getUsersList(modifierAttId, roles, rolesStr);
	}

	@Test
	public void testGetUsersList2() throws Exception {
		String modifierAttId = "mockMod";
		List<String> roles = new LinkedList<>();
		roles.add(UserRoleEnum.DESIGNER.name());
		String rolesStr = "";
		Either<List<User>, ResponseFormat> result;

		User a = new User();
		Either<User, ActionStatus> value3 = Either.left(a);
		Mockito.when(userAdminOperation.getUserData(modifierAttId, false)).thenReturn(value3);
		Either<List<User>, ActionStatus> value = Either.left(new LinkedList<>());
		Mockito.when(userAdminOperation.getAllUsersWithRole(Mockito.nullable(String.class), Mockito.anyString()))
				.thenReturn(value);

		result = testSubject.getUsersList(modifierAttId, roles, rolesStr);
	}


	@Test
	public void testDeActivateUserMissingID() throws Exception {
		User modifier = new User();
		modifier.setUserId(null);
		String userUniuqeIdToDeactive = "";
		Either<User, ResponseFormat> result;

		// default test
		result = testSubject.deActivateUser(modifier, userUniuqeIdToDeactive);
	}

	@Test
	public void testDeActivateUserModifierNotFound() throws Exception {
		User modifier = new User();
		modifier.setUserId("mockMod");
		String userUniuqeIdToDeactive = "";
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.right(ActionStatus.USER_NOT_FOUND);
		Mockito.when(userAdminOperation.getUserData("mockMod", false)).thenReturn(value);

		// default test
		result = testSubject.deActivateUser(modifier, userUniuqeIdToDeactive);
	}

	@Test
	public void testDeActivateUserModNotAdmin() throws Exception {

		User modifier = new User();
		modifier.setUserId("mockMod");
		modifier.setRole(UserRoleEnum.DESIGNER.getName());
		String userUniuqeIdToDeactive = "";
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(modifier);
		Mockito.when(userAdminOperation.getUserData("mockMod", false)).thenReturn(value);

		// default test
		result = testSubject.deActivateUser(modifier, userUniuqeIdToDeactive);
	}

	@Test
	public void testDeActivateUserDeacUserNotFound() throws Exception {

		User modifier = new User();
		modifier.setUserId("mockMod");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userUniuqeIdToDeactive = "mockDU";
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(modifier);
		Mockito.when(userAdminOperation.getUserData("mockMod", false)).thenReturn(value);
		Either<User, ActionStatus> value2 = Either.right(ActionStatus.USER_NOT_FOUND);
		Mockito.when(userAdminOperation.getUserData("mockDU", false)).thenReturn(value2);

		// default test
		result = testSubject.deActivateUser(modifier, userUniuqeIdToDeactive);
	}

	@Test
	public void testDeActivateUserDeacAndModSame() throws Exception {

		User modifier = new User();
		modifier.setUserId("mockMod");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userUniuqeIdToDeactive = "mockMod";
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(modifier);
		Mockito.when(userAdminOperation.getUserData("mockMod", false)).thenReturn(value);
		Either<User, ActionStatus> value2 = Either.left(modifier);
		Mockito.when(userAdminOperation.getUserData("mockDU", false)).thenReturn(value2);

		// default test
		result = testSubject.deActivateUser(modifier, userUniuqeIdToDeactive);
	}

	@Test
	public void testDeActivateUserAlreadyInactive() throws Exception {

		User modifier = new User();
		modifier.setUserId("mockMod");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userUniuqeIdToDeactive = "mockDU";
		User deacUser = new User();
		deacUser.setStatus(UserStatusEnum.INACTIVE);
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(modifier);
		Mockito.when(userAdminOperation.getUserData("mockMod", false)).thenReturn(value);
		Either<User, ActionStatus> value2 = Either.left(deacUser);
		Mockito.when(userAdminOperation.getUserData("mockDU", false)).thenReturn(value2);

		// default test
		result = testSubject.deActivateUser(modifier, userUniuqeIdToDeactive);
	}

	@Test
	public void testDeActivateUserFailToGetTasks() throws Exception {

		User modifier = new User();
		modifier.setUserId("mockMod");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userUniuqeIdToDeactive = "mockDU";
		User deacUser = new User();
		deacUser.setStatus(UserStatusEnum.ACTIVE);
		deacUser.setRole(UserRoleEnum.DESIGNER.name());
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(modifier);
		Mockito.when(userAdminOperation.getUserData("mockMod", false)).thenReturn(value);
		Either<User, ActionStatus> value2 = Either.left(deacUser);
		Mockito.when(userAdminOperation.getUserData("mockDU", false)).thenReturn(value2);
		Either<List<Edge>, StorageOperationStatus> value3 = Either.right(StorageOperationStatus.INCONSISTENCY);
		Mockito.when(userAdminOperation.getUserPendingTasksList(Mockito.any(), Mockito.any())).thenReturn(value3);

		// default test
		result = testSubject.deActivateUser(modifier, userUniuqeIdToDeactive);
	}

	@Test
	public void testDeActivateUserWithPendingTasks() throws Exception {

		User modifier = new User();
		modifier.setUserId("mockMod");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userUniuqeIdToDeactive = "mockDU";
		User deacUser = new User();
		deacUser.setStatus(UserStatusEnum.ACTIVE);
		deacUser.setRole(UserRoleEnum.DESIGNER.name());
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(modifier);
		Mockito.when(userAdminOperation.getUserData("mockMod", false)).thenReturn(value);
		Either<User, ActionStatus> value2 = Either.left(deacUser);
		Mockito.when(userAdminOperation.getUserData("mockDU", false)).thenReturn(value2);
		LinkedList<Edge> a = new LinkedList<>();
		a.add(new DetachedEdge(userUniuqeIdToDeactive, userUniuqeIdToDeactive, new HashMap<>(),
				"dsfds","dsfds", "dsfds", "dsfds"));
		Either<List<Edge>, StorageOperationStatus> value3 = Either.left(a);
		Mockito.when(userAdminOperation.getUserPendingTasksList(Mockito.any(), Mockito.any())).thenReturn(value3);

		// default test
		result = testSubject.deActivateUser(modifier, userUniuqeIdToDeactive);
	}

	@Test
	public void testDeActivateUserDeacFail() throws Exception {

		User modifier = new User();
		modifier.setUserId("mockMod");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userUniuqeIdToDeactive = "mockDU";
		User deacUser = new User();
		deacUser.setStatus(UserStatusEnum.ACTIVE);
		deacUser.setRole(UserRoleEnum.DESIGNER.name());
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(modifier);
		Mockito.when(userAdminOperation.getUserData("mockMod", false)).thenReturn(value);
		Either<User, ActionStatus> value2 = Either.left(deacUser);
		Mockito.when(userAdminOperation.getUserData("mockDU", false)).thenReturn(value2);
		Either<List<Edge>, StorageOperationStatus> value3 = Either.left(new LinkedList<>());
		Mockito.when(userAdminOperation.getUserPendingTasksList(Mockito.any(), Mockito.any())).thenReturn(value3);
		Either<User, StorageOperationStatus> value4 = Either.right(StorageOperationStatus.BAD_REQUEST);
		Mockito.when(userAdminOperation.deActivateUser(deacUser)).thenReturn(value4);
		// default test
		result = testSubject.deActivateUser(modifier, userUniuqeIdToDeactive);
	}

	@Test
	public void testDeActivateUser() throws Exception {

		User modifier = new User();
		modifier.setUserId("mockMod");
		modifier.setRole(UserRoleEnum.ADMIN.getName());
		String userUniuqeIdToDeactive = "mockDU";
		User deacUser = new User();
		deacUser.setStatus(UserStatusEnum.ACTIVE);
		deacUser.setRole(UserRoleEnum.DESIGNER.name());
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(modifier);
		Mockito.when(userAdminOperation.getUserData("mockMod", false)).thenReturn(value);
		Either<User, ActionStatus> value2 = Either.left(deacUser);
		Mockito.when(userAdminOperation.getUserData("mockDU", false)).thenReturn(value2);
		Either<List<Edge>, StorageOperationStatus> value3 = Either.left(new LinkedList<>());
		Mockito.when(userAdminOperation.getUserPendingTasksList(Mockito.any(), Mockito.any())).thenReturn(value3);
		Either<User, StorageOperationStatus> value4 = Either.left(deacUser);
		Mockito.when(userAdminOperation.deActivateUser(deacUser)).thenReturn(value4);
		// default test
		result = testSubject.deActivateUser(modifier, userUniuqeIdToDeactive);
	}

	@Test
	public void testAuthorizeMissingId() throws Exception {
		User authUser = new User();
		Either<User, ResponseFormat> result;

		// default test
		result = testSubject.authorize(authUser);
	}

	@Test
	public void testAuthorizeFail1() throws Exception {
		User authUser = new User();
		authUser.setUserId("mockAU");
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.right(ActionStatus.USER_NOT_FOUND);
		Mockito.when(userAdminOperation.getUserData("mockAU", false)).thenReturn(value);
		// default test
		result = testSubject.authorize(authUser);
	}

	@Test
	public void testAuthorizeFail2() throws Exception {
		User authUser = new User();
		authUser.setUserId("mockAU");
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.right(ActionStatus.AAI_ARTIFACT_GENERATION_FAILED);
		Mockito.when(userAdminOperation.getUserData("mockAU", false)).thenReturn(value);
		// default test
		result = testSubject.authorize(authUser);
	}

	@Test
	public void testAuthorizeFail3() throws Exception {
		User authUser = new User();
		authUser.setUserId("mockAU");
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(null);
		Mockito.when(userAdminOperation.getUserData("mockAU", false)).thenReturn(value);
		// default test
		result = testSubject.authorize(authUser);
	}


	@Test
	public void testAuthorize5() throws Exception {
		User authUser = new User();
		authUser.setUserId("mockAU");
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(authUser);
		Mockito.when(userAdminOperation.getUserData("mockAU", false)).thenReturn(value);
		Either<User, StorageOperationStatus> value2 = Either.left(authUser);
		Mockito.when(userAdminOperation.updateUserData(Mockito.any(User.class))).thenReturn(value2);
		// default test
		result = testSubject.authorize(authUser);
	}

	@Test
	public void testUpdateUserCredentialsMissingId() throws Exception {
		User updatedUserCred = new User();
		updatedUserCred.setUserId(null);
		Either<User, ResponseFormat> result;

		// default test
		result = testSubject.updateUserCredentials(updatedUserCred);
	}

	@Test
	public void testUpdateUserCredentialsFailedToGet() throws Exception {
		User updatedUserCred = new User();
		updatedUserCred.setUserId("mock");
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.right(ActionStatus.USER_NOT_FOUND);
		Mockito.when(userAdminOperation.getUserData("mock", false)).thenReturn(value);

		// default test
		result = testSubject.updateUserCredentials(updatedUserCred);
	}

	@Test
	public void testUpdateUserCredentialsFailedToGet2() throws Exception {
		User updatedUserCred = new User();
		updatedUserCred.setUserId("mock");
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.right(ActionStatus.ADDITIONAL_INFORMATION_ALREADY_EXISTS);
		Mockito.when(userAdminOperation.getUserData("mock", false)).thenReturn(value);

		// default test
		result = testSubject.updateUserCredentials(updatedUserCred);
	}

	@Test
	public void testUpdateUserCredentialsFailedToGet3() throws Exception {
		User updatedUserCred = new User();
		updatedUserCred.setUserId("mock");
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(null);
		Mockito.when(userAdminOperation.getUserData("mock", false)).thenReturn(value);

		// default test
		result = testSubject.updateUserCredentials(updatedUserCred);
	}

	@Test
	public void testUpdateUserCredentials() throws Exception {
		User updatedUserCred = new User();
		updatedUserCred.setUserId("mock");
		Either<User, ResponseFormat> result;

		Either<User, ActionStatus> value = Either.left(updatedUserCred);
		Mockito.when(userAdminOperation.getUserData("mock", false)).thenReturn(value);

		Either<User, StorageOperationStatus> value2 = Either.left(updatedUserCred);
		Mockito.when(userAdminOperation.updateUserData(Mockito.any(User.class))).thenReturn(value2);

		// default test
		result = testSubject.updateUserCredentials(updatedUserCred);
	}

	@Test
	public void testGetPendingUserPendingTasksWithCommit() throws Exception {
		Either<List<Edge>, StorageOperationStatus> result;
		User user = new User();
		for (UserRoleEnum iterable_element : UserRoleEnum.values()) {
			user.setRole(iterable_element.name());
			result = Deencapsulation.invoke(testSubject, "getPendingUserPendingTasksWithCommit", user);
		}

	}

	@Test
	public void testGetUserPendingTaskStatusByRole() throws Exception {
		String result;
		for (UserRoleEnum iterable_element : UserRoleEnum.values()) {
			result = Deencapsulation.invoke(testSubject, "getUserPendingTaskStatusByRole", iterable_element);
		}
	}
}