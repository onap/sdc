package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;

public class CategoryTest {

	private Category createTestSubject() {
		return new Category();
	}

	@Test
	public void testGetName() throws Exception {
		Category testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		Category testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testHashCode() throws Exception {
		Category testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		Category testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(new Object());
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);
		Category createTestSubject = createTestSubject();
		createTestSubject.setName("mock");
		testSubject.setName("mock2");
		result = testSubject.equals(createTestSubject);
		Assert.assertEquals(false, result);
	}

	@Test
	public void testToString() throws Exception {
		Category testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}