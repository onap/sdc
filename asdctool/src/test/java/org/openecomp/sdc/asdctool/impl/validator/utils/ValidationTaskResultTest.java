package org.openecomp.sdc.asdctool.impl.validator.utils;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;


public class ValidationTaskResultTest {

	private ValidationTaskResult createTestSubject() {
		return new ValidationTaskResult(new GraphVertex(), "", "", false);
	}

	
	@Test
	public void testGetName() throws Exception {
		ValidationTaskResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		ValidationTaskResult testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetResultMessage() throws Exception {
		ValidationTaskResult testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResultMessage();
	}

	
	@Test
	public void testSetResultMessage() throws Exception {
		ValidationTaskResult testSubject;
		String resultMessage = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResultMessage(resultMessage);
	}

	
	@Test
	public void testIsSuccessful() throws Exception {
		ValidationTaskResult testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isSuccessful();
	}

	
	@Test
	public void testSetSuccessful() throws Exception {
		ValidationTaskResult testSubject;
		boolean successful = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setSuccessful(successful);
	}
}