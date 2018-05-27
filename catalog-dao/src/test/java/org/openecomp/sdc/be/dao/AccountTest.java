package org.openecomp.sdc.be.dao;

import org.junit.Test;

public class AccountTest {

	private Account createTestSubject() {
		return new Account();
	}

	@Test
	public void testCtor() throws Exception {
		new Account("mock", "mock");
	}
	
	@Test
	public void testGetName() throws Exception {
		Account testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		Account testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testGetEmail() throws Exception {
		Account testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEmail();
	}

	@Test
	public void testSetEmail() throws Exception {
		Account testSubject;
		String email = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEmail(email);
	}

	@Test
	public void testEquals() throws Exception {
		Account testSubject;
		Object other = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(other);
		result = testSubject.equals(testSubject);
		result = testSubject.equals(createTestSubject());
	}

	@Test
	public void testHashCode() throws Exception {
		Account testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}
}