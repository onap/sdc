package org.openecomp.sdc.be.user;

import org.junit.Test;

public class UserAdminValidatorTest {

	private UserAdminValidator createTestSubject() {
		return UserAdminValidator.getInstance();
	}

	@Test
	public void testGetInstance() throws Exception {
		UserAdminValidator result;

		// default test
		result = UserAdminValidator.getInstance();
	}

	@Test
	public void testValidateEmail() throws Exception {
		UserAdminValidator testSubject;
		String hex = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateEmail(hex);
	}

	@Test
	public void testValidateUserId() throws Exception {
		UserAdminValidator testSubject;
		String userId = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateUserId(userId);
	}

	@Test
	public void testValidateRole() throws Exception {
		UserAdminValidator testSubject;
		String role = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateRole(role);
	}
}