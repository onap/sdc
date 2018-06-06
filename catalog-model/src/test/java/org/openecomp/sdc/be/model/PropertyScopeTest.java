package org.openecomp.sdc.be.model;

import org.junit.Assert;
import org.junit.Test;

public class PropertyScopeTest {

	private PropertyScope createTestSubject() {
		return new PropertyScope();
	}
	
	@Test
	public void testGetName() throws Exception {
		PropertyScope testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		PropertyScope testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testHashCode() throws Exception {
		PropertyScope testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		PropertyScope testSubject;
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
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(false, result);
		testSubject.setName("mock");
		PropertyScope createTestSubject2 = createTestSubject();
		result = testSubject.equals(createTestSubject2);
		Assert.assertEquals(false, result);
		createTestSubject2.setName("mock");
		result = testSubject.equals(createTestSubject2);
		Assert.assertEquals(true, result);
	}
}