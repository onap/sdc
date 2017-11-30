package org.openecomp.sdc.asdctool.servlets;

import org.junit.Test;


public class EntryPointTest {

	private EntryPoint createTestSubject() {
		return new EntryPoint();
	}

	
	@Test
	public void testTest() throws Exception {
		EntryPoint testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.test();
	}
}