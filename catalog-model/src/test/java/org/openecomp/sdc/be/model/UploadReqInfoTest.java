package org.openecomp.sdc.be.model;

import org.junit.Test;


public class UploadReqInfoTest {

	private UploadReqInfo createTestSubject() {
		return new UploadReqInfo();
	}

	
	@Test
	public void testGetCapabilityName() throws Exception {
		UploadReqInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilityName();
	}

	
	@Test
	public void testSetCapabilityName() throws Exception {
		UploadReqInfo testSubject;
		String capabilityName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilityName(capabilityName);
	}

	
	@Test
	public void testGetNode() throws Exception {
		UploadReqInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode();
	}

	
	@Test
	public void testSetNode() throws Exception {
		UploadReqInfo testSubject;
		String node = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNode(node);
	}
}