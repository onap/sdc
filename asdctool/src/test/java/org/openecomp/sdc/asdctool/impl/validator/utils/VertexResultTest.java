package org.openecomp.sdc.asdctool.impl.validator.utils;

import org.junit.Test;


public class VertexResultTest {

	private VertexResult createTestSubject() {
		return new VertexResult();
	}

	@Test
	public void createTestSubjectBoolean() {
		new VertexResult(true);
	}
	
	@Test
	public void testGetStatus() {
		VertexResult testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() {
		VertexResult testSubject;
		boolean status = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetResult() {
		VertexResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResult();
	}
}