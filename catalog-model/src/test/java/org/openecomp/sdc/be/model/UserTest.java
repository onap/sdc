package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.dao.utils.UserStatusEnum;
import org.openecomp.sdc.be.resources.data.UserData;

public class UserTest {

	private User createTestSubject() {
		return new User();
	}

	@Test
	public void testCtor() throws Exception {
		new User(new User());
		new User(new UserData());
		new User("mock", "mock", "mock", "mock", "mock", 0L);
	}
	
	@Test
	public void testCopyData() throws Exception {
		User testSubject;
		User other = null;

		// default test
		testSubject = createTestSubject();
		testSubject.copyData(other);
		testSubject.copyData(new User());
	}

	@Test
	public void testGetFirstName() throws Exception {
		User testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFirstName();
	}

	@Test
	public void testSetFirstName() throws Exception {
		User testSubject;
		String firstName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFirstName(firstName);
	}

	@Test
	public void testGetLastName() throws Exception {
		User testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastName();
	}

	@Test
	public void testSetLastName() throws Exception {
		User testSubject;
		String lastName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLastName(lastName);
	}

	@Test
	public void testGetUserId() throws Exception {
		User testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserId();
	}

	@Test
	public void testSetUserId() throws Exception {
		User testSubject;
		String userId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUserId(userId);
	}

	@Test
	public void testGetEmail() throws Exception {
		User testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEmail();
	}

	@Test
	public void testSetEmail() throws Exception {
		User testSubject;
		String email = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEmail(email);
	}

	@Test
	public void testGetRole() throws Exception {
		User testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRole();
	}

	@Test
	public void testSetRole() throws Exception {
		User testSubject;
		String role = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRole(role);
	}

	@Test
	public void testGetFullName() throws Exception {
		User testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFullName();
	}

	@Test
	public void testSetLastLoginTime() throws Exception {
		User testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setLastLoginTime();
	}

	@Test
	public void testSetLastLoginTime_1() throws Exception {
		User testSubject;
		Long time = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setLastLoginTime(time);
	}

	@Test
	public void testGetLastLoginTime() throws Exception {
		User testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastLoginTime();
	}

	@Test
	public void testHashCode() throws Exception {
		User testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		User testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);

		result = testSubject.equals(new Object());
		Assert.assertEquals(false, result);
		
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);
	}

	@Test
	public void testGetStatus() throws Exception {
		User testSubject;
		UserStatusEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	@Test
	public void testSetStatus() throws Exception {
		User testSubject;
		UserStatusEnum status = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	@Test
	public void testToString() throws Exception {
		User testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}