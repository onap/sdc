package org.openecomp.sdc.common.ecomplog;

import org.junit.Test;
import org.openecomp.sdc.common.ecomplog.Enums.Severity;
import org.openecomp.sdc.common.ecomplog.api.IEcompMdcWrapper;

public class EcompLoggerMetricTest {

	private EcompLoggerMetric createTestSubject() {
		return new EcompLoggerMetric(new EcompMDCWrapper(new Stopwatch()));
	}

	
	@Test
	public void testGetInstance() throws Exception {
		EcompLoggerMetric result;

		// default test
		result = EcompLoggerMetric.getInstance();
	}

	
	@Test
	public void testStartTimer() throws Exception {
		EcompLoggerMetric testSubject;
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.startTimer();
	}

	
	@Test
	public void testSetKeyRequestId() throws Exception {
		EcompLoggerMetric testSubject;
		String keyRequestId = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setKeyRequestId(keyRequestId);
	}

	
	@Test
	public void testStopTimer() throws Exception {
		EcompLoggerMetric testSubject;
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.stopTimer();
	}

	
	@Test
	public void testSetAutoServerFQDN() throws Exception {
		EcompLoggerMetric testSubject;
		String serverFQDN = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setAutoServerFQDN(serverFQDN);
	}

	
	@Test
	public void testSetAutoServerIPAddress() throws Exception {
		EcompLoggerMetric testSubject;
		String serverIPAddress = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setAutoServerIPAddress(serverIPAddress);
	}

	
	@Test
	public void testSetInstanceUUID() throws Exception {
		EcompLoggerMetric testSubject;
		String instanceUUID = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setInstanceUUID(instanceUUID);
	}

	
	@Test
	public void testSetOptProcessKey() throws Exception {
		EcompLoggerMetric testSubject;
		String processKey = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptProcessKey(processKey);
	}

	
	@Test
	public void testSetOptAlertSeverity() throws Exception {
		EcompLoggerMetric testSubject;
		Severity alertSeverity = null;
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptAlertSeverity(alertSeverity.OK);
	}

	
	@Test
	public void testSetOptCustomField1() throws Exception {
		EcompLoggerMetric testSubject;
		String customField1 = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptCustomField1(customField1);
	}

	
	@Test
	public void testSetOptCustomField2() throws Exception {
		EcompLoggerMetric testSubject;
		String customField2 = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptCustomField2(customField2);
	}

	
	@Test
	public void testSetOptCustomField3() throws Exception {
		EcompLoggerMetric testSubject;
		String customField3 = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptCustomField3(customField3);
	}

	
	@Test
	public void testSetOptCustomField4() throws Exception {
		EcompLoggerMetric testSubject;
		String customField4 = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptCustomField4(customField4);
	}

	
	@Test
	public void testSetRemoteHost() throws Exception {
		EcompLoggerMetric testSubject;
		String remoteHost = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setRemoteHost(remoteHost);
	}

	
	@Test
	public void testSetServiceName() throws Exception {
		EcompLoggerMetric testSubject;
		String serviceName = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setServiceName(serviceName);
	}

	
	@Test
	public void testSetStatusCode() throws Exception {
		EcompLoggerMetric testSubject;
		String statusCode = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setStatusCode(statusCode);
	}

	
	@Test
	public void testSetPartnerName() throws Exception {
		EcompLoggerMetric testSubject;
		String partnerName = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setPartnerName(partnerName);
	}

	
	@Test
	public void testSetResponseCode() throws Exception {
		EcompLoggerMetric testSubject;
		int responseCode = 0;
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setResponseCode(responseCode);
	}

	
	@Test
	public void testSetResponseDesc() throws Exception {
		EcompLoggerMetric testSubject;
		String responseDesc = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setResponseDesc(responseDesc);
	}

	
	@Test
	public void testSetOptServiceInstanceId() throws Exception {
		EcompLoggerMetric testSubject;
		String serviceInstanceId = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptServiceInstanceId(serviceInstanceId);
	}

	
	@Test
	public void testSetOptClassName() throws Exception {
		EcompLoggerMetric testSubject;
		String className = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptClassName(className);
	}

	
	@Test
	public void testSetTargetEntity() throws Exception {
		EcompLoggerMetric testSubject;
		String targetEntity = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setTargetEntity(targetEntity);
	}

	
	@Test
	public void testSetTargetServiceName() throws Exception {
		EcompLoggerMetric testSubject;
		String targetServiceName = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setTargetServiceName(targetServiceName);
	}

	
	@Test
	public void testSetTargetVirtualEntity() throws Exception {
		EcompLoggerMetric testSubject;
		String targetVirtualEntity = "";
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setTargetVirtualEntity(targetVirtualEntity);
	}

	
	@Test
	public void testClear() throws Exception {
		EcompLoggerMetric testSubject;
		EcompLoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.clear();
	}

	
	@Test
	public void testInitializeMandatoryFields() throws Exception {
		EcompLoggerMetric testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.initializeMandatoryFields();
	}
}