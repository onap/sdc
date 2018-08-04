package org.openecomp.sdc.common.ecomplog;

import org.junit.Test;

import org.openecomp.sdc.common.log.elements.LoggerFactory;
import org.openecomp.sdc.common.log.elements.LoggerMetric;
import org.openecomp.sdc.common.log.enums.Severity;
import org.openecomp.sdc.common.log.wrappers.Logger;


public class LoggerMetricTest {
	private static final Logger log = Logger.getLogger(LoggerMetricTest.class.getName());

	private LoggerMetric createTestSubject() {
		return LoggerFactory.getLogger(LoggerMetric.class,log);
	}

	
	@Test
	public void testStartTimer() throws Exception {
		LoggerMetric testSubject;
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.startTimer();
	}

	
	@Test
	public void testSetKeyRequestId() throws Exception {
		LoggerMetric testSubject;
		String keyRequestId = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setKeyRequestId(keyRequestId);
	}

	
	@Test
	public void testStopTimer() throws Exception {
		LoggerMetric testSubject;
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.stopTimer();
	}

	
	@Test
	public void testSetAutoServerFQDN() throws Exception {
		LoggerMetric testSubject;
		String serverFQDN = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setAutoServerFQDN(serverFQDN);
	}

	
	@Test
	public void testSetAutoServerIPAddress() throws Exception {
		LoggerMetric testSubject;
		String serverIPAddress = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setAutoServerIPAddress(serverIPAddress);
	}

	
	@Test
	public void testSetInstanceUUID() throws Exception {
		LoggerMetric testSubject;
		String instanceUUID = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setInstanceUUID(instanceUUID);
	}

	
	@Test
	public void testSetOptProcessKey() throws Exception {
		LoggerMetric testSubject;
		String processKey = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptProcessKey(processKey);
	}

	
	@Test
	public void testSetOptAlertSeverity() throws Exception {
		LoggerMetric testSubject;
		Severity alertSeverity = null;
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptAlertSeverity(alertSeverity.OK);
	}

	
	@Test
	public void testSetOptCustomField1() throws Exception {
		LoggerMetric testSubject;
		String customField1 = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptCustomField1(customField1);
	}

	
	@Test
	public void testSetOptCustomField2() throws Exception {
		LoggerMetric testSubject;
		String customField2 = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptCustomField2(customField2);
	}

	
	@Test
	public void testSetOptCustomField3() throws Exception {
		LoggerMetric testSubject;
		String customField3 = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptCustomField3(customField3);
	}

	
	@Test
	public void testSetOptCustomField4() throws Exception {
		LoggerMetric testSubject;
		String customField4 = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptCustomField4(customField4);
	}

	
	@Test
	public void testSetRemoteHost() throws Exception {
		LoggerMetric testSubject;
		String remoteHost = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setRemoteHost(remoteHost);
	}

	
	@Test
	public void testSetServiceName() throws Exception {
		LoggerMetric testSubject;
		String serviceName = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setServiceName(serviceName);
	}

	
	@Test
	public void testSetStatusCode() throws Exception {
		LoggerMetric testSubject;
		String statusCode = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setStatusCode(statusCode);
	}

	
	@Test
	public void testSetPartnerName() throws Exception {
		LoggerMetric testSubject;
		String partnerName = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setPartnerName(partnerName);
	}

	
	@Test
	public void testSetResponseCode() throws Exception {
		LoggerMetric testSubject;
		int responseCode = 0;
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setResponseCode(responseCode);
	}

	
	@Test
	public void testSetResponseDesc() throws Exception {
		LoggerMetric testSubject;
		String responseDesc = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setResponseDesc(responseDesc);
	}

	
	@Test
	public void testSetOptServiceInstanceId() throws Exception {
		LoggerMetric testSubject;
		String serviceInstanceId = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptServiceInstanceId(serviceInstanceId);
	}

	
	@Test
	public void testSetOptClassName() throws Exception {
		LoggerMetric testSubject;
		String className = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setOptClassName(className);
	}

	
	@Test
	public void testSetTargetEntity() throws Exception {
		LoggerMetric testSubject;
		String targetEntity = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setTargetEntity(targetEntity);
	}

	
	@Test
	public void testSetTargetServiceName() throws Exception {
		LoggerMetric testSubject;
		String targetServiceName = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setTargetServiceName(targetServiceName);
	}

	
	@Test
	public void testSetTargetVirtualEntity() throws Exception {
		LoggerMetric testSubject;
		String targetVirtualEntity = "";
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.setTargetVirtualEntity(targetVirtualEntity);
	}

	
	@Test
	public void testClear() throws Exception {
		LoggerMetric testSubject;
		LoggerMetric result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.clear();
	}
}