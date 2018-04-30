package org.openecomp.sdc.asdctool.impl.validator.utils;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;


public class ValidationTaskResultTest {

	private ValidationTaskResult createTestSubject() {
		return new ValidationTaskResult(new GraphVertex(), "", "", false);
	}

	
	@Test
	public void testGetName() {
		ValidationTaskResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() {
		ValidationTaskResult testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetResultMessage() {
		ValidationTaskResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResultMessage();
	}

	
	@Test
	public void testSetResultMessage() {
		ValidationTaskResult testSubject;
		String resultMessage = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResultMessage(resultMessage);
	}

	
	@Test
	public void testIsSuccessful() {
		ValidationTaskResult testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isSuccessful();
	}

	
	@Test
	public void testSetSuccessful() {
		ValidationTaskResult testSubject;
		boolean successful = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setSuccessful(successful);
	}
}