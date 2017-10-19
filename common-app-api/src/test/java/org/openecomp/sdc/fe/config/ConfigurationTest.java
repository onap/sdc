package org.openecomp.sdc.fe.config;

import java.util.Date;
import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.config.Configuration.OnboardingConfig;
import org.openecomp.sdc.fe.config.Configuration.FeMonitoringConfig;


public class ConfigurationTest {

	private Configuration createTestSubject() {
		return new Configuration();
	}

	
	@Test
	public void testGetKibanaProtocol() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getKibanaProtocol();
	}

	
	@Test
	public void testSetKibanaProtocol() throws Exception {
		Configuration testSubject;
		String kibanaProtocol = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setKibanaProtocol(kibanaProtocol);
	}

	
	@Test
	public void testGetKibanaHost() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getKibanaHost();
	}

	
	@Test
	public void testSetKibanaHost() throws Exception {
		Configuration testSubject;
		String kibanaHost = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setKibanaHost(kibanaHost);
	}

	
	@Test
	public void testGetKibanaPort() throws Exception {
		Configuration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getKibanaPort();
	}

	
	@Test
	public void testSetKibanaPort() throws Exception {
		Configuration testSubject;
		Integer kibanaPort = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setKibanaPort(kibanaPort);
	}

	
	@Test
	public void testGetSystemMonitoring() throws Exception {
		Configuration testSubject;
		FeMonitoringConfig result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSystemMonitoring();
	}

	
	@Test
	public void testSetSystemMonitoring() throws Exception {
		Configuration testSubject;
		FeMonitoringConfig systemMonitoring = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setSystemMonitoring(systemMonitoring);
	}

	
	@Test
	public void testGetHealthCheckSocketTimeoutInMs() throws Exception {
		Configuration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHealthCheckSocketTimeoutInMs();
	}

	
	@Test
	public void testGetHealthCheckSocketTimeoutInMs_1() throws Exception {
		Configuration testSubject;
		int defaultVal = 0;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHealthCheckSocketTimeoutInMs(defaultVal);
	}

	
	@Test
	public void testSetHealthCheckSocketTimeoutInMs() throws Exception {
		Configuration testSubject;
		Integer healthCheckSocketTimeout = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setHealthCheckSocketTimeoutInMs(healthCheckSocketTimeout);
	}

	
	@Test
	public void testGetHealthCheckIntervalInSeconds() throws Exception {
		Configuration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHealthCheckIntervalInSeconds();
	}

	
	@Test
	public void testGetHealthCheckIntervalInSeconds_1() throws Exception {
		Configuration testSubject;
		int defaultVal = 0;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHealthCheckIntervalInSeconds(defaultVal);
	}

	
	@Test
	public void testSetHealthCheckIntervalInSeconds() throws Exception {
		Configuration testSubject;
		Integer healthCheckInterval = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setHealthCheckIntervalInSeconds(healthCheckInterval);
	}

	
	@Test
	public void testGetReleased() throws Exception {
		Configuration testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getReleased();
	}

	
	@Test
	public void testGetVersion() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	
	@Test
	public void testSetReleased() throws Exception {
		Configuration testSubject;
		Date released = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setReleased(released);
	}

	
	@Test
	public void testSetVersion() throws Exception {
		Configuration testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	
	@Test
	public void testGetConnection() throws Exception {
		Configuration testSubject;
		Connection result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConnection();
	}

	
	@Test
	public void testSetConnection() throws Exception {
		Configuration testSubject;
		Connection connection = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setConnection(connection);
	}

	
	@Test
	public void testGetProtocols() throws Exception {
		Configuration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProtocols();
	}

	
	@Test
	public void testSetProtocols() throws Exception {
		Configuration testSubject;
		List<String> protocols = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProtocols(protocols);
	}

	
	@Test
	public void testGetBeHost() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBeHost();
	}

	
	@Test
	public void testSetBeHost() throws Exception {
		Configuration testSubject;
		String beHost = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setBeHost(beHost);
	}

	
	@Test
	public void testGetBeHttpPort() throws Exception {
		Configuration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBeHttpPort();
	}

	
	@Test
	public void testSetBeHttpPort() throws Exception {
		Configuration testSubject;
		Integer beHttpPort = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setBeHttpPort(beHttpPort);
	}

	
	@Test
	public void testGetBeSslPort() throws Exception {
		Configuration testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBeSslPort();
	}

	
	@Test
	public void testSetBeSslPort() throws Exception {
		Configuration testSubject;
		Integer beSslPort = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setBeSslPort(beSslPort);
	}

	
	@Test
	public void testGetBeContext() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBeContext();
	}

	
	@Test
	public void testSetBeContext() throws Exception {
		Configuration testSubject;
		String beContext = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setBeContext(beContext);
	}

	
	@Test
	public void testGetBeProtocol() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getBeProtocol();
	}

	
	@Test
	public void testSetBeProtocol() throws Exception {
		Configuration testSubject;
		String beProtocol = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setBeProtocol(beProtocol);
	}

	
	@Test
	public void testGetThreadpoolSize() throws Exception {
		Configuration testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getThreadpoolSize();
	}

	
	@Test
	public void testSetThreadpoolSize() throws Exception {
		Configuration testSubject;
		int threadpoolSize = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setThreadpoolSize(threadpoolSize);
	}

	
	@Test
	public void testGetRequestTimeout() throws Exception {
		Configuration testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestTimeout();
	}

	
	@Test
	public void testSetRequestTimeout() throws Exception {
		Configuration testSubject;
		int requestTimeout = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestTimeout(requestTimeout);
	}

	
	@Test
	public void testGetIdentificationHeaderFields() throws Exception {
		Configuration testSubject;
		List<List<String>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIdentificationHeaderFields();
	}

	
	@Test
	public void testSetIdentificationHeaderFields() throws Exception {
		Configuration testSubject;
		List<List<String>> identificationHeaderFields = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIdentificationHeaderFields(identificationHeaderFields);
	}

	
	@Test
	public void testGetOptionalHeaderFields() throws Exception {
		Configuration testSubject;
		List<List<String>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOptionalHeaderFields();
	}

	
	@Test
	public void testSetOptionalHeaderFields() throws Exception {
		Configuration testSubject;
		List<List<String>> optionalHeaderFields = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setOptionalHeaderFields(optionalHeaderFields);
	}

	
	@Test
	public void testGetForwardHeaderFields() throws Exception {
		Configuration testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getForwardHeaderFields();
	}

	
	@Test
	public void testSetForwardHeaderFields() throws Exception {
		Configuration testSubject;
		List<String> forwardHeaderFields = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setForwardHeaderFields(forwardHeaderFields);
	}

	
	@Test
	public void testGetFeFqdn() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFeFqdn();
	}

	
	@Test
	public void testSetFeFqdn() throws Exception {
		Configuration testSubject;
		String feFqdn = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFeFqdn(feFqdn);
	}

	
	@Test
	public void testGetOnboardingForwardContext() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOnboardingForwardContext();
	}

	
	@Test
	public void testSetOnboardingForwardContext() throws Exception {
		Configuration testSubject;
		String onboardingForwardContext = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOnboardingForwardContext(onboardingForwardContext);
	}

	


	


	
	@Test
	public void testToString() throws Exception {
		Configuration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}