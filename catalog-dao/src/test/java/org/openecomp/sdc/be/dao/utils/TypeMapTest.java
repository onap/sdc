package org.openecomp.sdc.be.dao.utils;

import org.junit.Test;

public class TypeMapTest {

	private TypeMap createTestSubject() {
		return new TypeMap();
	}

	@Test
	public void testPut() throws Exception {
		TypeMap testSubject;
		String key = "mock";
		Object value = new Object();

		// default test
		testSubject = createTestSubject();
		testSubject.put(key, value);
	}

	@Test
	public void testGet() throws Exception {
		TypeMap testSubject;
		Class clazz = Object.class;
		String key = "mock";
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.get(clazz, key);
	}
}