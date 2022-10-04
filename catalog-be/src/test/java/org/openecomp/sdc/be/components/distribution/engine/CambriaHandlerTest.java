/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.distribution.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.att.nsa.apiClient.credentials.ApiCredential;
import com.att.nsa.apiClient.http.HttpException;
import com.att.nsa.cambria.client.CambriaClient;
import com.att.nsa.cambria.client.CambriaClient.CambriaApiException;
import com.att.nsa.cambria.client.CambriaClientBuilders.AbstractAuthenticatedManagerBuilder;
import com.att.nsa.cambria.client.CambriaClientBuilders.TopicManagerBuilder;
import com.att.nsa.cambria.client.CambriaConsumer;
import com.att.nsa.cambria.client.CambriaIdentityManager;
import fj.data.Either;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

@RunWith(MockitoJUnitRunner.class)
public class CambriaHandlerTest extends BeConfDependentTest {

	private CambriaHandler createTestSubject() {
		return new CambriaHandler();
	}

	@Spy
	private CambriaHandler handler = new CambriaHandler();

	@Mock
	private CambriaIdentityManager createIdentityManager;

	private ApiCredential apiCredential = new ApiCredential("apiKey", "apiSecret");

	@BeforeClass
	public static void beforeClass() {
		String appConfigDir = "src/test/resources/config/catalog-be";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				appConfigDir);
		new ConfigurationManager(configurationSource);
	}

	@Before
	public void startUp() throws MalformedURLException, GeneralSecurityException {
		doReturn(createIdentityManager).when(handler).buildCambriaClient(any());
	}

	@Test
	public void testMockCreateUebKeys() throws HttpException, CambriaApiException, IOException {
		Mockito.when(createIdentityManager.createApiKey(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(apiCredential);
		Either<ApiCredential, CambriaErrorResponse> eitherCreateUebKeys = handler
				.createUebKeys(Arrays.asList("Myhost:1234"));

		Mockito.verify(createIdentityManager).setApiCredentials(Mockito.anyString(), Mockito.anyString());

		assertTrue("Unexpected Operational Status", eitherCreateUebKeys.isLeft());

	}

	@Test
	public void testMockCreateUebKeys_FAIL() throws HttpException, CambriaApiException, IOException {
		Mockito.when(createIdentityManager.createApiKey(Mockito.anyString(), Mockito.anyString()))
				.thenThrow(new CambriaApiException("Error Message"));
		Either<ApiCredential, CambriaErrorResponse> eitherCreateUebKeys = handler
				.createUebKeys(Arrays.asList("Myhost:1234"));
		Mockito.verify(createIdentityManager, Mockito.never()).setApiCredentials(Mockito.anyString(),
				Mockito.anyString());
		assertTrue("Unexpected Operational Status", eitherCreateUebKeys.isRight());
		CambriaErrorResponse response = eitherCreateUebKeys.right().value();
		assertEquals("Unexpected Operational Status", CambriaOperationStatus.CONNNECTION_ERROR,
				response.getOperationStatus());
		assertEquals("Unexpected HTTP Code", 500, response.getHttpCode().intValue());
	}

	@Test
	public void testProcessMessageException() throws Exception {
		CambriaHandler testSubject;
		String message = "";
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "processMessageException", new Object[] { message });
	}

	@Test
	public void testCheckPattern() throws Exception {
		CambriaHandler testSubject;
		String patternStr = "";
		String message = "";
		int groupIndex = 0;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "checkPattern", new Object[] { patternStr, message, groupIndex });
	}

//	@Test
//	public void testGetTopics() throws Exception {
//
//		CambriaHandler testSubject;
//		System.setProperty("JAASCONFIG", "thatsTheOne");
//		List<String> hostSet = new LinkedList<>();
//		hostSet.add("localhost:9092");
//		Either<Set<String>, CambriaErrorResponse> result;
//
//		// default test
//		testSubject = createTestSubject();
//		result = testSubject.getTopics(hostSet);
//	}

	@Test
	public void testProcessError() throws Exception {
		CambriaHandler testSubject;
		Exception e = null;
		CambriaErrorResponse result;

		// default test
		testSubject = createTestSubject();

		e = new Exception("HTTP Status 999");
		result = Deencapsulation.invoke(testSubject, "processError", e);

		e = new Exception("HTTP Status 401");
		result = Deencapsulation.invoke(testSubject, "processError", e);

		e = new Exception("HTTP Status 409");
		result = Deencapsulation.invoke(testSubject, "processError", e);

		e = new Exception("HTTP Status 500");
		result = Deencapsulation.invoke(testSubject, "processError", e);

		e = new Exception("mock", new Throwable(new Throwable("mock")));
		result = Deencapsulation.invoke(testSubject, "processError", e);
	}

	@Test
	public void testWriteErrorToLog() throws Exception {
		CambriaHandler testSubject;
		CambriaErrorResponse cambriaErrorResponse = new CambriaErrorResponse();
		cambriaErrorResponse.setOperationStatus(CambriaOperationStatus.AUTHENTICATION_ERROR);
		String errorMessage = "mock";
		String methodName = "mock";
		String operationDesc = "mock";

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "writeErrorToLog", cambriaErrorResponse, "mock", "mock");
	}

	@Test
	public void testCreateTopic() throws Exception {
		CambriaHandler testSubject;
		Collection<String> hostSet = new LinkedList<>();
		hostSet.add("mock");
		String apiKey = "mock";
		String secretKey = "mock";
		String topicName = "mock";
		int partitionCount = 0;
		int replicationCount = 0;
		CambriaErrorResponse result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createTopic(hostSet, apiKey, secretKey, topicName, partitionCount, replicationCount);
	}

	@Test
	public void testUnRegisterFromTopic() throws Exception {
		CambriaHandler testSubject;
		Collection<String> hostSet = new LinkedList<>();
		hostSet.add("mock");
		String managerApiKey = "mock";
		String managerSecretKey = "mock";
		String subscriberApiKey = "mock";
		String topicName = "mock";
		CambriaErrorResponse unRegisterFromTopic = null;

		// default test
		testSubject = createTestSubject();
		unRegisterFromTopic = testSubject.unRegisterFromTopic(hostSet, managerApiKey, managerSecretKey,
				subscriberApiKey, SubscriberTypeEnum.CONSUMER, topicName);
	}

	@Test
	public void testRegisterToTopic() throws Exception {
		CambriaHandler testSubject;
		Collection<String> hostSet = new LinkedList<String>();
		hostSet.add("mock");
		String managerApiKey = "mock";
		String managerSecretKey = "mock";
		String subscriberApiKey = "mock";
		SubscriberTypeEnum subscriberTypeEnum = null;
		String topicName = "mock";
		CambriaErrorResponse result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.registerToTopic(hostSet, managerApiKey, managerSecretKey, subscriberApiKey,
				SubscriberTypeEnum.CONSUMER, topicName);
	}

	@Test
	public void testCreateConsumer() throws Exception {
		CambriaHandler testSubject;
		Collection<String> hostSet = new LinkedList<>();
		hostSet.add("mock");
		String topicName = "mock";
		String apiKey = "mock";
		String secretKey = "mock";
		String consumerId = "mock";
		String consumerGroup = "mock";
		int timeoutMS = 0;
		CambriaConsumer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createConsumer(hostSet, topicName, apiKey, secretKey, consumerId, consumerGroup,
				timeoutMS);
	}

	@Test
	public void testCloseConsumer() throws Exception {
		CambriaHandler testSubject;
		CambriaConsumer consumer = null;

		// test 1
		testSubject = createTestSubject();
		consumer = null;
		testSubject.closeConsumer(consumer);
	}

	@Test
	public void testFetchFromTopic() throws Exception {
		CambriaHandler testSubject;
		CambriaConsumer topicConsumer = null;
		Either<Iterable<String>, CambriaErrorResponse> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.fetchFromTopic(topicConsumer);
	}

	@Test
	public void testSendNotification() throws Exception {
		CambriaHandler testSubject;
		String topicName = "mock";
		String uebPublicKey = "mock";
		String uebSecretKey = "mock";
		List<String> uebServers = new LinkedList<>();
		uebServers.add("mock");
		INotificationData data = null;
		CambriaErrorResponse result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.sendNotification(topicName, uebPublicKey, uebSecretKey, uebServers, data);
	}

	@Test
	public void testSendNotificationAndClose() throws Exception {
		CambriaHandler testSubject;
		String topicName = "mock";
		String uebPublicKey = "mock";
		String uebSecretKey = "mock";
		List<String> uebServers = new LinkedList<>();
		uebServers.add("mock");
		INotificationData data = null;
		long waitBeforeCloseTimeout = 1;
		CambriaErrorResponse result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.sendNotificationAndClose(topicName, uebPublicKey, uebSecretKey, uebServers, data,
				waitBeforeCloseTimeout);
	}

	@Test
	public void testGetApiKey() throws Exception {
		CambriaHandler testSubject;
		String server = "";
		String apiKey = "";
		CambriaErrorResponse result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getApiKey(server, apiKey);
	}

	@Test
	public void testCreateUebKeys() throws Exception {
		CambriaHandler testSubject;
		List<String> hostSet = null;
		Either<ApiCredential, CambriaErrorResponse> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.createUebKeys(hostSet);
	}

	@Test
	public void testBuildCambriaClient() throws Exception {
		CambriaHandler testSubject;
		AbstractAuthenticatedManagerBuilder<? extends CambriaClient> client = new TopicManagerBuilder()
				.usingHosts("mock").authenticatedBy("mock", "mock");

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "buildCambriaClient", client);
	}

}
