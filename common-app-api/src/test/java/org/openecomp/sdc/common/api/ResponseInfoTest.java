package org.openecomp.sdc.common.api;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.common.api.ResponseInfo.ResponseStatusEnum;


public class ResponseInfoTest {

	private ResponseInfo createTestSubject() {
		return new ResponseInfo(null, "");
	}

	
	@Test
	public void testGetApplicativeStatus() throws Exception {
		ResponseInfo testSubject;
		ResponseStatusEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getApplicativeStatus();
	}

	
	@Test
	public void testSetApplicativeStatus() throws Exception {
		ResponseInfo testSubject;
		ResponseStatusEnum applicativeStatus = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setApplicativeStatus(applicativeStatus);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		ResponseInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		ResponseInfo testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	

}