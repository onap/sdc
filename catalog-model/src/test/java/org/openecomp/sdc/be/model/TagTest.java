package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;

public class TagTest {

	private Tag createTestSubject() {
		return new Tag();
	}

	@Test
	public void testGetName() throws Exception {
		Tag testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		Tag testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testHashCode() throws Exception {
		Tag testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		Tag testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		obj = new Object();
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		
		Tag createTestSubject = createTestSubject();
		result = testSubject.equals(createTestSubject);
		Assert.assertEquals(false, result);
		testSubject.setName("mock");
		result = testSubject.equals(createTestSubject);
		Assert.assertEquals(false, result);
		createTestSubject.setName("mock");
		result = testSubject.equals(createTestSubject);
		Assert.assertEquals(true, result);
	}
}