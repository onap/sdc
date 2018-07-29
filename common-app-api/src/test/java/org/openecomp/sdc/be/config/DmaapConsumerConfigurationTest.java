package org.openecomp.sdc.be.config;

import org.junit.Test;
import org.openecomp.sdc.be.config.DmaapConsumerConfiguration.Credential;

public class DmaapConsumerConfigurationTest {

	private DmaapConsumerConfiguration createTestSubject() {
		return new DmaapConsumerConfiguration();
	}

	
	@Test
	public void testGetHosts() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHosts();
	}

	
	@Test
	public void testSetHosts() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String hosts = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setHosts(hosts);
	}

	
	@Test
	public void testGetConsumerGroup() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerGroup();
	}

	
	@Test
	public void testSetConsumerGroup() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String consumerGroup = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerGroup(consumerGroup);
	}

	
	@Test
	public void testGetConsumerId() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerId();
	}

	
	@Test
	public void testSetConsumerId() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String consumerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerId(consumerId);
	}

	
	@Test
	public void testGetTimeoutMs() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimeoutMs();
	}

	
	@Test
	public void testSetTimeoutMs() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer timeoutMs = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimeoutMs(timeoutMs);
	}

	
	@Test
	public void testGetLimit() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLimit();
	}

	
	@Test
	public void testSetLimit() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer limit = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setLimit(limit);
	}

	
	@Test
	public void testGetPollingInterval() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPollingInterval();
	}

	
	@Test
	public void testSetPollingInterval() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer pollingInterval = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setPollingInterval(pollingInterval);
	}

	
	@Test
	public void testGetTopic() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTopic();
	}

	
	@Test
	public void testSetTopic() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String topic = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTopic(topic);
	}

	
	@Test
	public void testGetLatitude() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Double result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLatitude();
	}

	
	@Test
	public void testSetLatitude() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Double latitude = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setLatitude(latitude);
	}

	
	@Test
	public void testGetLongitude() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Double result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLongitude();
	}

	
	@Test
	public void testSetLongitude() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Double longitude = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setLongitude(longitude);
	}

	
	@Test
	public void testGetVersion() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	
	@Test
	public void testSetVersion() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	
	@Test
	public void testGetServiceName() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceName();
	}

	
	@Test
	public void testSetServiceName() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String serviceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceName(serviceName);
	}

	
	@Test
	public void testGetEnvironment() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEnvironment();
	}

	
	@Test
	public void testSetEnvironment() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String environment = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEnvironment(environment);
	}

	
	@Test
	public void testGetPartner() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPartner();
	}

	
	@Test
	public void testSetPartner() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String partner = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPartner(partner);
	}

	
	@Test
	public void testGetRouteOffer() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRouteOffer();
	}

	
	@Test
	public void testSetRouteOffer() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String routeOffer = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRouteOffer(routeOffer);
	}

	
	@Test
	public void testGetProtocol() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProtocol();
	}

	
	@Test
	public void testSetProtocol() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String protocol = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setProtocol(protocol);
	}

	
	@Test
	public void testGetContenttype() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getContenttype();
	}

	
	@Test
	public void testSetContenttype() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String contenttype = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setContenttype(contenttype);
	}

	
	@Test
	public void testIsDme2TraceOn() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isDme2TraceOn();
	}

	
	@Test
	public void testGetDme2TraceOn() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDme2TraceOn();
	}

	
	@Test
	public void testSetDme2TraceOn() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Boolean dme2TraceOn = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDme2TraceOn(dme2TraceOn);
	}

	
	@Test
	public void testGetAftEnvironment() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAftEnvironment();
	}

	
	@Test
	public void testSetAftEnvironment() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String aftEnvironment = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAftEnvironment(aftEnvironment);
	}

	
	@Test
	public void testGetAftDme2ConnectionTimeoutMs() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAftDme2ConnectionTimeoutMs();
	}

	
	@Test
	public void testSetAftDme2ConnectionTimeoutMs() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer aftDme2ConnectionTimeoutMs = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setAftDme2ConnectionTimeoutMs(aftDme2ConnectionTimeoutMs);
	}

	
	@Test
	public void testGetAftDme2RoundtripTimeoutMs() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAftDme2RoundtripTimeoutMs();
	}

	
	@Test
	public void testSetAftDme2RoundtripTimeoutMs() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer aftDme2RoundtripTimeoutMs = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setAftDme2RoundtripTimeoutMs(aftDme2RoundtripTimeoutMs);
	}

	
	@Test
	public void testGetAftDme2ReadTimeoutMs() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAftDme2ReadTimeoutMs();
	}

	
	@Test
	public void testSetAftDme2ReadTimeoutMs() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer aftDme2ReadTimeoutMs = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setAftDme2ReadTimeoutMs(aftDme2ReadTimeoutMs);
	}

	
	@Test
	public void testGetDme2preferredRouterFilePath() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDme2preferredRouterFilePath();
	}

	
	@Test
	public void testSetDme2preferredRouterFilePath() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String dme2preferredRouterFilePath = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDme2preferredRouterFilePath(dme2preferredRouterFilePath);
	}

	
	@Test
	public void testGetCredential() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Credential result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCredential();
	}

	
	@Test
	public void testSetCredential() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Credential credential = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCredential(credential);
	}

	
	@Test
	public void testToString() throws Exception {
		DmaapConsumerConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetTimeLimitForNotificationHandleMs() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimeLimitForNotificationHandleMs();
	}

	
	@Test
	public void testSetTimeLimitForNotificationHandleMs() throws Exception {
		DmaapConsumerConfiguration testSubject;
		Integer timeLimitForNotificationHandleMs = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimeLimitForNotificationHandleMs(timeLimitForNotificationHandleMs);
	}
}