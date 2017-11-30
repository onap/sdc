package org.openecomp.sdc.be.info;

import org.junit.Test;
import org.openecomp.sdc.common.datastructure.ESTimeBasedEvent;


public class DistributionStatusInfoTest {

	private DistributionStatusInfo createTestSubject() {
		return new DistributionStatusInfo(new ESTimeBasedEvent());
	}

	
	@Test
	public void testGetOmfComponentID() throws Exception {
		DistributionStatusInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOmfComponentID();
	}

	
	@Test
	public void testSetOmfComponentID() throws Exception {
		DistributionStatusInfo testSubject;
		String omfComponentID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOmfComponentID(omfComponentID);
	}

	
	@Test
	public void testGetTimestamp() throws Exception {
		DistributionStatusInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp();
	}

	
	@Test
	public void testSetTimestamp() throws Exception {
		DistributionStatusInfo testSubject;
		String timestamp = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp(timestamp);
	}

	
	@Test
	public void testGetUrl() throws Exception {
		DistributionStatusInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUrl();
	}

	
	@Test
	public void testSetUrl() throws Exception {
		DistributionStatusInfo testSubject;
		String url = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUrl(url);
	}

	
	@Test
	public void testGetStatus() throws Exception {
		DistributionStatusInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		DistributionStatusInfo testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetErrorReason() throws Exception {
		DistributionStatusInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getErrorReason();
	}

	
	@Test
	public void testSetErrorReason() throws Exception {
		DistributionStatusInfo testSubject;
		String errorReason = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setErrorReason(errorReason);
	}
}