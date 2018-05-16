package org.openecomp.sdc.be.components.validation;

import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.user.IUserBusinessLogic;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.exception.ResponseFormat;
import org.openecomp.sdc.test.utils.TestUtilsSdc;
import org.slf4j.LoggerFactory;import cucumber.api.java.sk.A;
import fj.data.Either;

public class UserValidationsTest {

	@InjectMocks
	UserValidations testSubject;
	
	@Mock
	IUserBusinessLogic userAdmin;
	
	@Mock
    ComponentsUtils componentsUtils;
	
	@Before
	public void setUp() throws Exception {
		TestUtilsSdc.setFinalStatic(UserValidations.class, "log", LoggerFactory.getLogger(UserValidations.class));
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testValidateUserExists() throws Exception {
		String userId = "mock";
		String ecompErrorContext = "mock";
		User usr = new User();
		boolean inTransaction = false;
		Either<User, ResponseFormat> result;
		
		
		Mockito.when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Either.left(usr));
		
		// default test
		result = testSubject.validateUserExists(userId, ecompErrorContext, inTransaction);
	}
	
	@Test
	public void testValidateUserExists2() throws Exception {
		String userId = "mock";
		String ecompErrorContext = "mock";
		boolean inTransaction = false;
		Either<User, ResponseFormat> result;
		
		
		Mockito.when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Either.right(ActionStatus.USER_NOT_FOUND));
		
		// default test
		result = testSubject.validateUserExists(userId, ecompErrorContext, inTransaction);
	}

	@Test
	public void testValidateUserRole() throws Exception {
		User user = new User();
		List<Role> roles = new LinkedList<>();
		Either<Boolean, ResponseFormat> result;
		
		user.setRole(Role.DESIGNER.name());
		
		// test 1
		result = testSubject.validateUserRole(user, roles);
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
		String ecompErrorContext = "mock";
		Either<User, ResponseFormat> result;

		// default test
		result = testSubject.validateUserNotEmpty(user, ecompErrorContext);
	}

	@Test
	public void testValidateUserExist() throws Exception {
		String userId = "";
		String ecompErrorContext = "";
		Wrapper<ResponseFormat> errorWrapper = new Wrapper<>();
		
		Mockito.when(userAdmin.getUser(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(Either.right(ActionStatus.USER_NOT_FOUND));
		
		// default test
		testSubject.validateUserExist(userId, ecompErrorContext, errorWrapper);
	}
}