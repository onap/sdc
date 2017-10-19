package org.openecomp.sdc.asdctool.impl.validator.utils;

import javax.annotation.Generated;

import org.junit.Test;


public class VertexResultTest {

	private VertexResult createTestSubject() {
		return new VertexResult();
	}

	
	@Test
	public void testGetStatus() throws Exception {
		VertexResult testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		VertexResult testSubject;
		boolean status = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetResult() throws Exception {
		VertexResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResult();
	}
}