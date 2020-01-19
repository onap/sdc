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

import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.utils.OperationalEnvironmentBuilder;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DistributionEngineTest{

	public static final String DISTRIBUTION_ID = "distId";
	public static final String ENV_ID = "envId";
	public static final String USER_ID = "userId";
	public static final String MODIFIER = "modifier";

	@InjectMocks
	private DistributionEngine testInstance;

	@Mock
	private EnvironmentsEngine environmentsEngine;

	@Mock
	private DistributionNotificationSender distributionNotificationSender;
	
	@Mock
	private ServiceDistributionArtifactsBuilder serviceDistributionArtifactsBuilder;
	
	private DummyDistributionConfigurationManager distributionEngineConfigurationMock;

	private Map<String, OperationalEnvironmentEntry> envs;

    private User modifier = new User();

    private Configuration.EnvironmentContext environmentContext = mock(Configuration.EnvironmentContext.class);

	@Before
	public void setUpMock() throws Exception {
		MockitoAnnotations.initMocks(this);
		distributionEngineConfigurationMock = new DummyDistributionConfigurationManager();
		envs = getEnvs(ENV_ID);
        modifier.setUserId(USER_ID);
        modifier.setFirstName(MODIFIER);
        modifier.setLastName(MODIFIER);
        when(environmentContext.getDefaultValue()).thenReturn("General_Revenue-Bearing");
        when(distributionEngineConfigurationMock.getConfiguration().getEnvironmentContext())
				.thenReturn(environmentContext);
	}

    @Test
    public void notifyService() throws Exception {
        NotificationDataImpl notificationData = new NotificationDataImpl();
        Service service = new Service();
        when(environmentsEngine.getEnvironmentById(ENV_ID)).thenReturn(envs.get(ENV_ID));
        when(distributionEngineConfigurationMock.getConfigurationMock().getDistributionNotifTopicName()).thenReturn("topic");
        when(distributionNotificationSender.sendNotification(eq("topic-ENVID"), eq(DISTRIBUTION_ID), any(EnvironmentMessageBusData.class),
                any(NotificationDataImpl.class), any(Service.class), any(User.class)))
        .thenReturn(ActionStatus.OK);
        ActionStatus actionStatus = testInstance.notifyService(DISTRIBUTION_ID, service, notificationData, ENV_ID, modifier);
        assertEquals(ActionStatus.OK, actionStatus);
    }

    @Test
    public void notifyService_couldNotResolveEnvironment() throws Exception {
        when(environmentsEngine.getEnvironments()).thenReturn(envs);
        ActionStatus actionStatus = testInstance.notifyService(DISTRIBUTION_ID, new Service(), new NotificationDataImpl(), "someNonExisitngEnv", modifier);
        assertEquals(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE, actionStatus);
        verifyZeroInteractions(distributionNotificationSender);
    }

    @Test
    public void notifyService_failedWhileSendingNotification() throws Exception {
        NotificationDataImpl notificationData = new NotificationDataImpl();
        Service service = new Service();
        when(environmentsEngine.getEnvironmentById(ENV_ID)).thenReturn(envs.get(ENV_ID));
        when(distributionEngineConfigurationMock.getConfigurationMock().getDistributionNotifTopicName()).thenReturn("topic");
        when(distributionNotificationSender.sendNotification(eq("topic-ENVID"), eq(DISTRIBUTION_ID), any(EnvironmentMessageBusData.class),
                any(NotificationDataImpl.class), any(Service.class), any(User.class)))
                .thenReturn(ActionStatus.GENERAL_ERROR);
        ActionStatus actionStatus = testInstance.notifyService(DISTRIBUTION_ID, service, notificationData, ENV_ID, modifier);
        assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
    }

	private Map<String, OperationalEnvironmentEntry> getEnvs(String... environmentIds) {
		Set<String> uebAddress = new HashSet<>();
		uebAddress.add("someAddress");
		return Stream.of(environmentIds)
				.map(id -> new OperationalEnvironmentBuilder().setEnvId(id).setDmaapUebAddress(uebAddress).build())
				.collect(Collectors.toMap(OperationalEnvironmentEntry::getEnvironmentId, Function.identity()));
	}

	private DistributionEngine createTestSubject() {
		return new DistributionEngine();
	}

	@Test(expected=NullPointerException.class)
	public void testInit() throws Exception {
		DistributionEngine testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "init");
	}

	@Test
	public void testShutdown() throws Exception {
		DistributionEngine testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.shutdown();
	}

	@Test
	public void testValidateConfiguration() throws Exception {
		DistributionEngine testSubject;
		DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
		deConfiguration.setDistributionStatusTopic((new DistributionStatusTopicConfig()));
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "validateConfiguration", deConfiguration);
	}

	@Test
	public void testIsValidServers() throws Exception {
		DistributionEngine testSubject;
		List<String> uebServers = null;
		String methodName = "";
		String paramName = "";
		boolean result;

		// test 1
		testSubject = createTestSubject();
		uebServers = null;
		result = Deencapsulation.invoke(testSubject, "isValidServers",
				new Object[] { List.class, methodName, paramName });
		Assert.assertEquals(false, result);
	}

	@Test
	public void testIsValidFqdn() throws Exception {
		DistributionEngine testSubject;
		String serverFqdn = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "isValidFqdn", new Object[] { serverFqdn });
	}

	@Test
	public void testIsValidParam() throws Exception {
		DistributionEngine testSubject;
		String paramValue = "";
		String methodName = "";
		String paramName = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "isValidParam",
				new Object[] { paramValue, methodName, paramName });
	}

	@Test
	public void testIsValidParam_1() throws Exception {
		DistributionEngine testSubject;
		List<String> paramValue = null;
		String methodName = "";
		String paramName = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "isValidParam",
				new Object[] { List.class, methodName, paramName });
	}

	@Test
	public void testIsValidObject() throws Exception {
		DistributionEngine testSubject;
		Object paramValue = null;
		String methodName = "";
		String paramName = "";
		boolean result;

		// test 1
		testSubject = createTestSubject();
		paramValue = null;
		result = Deencapsulation.invoke(testSubject, "isValidObject",
				new Object[] { Object.class, methodName, paramName });
		Assert.assertEquals(false, result);
	}

	@Test
	public void testGetEnvironmentErrorDescription() throws Exception {
		DistributionEngine testSubject;
		StorageOperationStatus status = null;
		String result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "getEnvironmentErrorDescription",
				 StorageOperationStatus.DISTR_ENVIRONMENT_NOT_AVAILABLE);
		result = Deencapsulation.invoke(testSubject, "getEnvironmentErrorDescription",
				 StorageOperationStatus.DISTR_ENVIRONMENT_NOT_FOUND);
		result = Deencapsulation.invoke(testSubject, "getEnvironmentErrorDescription",
				 StorageOperationStatus.DISTR_ENVIRONMENT_SENT_IS_INVALID);
		result = Deencapsulation.invoke(testSubject, "getEnvironmentErrorDescription",
				 StorageOperationStatus.ARTIFACT_NOT_FOUND);
	}

	@Test
	public void testIsEnvironmentAvailable() throws Exception {
		DistributionEngine testSubject;
		String envName = "";
		StorageOperationStatus result;

		// test 1
		testSubject = createTestSubject();
		envName = null;
		result = testSubject.isEnvironmentAvailable(envName);
		Assert.assertEquals(StorageOperationStatus.DISTR_ENVIRONMENT_SENT_IS_INVALID, result);

		// test 2
		testSubject = createTestSubject();
		envName = "mock";
		result = testSubject.isEnvironmentAvailable(envName);
		Assert.assertEquals(StorageOperationStatus.DISTR_ENVIRONMENT_NOT_FOUND, result);
	}

	//TODO Create test coverage for this method
	/*@Test
	public void testIsEnvironmentAvailable_1() throws Exception {
		DistributionEngine testSubject;
		StorageOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testInstance.isEnvironmentAvailable();
	}*/

	@Test(expected=NullPointerException.class)
	public void testDisableEnvironment() throws Exception {
		DistributionEngine testSubject;
		String envName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.disableEnvironment(envName);
	}

	@Test
	public void testBuildTopicName() throws Exception {
		DistributionEngine testSubject;
		String envName = "";
		String result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "buildTopicName", new Object[] { envName });
	}

	@Test
	public void testIsReadyForDistribution() throws Exception {
		DistributionEngine testSubject;
		Service service = null;
		String envName = "";
		StorageOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isReadyForDistribution(envName);
	}


	@Test
	public void testGetEnvironmentById() throws Exception {
		DistributionEngine testSubject;
		String opEnvId = "";
		OperationalEnvironmentEntry result;

		// default test
		when(environmentsEngine.getEnvironmentById(ArgumentMatchers.anyString())).thenReturn(new OperationalEnvironmentEntry());
		result = testInstance.getEnvironmentById(opEnvId);
	}

	@Test
	public void testBuildServiceForDistribution() throws Exception {
		Service service = new Service();
		String distributionId = "";
		String workloadContext = "";
		INotificationData result;

		// default test
		//testSubject = createTestSubject();
		when(serviceDistributionArtifactsBuilder.buildResourceInstanceForDistribution(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(new NotificationDataImpl());
		result = testInstance.buildServiceForDistribution(service, distributionId, workloadContext);
	}
}
