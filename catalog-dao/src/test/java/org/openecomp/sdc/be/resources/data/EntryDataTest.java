package org.openecomp.sdc.be.resources.data;

import org.junit.Test;

public class EntryDataTest {

	private EntryData createTestSubject() {
		return new EntryData(new Object(), new Object());
	}

	@Test
	public void testGetKey() throws Exception {
		EntryData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getKey();
	}

	@Test
	public void testGetValue() throws Exception {
		EntryData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testSetValue() throws Exception {
		EntryData testSubject;
		Object value = null;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setValue(value);
	}
}