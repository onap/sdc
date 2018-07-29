package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;

import java.util.Date;
import java.util.Set;

public class OperationalEnvironmentEntryTest {

	private OperationalEnvironmentEntry createTestSubject() {
		return new OperationalEnvironmentEntry();
	}

	@Test
	public void testGetLastModified() throws Exception {
		OperationalEnvironmentEntry testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastModified();
	}

	@Test
	public void testSetLastModified() throws Exception {
		OperationalEnvironmentEntry testSubject;
		Date lastModified = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setLastModified(lastModified);
	}

	@Test
	public void testGetEnvironmentId() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEnvironmentId();
	}

	@Test
	public void testSetEnvironmentId() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String environmentId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEnvironmentId(environmentId);
	}

	@Test
	public void testGetTenant() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTenant();
	}

	@Test
	public void testSetTenant() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String tenant = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTenant(tenant);
	}

	@Test
	public void testGetIsProduction() throws Exception {
		OperationalEnvironmentEntry testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsProduction();
	}

	@Test
	public void testSetIsProduction() throws Exception {
		OperationalEnvironmentEntry testSubject;
		Boolean production = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIsProduction(production);
	}

	@Test
	public void testGetEcompWorkloadContext() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEcompWorkloadContext();
	}

	@Test
	public void testSetEcompWorkloadContext() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String ecompWorkloadContext = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEcompWorkloadContext(ecompWorkloadContext);
	}

	@Test
	public void testGetStatus() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	@Test
	public void testSetStatus() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	@Test
	public void testSetStatus_1() throws Exception {
		OperationalEnvironmentEntry testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(EnvironmentStatusEnum.COMPLETED);
	}

	@Test
	public void testGetDmaapUebAddress() throws Exception {
		OperationalEnvironmentEntry testSubject;
		Set<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDmaapUebAddress();
	}

	@Test
	public void testSetDmaapUebAddress() throws Exception {
		OperationalEnvironmentEntry testSubject;
		Set<String> dmaapUebAddress = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDmaapUebAddress(dmaapUebAddress);
	}

	@Test
	public void testAddDmaapUebAddress() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String address = "";

		// default test
		testSubject = createTestSubject();
		testSubject.addDmaapUebAddress(address);
	}

	@Test
	public void testGetUebApikey() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUebApikey();
	}

	@Test
	public void testSetUebApikey() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String uebApikey = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUebApikey(uebApikey);
	}

	@Test
	public void testGetUebSecretKey() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUebSecretKey();
	}

	@Test
	public void testSetUebSecretKey() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String uebSecretKey = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUebSecretKey(uebSecretKey);
	}

	@Test
	public void testToString() throws Exception {
		OperationalEnvironmentEntry testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}