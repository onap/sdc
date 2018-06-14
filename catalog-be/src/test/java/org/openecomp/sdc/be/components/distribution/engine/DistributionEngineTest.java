package org.openecomp.sdc.be.components.distribution.engine;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.utils.OperationalEnvironmentBuilder;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.CreateTopicConfig;
import org.openecomp.sdc.be.config.DistributionEngineConfiguration.DistributionStatusTopicConfig;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

import mockit.Deencapsulation;

public class DistributionEngineTest extends BeConfDependentTest{

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

	@Mock
	private DistributionEngineClusterHealth distributionEngineClusterHealth;

	private DummyDistributionConfigurationManager distributionEngineConfigurationMock;

	private Map<String, OperationalEnvironmentEntry> envs;

	@Before
	public void setUpMock() throws Exception {
		MockitoAnnotations.initMocks(this);
		distributionEngineConfigurationMock = new DummyDistributionConfigurationManager();
		envs = getEnvs(ENV_ID);
	}

	@Test
	public void notifyService() throws Exception {
		NotificationDataImpl notificationData = new NotificationDataImpl();
		Service service = new Service();
		when(environmentsEngine.getEnvironmentById(ENV_ID)).thenReturn(envs.get(ENV_ID));
		when(distributionEngineConfigurationMock.getConfigurationMock().getDistributionNotifTopicName())
				.thenReturn("topic");
		when(distributionNotificationSender.sendNotification(eq("topic-ENVID"), eq(DISTRIBUTION_ID),
				any(EnvironmentMessageBusData.class), any(NotificationDataImpl.class), any(Service.class), eq(USER_ID),
				eq(MODIFIER))).thenReturn(ActionStatus.OK);
		ActionStatus actionStatus = testInstance.notifyService(DISTRIBUTION_ID, service, notificationData, ENV_ID,
				USER_ID, MODIFIER);
		assertEquals(ActionStatus.OK, actionStatus);
	}

	@Test
	public void notifyService_couldNotResolveEnvironment() throws Exception {
		when(environmentsEngine.getEnvironments()).thenReturn(envs);
		ActionStatus actionStatus = testInstance.notifyService(DISTRIBUTION_ID, new Service(),
				new NotificationDataImpl(), "someNonExisitngEnv", USER_ID, MODIFIER);
		assertEquals(ActionStatus.DISTRIBUTION_ENVIRONMENT_NOT_AVAILABLE, actionStatus);
		verifyZeroInteractions(distributionNotificationSender);
	}

	@Test
	public void notifyService_failedWhileSendingNotification() throws Exception {
		NotificationDataImpl notificationData = new NotificationDataImpl();
		Service service = new Service();
		when(environmentsEngine.getEnvironmentById(ENV_ID)).thenReturn(envs.get(ENV_ID));
		when(distributionEngineConfigurationMock.getConfigurationMock().getDistributionNotifTopicName())
				.thenReturn("topic");
		when(distributionNotificationSender.sendNotification(eq("topic-ENVID"), eq(DISTRIBUTION_ID),
				any(EnvironmentMessageBusData.class), any(NotificationDataImpl.class), any(Service.class), eq(USER_ID),
				eq(MODIFIER))).thenReturn(ActionStatus.GENERAL_ERROR);
		ActionStatus actionStatus = testInstance.notifyService(DISTRIBUTION_ID, service, notificationData, ENV_ID,
				USER_ID, MODIFIER);
		assertEquals(ActionStatus.GENERAL_ERROR, actionStatus);
	}

	private Map<String, OperationalEnvironmentEntry> getEnvs(String... environmentIds) {
		Set<String> uebAddress = new HashSet<>();
		uebAddress.add("someAddress");
		return Stream.of(environmentIds)
				.map(id -> new OperationalEnvironmentBuilder().setEnvId(id).setDmaapUebAddress(uebAddress).build())
				.collect(Collectors.toMap(OperationalEnvironmentEntry::getEnvironmentId, Function.identity()));
	}

	@Test
	public void testIsActive() throws Exception {
		// default test
		testInstance.isActive();
	}
	
	@Test
	public void testInitDisabled() throws Exception {
		// default test
		Deencapsulation.invoke(testInstance, "init");
	}

	@Test
	public void testInitNotValidConfig() throws Exception {
		DistributionEngine testSubject;

		// default test
		Mockito.when(distributionEngineConfigurationMock.getConfigurationMock().isStartDistributionEngine())
				.thenReturn(true);
		Mockito.when(distributionEngineConfigurationMock.configurationMock.getDistributionStatusTopic())
				.thenReturn(new DistributionStatusTopicConfig());
		Deencapsulation.invoke(testInstance, "init");
	}

	@Test
	public void testInit() throws Exception {
		DistributionEngine testSubject;

		// default test
		Mockito.when(distributionEngineConfigurationMock.getConfigurationMock().isStartDistributionEngine())
				.thenReturn(true);
		DistributionStatusTopicConfig value = new DistributionStatusTopicConfig();
		value.setConsumerId("mock");
		value.setConsumerGroup("mock");
		value.setFetchTimeSec(0);
		value.setPollingIntervalSec(0);
		LinkedList<String> value2 = new LinkedList<>();
		value2.add("uebsb91kcdc.it.att.com:3904");
		value2.add("uebsb92kcdc.it.att.com:3904");
		Mockito.when(distributionEngineConfigurationMock.configurationMock.getUebServers()).thenReturn(value2);
		Mockito.when(distributionEngineConfigurationMock.configurationMock.getEnvironments()).thenReturn(value2);
		Mockito.when(distributionEngineConfigurationMock.configurationMock.getUebPublicKey()).thenReturn("mock");
		Mockito.when(distributionEngineConfigurationMock.configurationMock.getUebSecretKey()).thenReturn("mock");
		Mockito.when(distributionEngineConfigurationMock.configurationMock.getDistributionNotifTopicName())
				.thenReturn("mock");
		Mockito.when(distributionEngineConfigurationMock.configurationMock.getDistributionStatusTopicName())
				.thenReturn("mock");
		Mockito.when(distributionEngineConfigurationMock.configurationMock.getCreateTopic())
				.thenReturn(new CreateTopicConfig());
		Mockito.when(distributionEngineConfigurationMock.configurationMock.getDistributionStatusTopic())
				.thenReturn(value);
		Deencapsulation.invoke(testInstance, "init");
	}

	@Test
	public void testShutdown() throws Exception {
		DistributionEngine testSubject;

		// default test
		testInstance.shutdown();
	}

	@Test
	public void testValidateConfiguration() throws Exception {
		DistributionEngineConfiguration deConfiguration = new DistributionEngineConfiguration();
		deConfiguration.setDistributionStatusTopic((new DistributionStatusTopicConfig()));
		boolean result;

		// default test
		result = Deencapsulation.invoke(testInstance, "validateConfiguration", deConfiguration);
	}

	@Test
	public void testIsValidServers() throws Exception {
		List<String> uebServers = null;
		String methodName = "";
		String paramName = "";
		boolean result;

		// test 1
		uebServers = null;
		result = Deencapsulation.invoke(testInstance, "isValidServers",
				new Object[] { List.class, methodName, paramName });
		Assert.assertEquals(false, result);
	}

	@Test
	public void testIsValidFqdn() throws Exception {
		String serverFqdn = "";
		boolean result;

		// default test
		result = Deencapsulation.invoke(testInstance, "isValidFqdn", new Object[] { serverFqdn });
	}

	@Test
	public void testIsValidParam() throws Exception {
		String paramValue = "";
		String methodName = "";
		String paramName = "";
		boolean result;

		// default test
		result = Deencapsulation.invoke(testInstance, "isValidParam",
				new Object[] { paramValue, methodName, paramName });
	}

	@Test
	public void testIsValidParam_1() throws Exception {
		List<String> paramValue = null;
		String methodName = "";
		String paramName = "";
		boolean result;

		// default test
		result = Deencapsulation.invoke(testInstance, "isValidParam",
				new Object[] { List.class, methodName, paramName });
	}

	@Test
	public void testIsValidObject() throws Exception {
		Object paramValue = null;
		String methodName = "";
		String paramName = "";
		boolean result;

		// test 1
		paramValue = null;
		result = Deencapsulation.invoke(testInstance, "isValidObject",
				new Object[] { Object.class, methodName, paramName });
		Assert.assertEquals(false, result);
	}

	@Test
	public void testGetEnvironmentErrorDescription() throws Exception {
		StorageOperationStatus status = null;
		String result;

		// default test
		result = Deencapsulation.invoke(testInstance, "getEnvironmentErrorDescription",
				StorageOperationStatus.DISTR_ENVIRONMENT_NOT_AVAILABLE);
		result = Deencapsulation.invoke(testInstance, "getEnvironmentErrorDescription",
				StorageOperationStatus.DISTR_ENVIRONMENT_NOT_FOUND);
		result = Deencapsulation.invoke(testInstance, "getEnvironmentErrorDescription",
				StorageOperationStatus.DISTR_ENVIRONMENT_SENT_IS_INVALID);
		result = Deencapsulation.invoke(testInstance, "getEnvironmentErrorDescription",
				StorageOperationStatus.ARTIFACT_NOT_FOUND);
	}

	@Test
	public void testIsEnvironmentAvailable() throws Exception {
		DistributionEngine testSubject;
		String envName = "";
		StorageOperationStatus result;

		// test 1
		envName = null;
		result = testInstance.isEnvironmentAvailable(envName);
		Assert.assertEquals(StorageOperationStatus.DISTR_ENVIRONMENT_SENT_IS_INVALID, result);

		// test 2
		envName = "mock";
		result = testInstance.isEnvironmentAvailable(envName);
		Assert.assertEquals(StorageOperationStatus.DISTR_ENVIRONMENT_NOT_FOUND, result);
	}

	@Test
	public void testIsEnvironmentAvailable_1() throws Exception {
		DistributionEngine testSubject;
		StorageOperationStatus result;

		LinkedList<String> value = new LinkedList<>();
		value.add("mock");
		Mockito.when(distributionEngineConfigurationMock.configurationMock.getEnvironments()).thenReturn(value);
		testInstance.isEnvironmentAvailable();
	}

	@Test
	public void testBuildTopicName() throws Exception {
		String envName = "";
		String result;

		// default test
		result = Deencapsulation.invoke(testInstance, "buildTopicName", new Object[] { envName });
	}

	@Test
	public void testIsReadyForDistribution() throws Exception {
		Service service = null;
		String envName = "";
		StorageOperationStatus result;

		// default test
		result = testInstance.isReadyForDistribution(service, envName);
	}

	@Test
	public void testVerifyServiceHasDeploymentArtifactsTrue() throws Exception {
		Service service = new Service();
		StorageOperationStatus result;

		// default test
		when(serviceDistributionArtifactsBuilder
				.verifyServiceContainsDeploymentArtifacts(ArgumentMatchers.any(Service.class))).thenReturn(true);
		result = testInstance.verifyServiceHasDeploymentArtifacts(service);
	}

	@Test
	public void testVerifyServiceHasDeploymentArtifactsFalse() throws Exception {
		Service service = new Service();
		StorageOperationStatus result;

		// default test
		when(serviceDistributionArtifactsBuilder
				.verifyServiceContainsDeploymentArtifacts(ArgumentMatchers.any(Service.class))).thenReturn(false);
		result = testInstance.verifyServiceHasDeploymentArtifacts(service);
	}

	@Test
	public void testGetEnvironmentById() throws Exception {
		DistributionEngine testSubject;
		String opEnvId = "";
		OperationalEnvironmentEntry result;

		// default test
		when(environmentsEngine.getEnvironmentById(ArgumentMatchers.anyString()))
				.thenReturn(new OperationalEnvironmentEntry());
		result = testInstance.getEnvironmentById(opEnvId);
	}

	@Test
	public void testBuildServiceForDistribution() throws Exception {
		Service service = new Service();
		String distributionId = "";
		String workloadContext = "";
		INotificationData result;

		// default test
		// testSubject = createTestSubject();
		when(serviceDistributionArtifactsBuilder.buildResourceInstanceForDistribution(ArgumentMatchers.any(),
				ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(new NotificationDataImpl());
		result = testInstance.buildServiceForDistribution(service, distributionId, workloadContext);
	}
}