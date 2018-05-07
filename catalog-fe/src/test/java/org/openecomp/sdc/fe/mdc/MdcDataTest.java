package org.openecomp.sdc.fe.mdc;

import org.junit.Test;

public class MdcDataTest {

	private MdcData createTestSubject() {
		return new MdcData("", "", "", "", null);
	}

	@Test
	public void testGetTransactionStartTime() throws Exception {
		MdcData testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTransactionStartTime();
	}

	@Test
	public void testGetUserId() throws Exception {
		MdcData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserId();
	}

	@Test
	public void testGetRemoteAddr() throws Exception {
		MdcData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRemoteAddr();
	}

	@Test
	public void testGetLocalAddr() throws Exception {
		MdcData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLocalAddr();
	}

	@Test
	public void testGetServiceInstanceID() throws Exception {
		MdcData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceID();
	}
}