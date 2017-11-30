/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.BaseConfDependentTest;
import org.openecomp.sdc.be.components.distribution.engine.CambriaErrorResponse;
import org.openecomp.sdc.be.components.distribution.engine.CambriaHandler;
import org.openecomp.sdc.be.components.distribution.engine.DistributionEngineInitTask;
import org.openecomp.sdc.be.components.distribution.engine.SubscriberTypeEnum;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.distribution.api.client.CambriaOperationStatus;
import org.openecomp.sdc.be.distribution.api.client.RegistrationRequest;
import org.openecomp.sdc.be.distribution.api.client.TopicRegistrationResponse;
import org.openecomp.sdc.be.distribution.api.client.TopicUnregistrationResponse;
import org.openecomp.sdc.common.datastructure.Wrapper;

public class DistributionBusinessLogicTest extends BaseConfDependentTest {

	@InjectMocks
	DistributionBusinessLogic distributionBusinessLogic = Mockito.spy(DistributionBusinessLogic.class);

	CambriaHandler cambriaHandler = Mockito.mock(CambriaHandler.class);
	AuditHandler auditHandler = Mockito.mock(AuditHandler.class);

	@Test
	public void testHandleRegistrationHappyScenario() {
		CambriaErrorResponse okResponse = new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);
		Mockito.when(cambriaHandler.registerToTopic(Mockito.anyCollection(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SubscriberTypeEnum.class))).thenReturn(okResponse);

		Wrapper<Response> responseWrapper = new Wrapper<>();
		RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", "myEnv");
		distributionBusinessLogic.handleRegistration(responseWrapper, registrationRequest, auditHandler);

		Mockito.verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(responseWrapper, registrationRequest, SubscriberTypeEnum.PRODUCER);
		Mockito.verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(responseWrapper, registrationRequest, SubscriberTypeEnum.CONSUMER);
		Mockito.verify(cambriaHandler, Mockito.times(0)).unRegisterFromTopic(Mockito.anyCollection(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SubscriberTypeEnum.class));

		assertTrue(!responseWrapper.isEmpty());
		Response response = responseWrapper.getInnerElement();
		assertTrue(response.getStatus() == HttpStatus.SC_OK);

		TopicRegistrationResponse okTopicResponse = (TopicRegistrationResponse) response.getEntity();

		String expectedStatusTopicName = DistributionEngineInitTask.buildTopicName(configurationManager.getDistributionEngineConfiguration().getDistributionStatusTopicName(), registrationRequest.getDistrEnvName());
		String actualStatusTopicName = okTopicResponse.getDistrStatusTopicName();
		assertEquals(expectedStatusTopicName, actualStatusTopicName);

		String expectedNotificationTopicName = DistributionEngineInitTask.buildTopicName(configurationManager.getDistributionEngineConfiguration().getDistributionNotifTopicName(), registrationRequest.getDistrEnvName());
		String actualNotificationTopicName = okTopicResponse.getDistrNotificationTopicName();
		assertEquals(expectedNotificationTopicName, actualNotificationTopicName);

	}

	@Test
	public void testHandleRegistrationFailedScenario() {
		CambriaErrorResponse okResponse = new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);
		CambriaErrorResponse errorResponse = new CambriaErrorResponse(CambriaOperationStatus.CONNNECTION_ERROR, HttpStatus.SC_SERVICE_UNAVAILABLE);
		DistributionEngineConfiguration config = configurationManager.getDistributionEngineConfiguration();
		RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", "myEnv");
		String expectedStatusTopicName = DistributionEngineInitTask.buildTopicName(config.getDistributionStatusTopicName(), registrationRequest.getDistrEnvName());
		String expectedNotificationTopicName = DistributionEngineInitTask.buildTopicName(config.getDistributionNotifTopicName(), registrationRequest.getDistrEnvName());

		Mockito.when(cambriaHandler.registerToTopic(config.getUebServers(), expectedStatusTopicName, config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.PRODUCER)).thenReturn(okResponse);
		Mockito.when(cambriaHandler.registerToTopic(config.getUebServers(), expectedNotificationTopicName, config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.CONSUMER))
				.thenReturn(errorResponse);

		Wrapper<Response> responseWrapper = new Wrapper<>();

		distributionBusinessLogic.handleRegistration(responseWrapper, registrationRequest, auditHandler);

		Mockito.verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(responseWrapper, registrationRequest, SubscriberTypeEnum.PRODUCER);
		Mockito.verify(distributionBusinessLogic, Mockito.times(1)).registerDistributionClientToTopic(responseWrapper, registrationRequest, SubscriberTypeEnum.CONSUMER);
		Mockito.verify(cambriaHandler, Mockito.times(1)).unRegisterFromTopic(config.getUebServers(), expectedStatusTopicName, config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.PRODUCER);

		assertTrue(!responseWrapper.isEmpty());
		Response response = responseWrapper.getInnerElement();
		assertTrue(response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR);

	}

	@Test
	public void testHandleUnRegistrationHappyScenario() {
		CambriaErrorResponse okResponse = new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);

		Mockito.when(cambriaHandler.unRegisterFromTopic(Mockito.anyCollection(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SubscriberTypeEnum.class))).thenReturn(okResponse);

		Wrapper<Response> responseWrapper = new Wrapper<>();
		RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", "myEnv");
		distributionBusinessLogic.handleUnRegistration(responseWrapper, registrationRequest, auditHandler);

		Mockito.verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(responseWrapper, registrationRequest, SubscriberTypeEnum.PRODUCER);
		Mockito.verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(responseWrapper, registrationRequest, SubscriberTypeEnum.CONSUMER);
		Mockito.verify(distributionBusinessLogic, Mockito.times(1)).unRegisterDistributionClientFromTopic(registrationRequest, SubscriberTypeEnum.PRODUCER);
		Mockito.verify(distributionBusinessLogic, Mockito.times(1)).unRegisterDistributionClientFromTopic(registrationRequest, SubscriberTypeEnum.CONSUMER);

		Mockito.verify(cambriaHandler, Mockito.times(2)).unRegisterFromTopic(Mockito.anyCollection(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SubscriberTypeEnum.class));

		assertTrue(!responseWrapper.isEmpty());
		Response response = responseWrapper.getInnerElement();
		assertTrue(response.getStatus() == HttpStatus.SC_OK);

		TopicUnregistrationResponse okTopicUnregisterResponse = (TopicUnregistrationResponse) response.getEntity();

		String expectedStatusTopicName = DistributionEngineInitTask.buildTopicName(configurationManager.getDistributionEngineConfiguration().getDistributionStatusTopicName(), registrationRequest.getDistrEnvName());
		String actualStatusTopicName = okTopicUnregisterResponse.getDistrStatusTopicName();
		assertEquals(expectedStatusTopicName, actualStatusTopicName);

		String expectedNotificationTopicName = DistributionEngineInitTask.buildTopicName(configurationManager.getDistributionEngineConfiguration().getDistributionNotifTopicName(), registrationRequest.getDistrEnvName());
		String actualNotificationTopicName = okTopicUnregisterResponse.getDistrNotificationTopicName();
		assertEquals(expectedNotificationTopicName, actualNotificationTopicName);

		assertEquals(okTopicUnregisterResponse.getNotificationUnregisterResult(), CambriaOperationStatus.OK);
		assertEquals(okTopicUnregisterResponse.getStatusUnregisterResult(), CambriaOperationStatus.OK);

	}

	@Test
	public void testHandleUnRegistrationFailedScenario() {
		CambriaErrorResponse okResponse = new CambriaErrorResponse(CambriaOperationStatus.OK, HttpStatus.SC_OK);
		CambriaErrorResponse errorResponse = new CambriaErrorResponse(CambriaOperationStatus.AUTHENTICATION_ERROR, HttpStatus.SC_INTERNAL_SERVER_ERROR);

		Wrapper<Response> responseWrapper = new Wrapper<>();
		RegistrationRequest registrationRequest = new RegistrationRequest("myPublicKey", "myEnv");
		DistributionEngineConfiguration config = configurationManager.getDistributionEngineConfiguration();
		String expectedStatusTopicName = DistributionEngineInitTask.buildTopicName(config.getDistributionStatusTopicName(), registrationRequest.getDistrEnvName());
		String expectedNotificationTopicName = DistributionEngineInitTask.buildTopicName(config.getDistributionNotifTopicName(), registrationRequest.getDistrEnvName());
		Mockito.when(cambriaHandler.unRegisterFromTopic(config.getUebServers(), expectedStatusTopicName, config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.PRODUCER)).thenReturn(okResponse);
		Mockito.when(cambriaHandler.unRegisterFromTopic(config.getUebServers(), expectedNotificationTopicName, config.getUebPublicKey(), config.getUebSecretKey(), registrationRequest.getApiPublicKey(), SubscriberTypeEnum.CONSUMER))
				.thenReturn(errorResponse);

		distributionBusinessLogic.handleUnRegistration(responseWrapper, registrationRequest, auditHandler);

		Mockito.verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(responseWrapper, registrationRequest, SubscriberTypeEnum.PRODUCER);
		Mockito.verify(distributionBusinessLogic, Mockito.times(0)).registerDistributionClientToTopic(responseWrapper, registrationRequest, SubscriberTypeEnum.CONSUMER);
		Mockito.verify(distributionBusinessLogic, Mockito.times(1)).unRegisterDistributionClientFromTopic(registrationRequest, SubscriberTypeEnum.PRODUCER);
		Mockito.verify(distributionBusinessLogic, Mockito.times(1)).unRegisterDistributionClientFromTopic(registrationRequest, SubscriberTypeEnum.CONSUMER);

		assertTrue(!responseWrapper.isEmpty());
		Response response = responseWrapper.getInnerElement();
		assertTrue(response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR);

		TopicUnregistrationResponse okTopicUnregisterResponse = (TopicUnregistrationResponse) response.getEntity();

		String actualStatusTopicName = okTopicUnregisterResponse.getDistrStatusTopicName();
		assertEquals(expectedStatusTopicName, actualStatusTopicName);

		String actualNotificationTopicName = okTopicUnregisterResponse.getDistrNotificationTopicName();
		assertEquals(expectedNotificationTopicName, actualNotificationTopicName);

		assertEquals(okTopicUnregisterResponse.getNotificationUnregisterResult(), CambriaOperationStatus.AUTHENTICATION_ERROR);
		assertEquals(okTopicUnregisterResponse.getStatusUnregisterResult(), CambriaOperationStatus.OK);

	}

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		Mockito.reset(cambriaHandler);
	}

}
