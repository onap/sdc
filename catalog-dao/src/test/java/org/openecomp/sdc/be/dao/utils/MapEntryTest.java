package org.openecomp.sdc.be.dao.utils;

import org.junit.Test;

public class MapEntryTest {

	private MapEntry createTestSubject() {
		return new MapEntry();
	}

	@Test
	public void testCtor() throws Exception {
		new MapEntry(new Object(), new Object());
	}
	
	@Test
	public void testGetKey() throws Exception {
		MapEntry testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getKey();
	}

	@Test
	public void testSetKey() throws Exception {
		MapEntry testSubject;
		Object key = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setKey(key);
	}

	@Test
	public void testGetValue() throws Exception {
		MapEntry testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testSetValue() throws Exception {
		MapEntry testSubject;
		Object value = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValue(value);
	}
}