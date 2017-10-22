package org.openecomp.sdc.be.config;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.config.ErrorInfo.ErrorInfoType;


public class ErrorInfoTest {

	private ErrorInfo createTestSubject() {
		return new ErrorInfo();
	}

	
	@Test
	public void testGetCode() throws Exception {
		ErrorInfo testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCode();
	}

	
	@Test
	public void testSetCode() throws Exception {
		ErrorInfo testSubject;
		Integer code = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setCode(code);
	}

	
	@Test
	public void testGetMessage() throws Exception {
		ErrorInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMessage();
	}

	
	@Test
	public void testSetMessage() throws Exception {
		ErrorInfo testSubject;
		String message = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setMessage(message);
	}

	
	@Test
	public void testGetMessageId() throws Exception {
		ErrorInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMessageId();
	}

	
	@Test
	public void testSetMessageId() throws Exception {
		ErrorInfo testSubject;
		String messageId = "";

		// test 1
		testSubject = createTestSubject();
		messageId = null;
		testSubject.setMessageId(messageId);

		// test 2
		testSubject = createTestSubject();
		messageId = "";
		testSubject.setMessageId(messageId);
	}

	
	@Test
	public void testGetErrorInfoType() throws Exception {
		ErrorInfo testSubject;
		ErrorInfoType result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getErrorInfoType();
	}

	

	
	@Test
	public void testToString() throws Exception {
		ErrorInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}