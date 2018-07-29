package org.openecomp.sdc.be.components.distribution.engine;

import com.att.nsa.mr.client.MRConsumer;
import mockit.Deencapsulation;
import org.junit.Test;
import org.openecomp.sdc.be.config.DmaapConsumerConfiguration;
import org.openecomp.sdc.be.config.DmaapConsumerConfiguration.Credential;

import java.io.File;
import java.security.GeneralSecurityException;
import java.util.Properties;

public class DmaapClientFactoryTest {

	private DmaapClientFactory createTestSubject() {
		return new DmaapClientFactory();
	}

	@Test
	public void testCreate() throws Exception {
		DmaapClientFactory testSubject;
		DmaapConsumerConfiguration parameters = new DmaapConsumerConfiguration();
		MRConsumer result;
		String filePath = "src/test/resources/config/mock.txt";

		Credential credential = new Credential();
		credential.setPassword("hmXYcznAljMSisdy8zgcag==");
		credential.setUsername("mock");
		parameters.setCredential(credential);
		parameters.setLatitude(new Double(32452));
		parameters.setLongitude(new Double(543534));
		parameters.setVersion("mock");
		parameters.setServiceName("mock");
		parameters.setServiceName("mock");
		parameters.setEnvironment("mock");
		parameters.setPartner("mock");
		parameters.setRouteOffer("mock");
		parameters.setProtocol("mock");
		parameters.setContenttype("mock");
		parameters.setHosts("mock");
		parameters.setTopic("mock");
		parameters.setConsumerGroup("mock");
		parameters.setConsumerId("mock");
		parameters.setTimeoutMs(42354);
		parameters.setLimit(43534);
		parameters.setDme2TraceOn(true);
		parameters.setAftEnvironment("mock");
		parameters.setAftDme2ConnectionTimeoutMs(234324);
		parameters.setAftDme2RoundtripTimeoutMs(435345);
		parameters.setAftDme2ReadTimeoutMs(5645);
		parameters.setDme2preferredRouterFilePath(filePath);

		// default test
		testSubject = createTestSubject();
		result = testSubject.create(parameters);
		File file = new File(filePath);
		file.delete();
		
	}

	@Test
	public void testBuildProperties() throws Exception {
		DmaapClientFactory testSubject;
		DmaapConsumerConfiguration parameters = new DmaapConsumerConfiguration();
		Properties result;
		String filePath = "src/test/resources/config/mock.txt";

		Credential credential = new Credential();
		credential.setPassword("hmXYcznAljMSisdy8zgcag==");
		credential.setUsername("mock");
		parameters.setCredential(credential);
		parameters.setLatitude(new Double(32452));
		parameters.setLongitude(new Double(543534));
		parameters.setVersion("mock");
		parameters.setServiceName("mock");
		parameters.setServiceName("mock");
		parameters.setEnvironment("mock");
		parameters.setPartner("mock");
		parameters.setRouteOffer("mock");
		parameters.setProtocol("mock");
		parameters.setContenttype("mock");
		parameters.setHosts("mock");
		parameters.setTopic("mock");
		parameters.setConsumerGroup("mock");
		parameters.setConsumerId("mock");
		parameters.setTimeoutMs(42354);
		parameters.setLimit(43534);
		parameters.setDme2TraceOn(true);
		parameters.setAftEnvironment("mock");
		parameters.setAftDme2ConnectionTimeoutMs(234324);
		parameters.setAftDme2RoundtripTimeoutMs(435345);
		parameters.setAftDme2ReadTimeoutMs(5645);
		parameters.setDme2preferredRouterFilePath(filePath);

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "buildProperties", parameters);

		File file = new File(filePath);
		file.delete();
	}

	@Test(expected = GeneralSecurityException.class)
	public void testBuildProperties_2() throws Exception {
		DmaapClientFactory testSubject;
		DmaapConsumerConfiguration parameters = new DmaapConsumerConfiguration();
		Properties result;
		String filePath = "src/test/resources/config/mock.txt";

		Credential credential = new Credential();
		credential.setPassword("mock");
		parameters.setCredential(credential);

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "buildProperties", parameters);
	}

	@Test
	public void testEnsureFileExists() throws Exception {
		DmaapClientFactory testSubject;
		String filePath = "src/test/resources/config/mock.txt";

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "ensureFileExists", new Object[] { filePath });
		Deencapsulation.invoke(testSubject, "ensureFileExists", filePath);
		File file = new File(filePath);
		file.delete();
	}
}