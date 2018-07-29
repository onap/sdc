package org.openecomp.sdc.be.components.validation;

import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.impl.exceptions.ComponentException;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;

import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class UserValidationsTest {

	@InjectMocks
	UserValidations testSubject;
	
	@Mock
	IUserBusinessLogic userAdmin;
	
	@Mock
    ComponentsUtils componentsUtils;
	
	@Before
	public void setUp() throws Exception {
		//TestUtilsSdc.setFinalStatic(UserValidations.class, "log", LoggerFactory.getLogger(UserValidations.class));
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateUserExists() throws Exception {
		String userId = "mock";
		String ecompErrorContext = "mock";
		User usr = new User();
		boolean inTransaction = false;
		User result;
		
		
		Mockito.when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Either.left(usr));
		
		// default test
		result = testSubject.validateUserExists(userId, ecompErrorContext, inTransaction);
	}
	
	@Test
	public void testValidateNonExistingUser2() throws Exception {
		String userId = "mock";
		String ecompErrorContext = "mock";
		boolean inTransaction = false;
		User result;
		
		
		Mockito.when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Either.right(ActionStatus.USER_NOT_FOUND));

		Throwable thrown = catchThrowable(() -> testSubject.validateUserExists(userId, ecompErrorContext, inTransaction) );
		assertThat(thrown).isInstanceOf(ComponentException.class).hasFieldOrPropertyWithValue("actionStatus" , ActionStatus.AUTH_FAILED);

	}

	@Test
	public void testValidateUserRole() throws Exception {
		User user = new User();
		List<Role> roles = new LinkedList<>();
		roles.add(Role.DESIGNER);
		
		user.setRole(Role.DESIGNER.name());
		
		// test 1
		testSubject.validateUserRole(user, roles);
	}

	@Test
	public void testValidateUserExistsActionStatus() throws Exception {
		String userId = "mock";
		String ecompErrorContext = "mock";
		Either<User, ActionStatus> result;
		User usr = new User();
		
		Mockito.when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Either.left(usr));
		
		// default test
		result = testSubject.validateUserExistsActionStatus(userId, ecompErrorContext);
	}

	@Test
	public void testValidateUserExistsActionStatus2() throws Exception {
		String userId = "mock";
		String ecompErrorContext = "mock";
		Either<User, ActionStatus> result;
		User usr = new User();
		
		Mockito.when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Either.right(ActionStatus.USER_NOT_FOUND));
		
		// default test
		result = testSubject.validateUserExistsActionStatus(userId, ecompErrorContext);
	}
	
	@Test
	public void testValidateUserNotEmpty() throws Exception {
		User user = new User();
		user.setUserId("userId");
		String ecompErrorContext = "mock";
		User result;

		// default test
		result = testSubject.validateUserNotEmpty(user, ecompErrorContext);
	}

	@Test
	public void testValidateNonExistingUser() throws Exception {
		String userId = "";
		String ecompErrorContext = "";

		Mockito.when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Either.right(ActionStatus.USER_NOT_FOUND));
		
		// default test
		Throwable thrown = catchThrowable(() -> testSubject.validateUserExist(userId, ecompErrorContext) );
		assertThat(thrown).isInstanceOf(ComponentException.class).hasFieldOrPropertyWithValue("actionStatus" , ActionStatus.AUTH_FAILED);
	}
}