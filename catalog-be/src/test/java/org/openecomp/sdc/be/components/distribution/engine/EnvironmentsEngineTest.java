package org.openecomp.sdc.be.components.distribution.engine;

import com.att.nsa.apiClient.credentials.ApiCredential;
import fj.data.Either;
import mockit.Deencapsulation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.distribution.engine.IDmaapNotificationData.DmaapActionEnum;
import org.openecomp.sdc.be.components.distribution.engine.IDmaapNotificationData.OperationaEnvironmentTypeEnum;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.openecomp.sdc.common.http.client.api.HttpResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class EnvironmentsEngineTest extends BeConfDependentTest {

	@InjectMocks
	EnvironmentsEngine testSubject;

	@Mock
	ComponentsUtils componentUtils;

	@Mock
	OperationalEnvironmentDao operationalEnvironmentDao;

	@Mock
	CambriaHandler cambriaHandler;
	
	@Mock
	AaiRequestHandler aaiRequestHandler;
	
	@Before
	public void setUpMocks() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testInit() throws Exception {
		// default test
		Deencapsulation.invoke(testSubject, "init");
	}

	@Test
	public void testConnectUebTopicTenantIsolation() throws Exception {
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();
		opEnvEntry.setEnvironmentId("mock");
		AtomicBoolean status = null;
		Map<String, DistributionEngineInitTask> envNamePerInitTask = new HashMap<>();
		Map<String, DistributionEnginePollingTask> envNamePerPollingTask = new HashMap<>();

		// default test
		testSubject.connectUebTopicTenantIsolation(opEnvEntry, status, envNamePerInitTask, envNamePerPollingTask);
	}

	@Test
	public void testConnectUebTopic() throws Exception {
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();
		AtomicBoolean status = new AtomicBoolean(true);
		Map<String, DistributionEngineInitTask> envNamePerInitTask = new HashMap<>();
		Map<String, DistributionEnginePollingTask> envNamePerPollingTask = new HashMap<>();

		// default test
		Deencapsulation.invoke(testSubject, "connectUebTopic", opEnvEntry, status, envNamePerInitTask,
				envNamePerPollingTask);
	}

	@Test
	public void testHandleMessage() throws Exception {
		String notification = "";
		boolean result;

		// default test
		result = testSubject.handleMessage(notification);
	}

	@Test
	public void testHandleMessageLogic() throws Exception {
		String notification = "";
		boolean result;

		// default test
		result = testSubject.handleMessageLogic(notification);
	}

	@Test
	public void testValidateNotification() throws Exception {
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		errorWrapper.setInnerElement(true);
		IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);
		IDmaapAuditNotificationData auditNotificationData = Mockito.mock(IDmaapAuditNotificationData.class);

		// default test
		Deencapsulation.invoke(testSubject, "validateNotification", errorWrapper, notificationData,
				auditNotificationData);
	}

	@Test
	public void testSaveEntryWithFailedStatus() throws Exception {
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();

		// default test
		Deencapsulation.invoke(testSubject, "saveEntryWithFailedStatus", errorWrapper, opEnvEntry);
	}

	@Test
	public void testRetrieveUebAddressesFromAftDme() throws Exception {
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();

		// default test
		Deencapsulation.invoke(testSubject, "retrieveUebAddressesFromAftDme", errorWrapper, opEnvEntry);
	}

	@Test
	public void testCreateUebKeys() throws Exception {
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();

		Set<String> dmaapUebAddress = new HashSet<>();
		dmaapUebAddress.add("mock");
		opEnvEntry.setDmaapUebAddress(dmaapUebAddress);
		
		Mockito.when(cambriaHandler.createUebKeys(Mockito.any())).thenReturn(Either.left(new ApiCredential("mock", "mock")));
		
		// default test
		Deencapsulation.invoke(testSubject, "createUebKeys", errorWrapper, opEnvEntry);
	}
	
	@Test
	public void testCreateUebKeysError() throws Exception {
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();

		Set<String> dmaapUebAddress = new HashSet<>();
		dmaapUebAddress.add("mock");
		opEnvEntry.setDmaapUebAddress(dmaapUebAddress);
		
		Mockito.when(cambriaHandler.createUebKeys(Mockito.any())).thenReturn(Either.right(new CambriaErrorResponse()));
		
		// default test
		Deencapsulation.invoke(testSubject, "createUebKeys", errorWrapper, opEnvEntry);
	}
	
	@Test
	public void testRetrieveOpEnvInfoFromAAI() throws Exception {
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();
		opEnvEntry.setEnvironmentId("mock");
		Mockito.when(aaiRequestHandler.getOperationalEnvById(Mockito.nullable(String.class))).thenReturn(new HttpResponse<String>("{}", 200));
		// default test
		Deencapsulation.invoke(testSubject, "retrieveOpEnvInfoFromAAI", new Wrapper<>(), opEnvEntry);
	}

	@Test
	public void testRetrieveOpEnvInfoFromAAIError() throws Exception {
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();
		opEnvEntry.setEnvironmentId("mock");
		Mockito.when(aaiRequestHandler.getOperationalEnvById(Mockito.nullable(String.class))).thenReturn(new HttpResponse<String>("{}", 500));
		// default test
		Deencapsulation.invoke(testSubject, "retrieveOpEnvInfoFromAAI", new Wrapper<>(), opEnvEntry);
	}
	
	@Test
	public void testSaveEntryWithInProgressStatus() throws Exception {
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		Wrapper<OperationalEnvironmentEntry> opEnvEntryWrapper = new Wrapper<>();
		IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);

		Deencapsulation.invoke(testSubject, "saveEntryWithInProgressStatus", errorWrapper, opEnvEntryWrapper,
				notificationData);
	}

	@Test
	public void testValidateStateGeneralError() throws Exception {
		Wrapper<Boolean> errorWrapper = null;
		IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);

		Mockito.when(operationalEnvironmentDao.get(Mockito.nullable(String.class)))
				.thenReturn(Either.right(CassandraOperationStatus.GENERAL_ERROR));

		Deencapsulation.invoke(testSubject, "validateState", new Wrapper<>(), notificationData);
	}

	@Test
	public void testValidateState() throws Exception {
		Wrapper<Boolean> errorWrapper = null;
		IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);

		OperationalEnvironmentEntry a = new OperationalEnvironmentEntry();
		a.setStatus(EnvironmentStatusEnum.IN_PROGRESS.getName());
		Mockito.when(operationalEnvironmentDao.get(Mockito.nullable(String.class))).thenReturn(Either.left(a));

		Deencapsulation.invoke(testSubject, "validateState", new Wrapper<>(), notificationData);
	}

	@Test
	public void testValidateActionType() throws Exception {
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);
		Mockito.when(notificationData.getAction()).thenReturn(DmaapActionEnum.DELETE);

		Deencapsulation.invoke(testSubject, "validateActionType", errorWrapper, notificationData);
	}

	@Test
	public void testValidateActionType2() throws Exception {
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);
		Mockito.when(notificationData.getAction()).thenReturn(DmaapActionEnum.CREATE);

		Deencapsulation.invoke(testSubject, "validateActionType", errorWrapper, notificationData);
	}

	@Test
	public void testValidateEnvironmentType() throws Exception {
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);
		IDmaapAuditNotificationData auditNotificationData = Mockito.mock(IDmaapAuditNotificationData.class);
		Mockito.when(auditNotificationData.getOperationalEnvironmentName()).thenReturn("mock");
		Mockito.when(notificationData.getOperationalEnvironmentType()).thenReturn(OperationaEnvironmentTypeEnum.ECOMP);

		// default test
		Deencapsulation.invoke(testSubject, "validateEnvironmentType", errorWrapper, notificationData,
				auditNotificationData);
	}

	@Test
	public void testValidateEnvironmentType1() throws Exception {
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		IDmaapNotificationData notificationData = Mockito.mock(IDmaapNotificationData.class);
		IDmaapAuditNotificationData auditNotificationData = Mockito.mock(IDmaapAuditNotificationData.class);
		Mockito.when(auditNotificationData.getOperationalEnvironmentName()).thenReturn("mock");
		Mockito.when(notificationData.getOperationalEnvironmentType()).thenReturn(OperationaEnvironmentTypeEnum.UNKONW);
		Mockito.when(notificationData.getAction()).thenReturn(DmaapActionEnum.CREATE);

		Deencapsulation.invoke(testSubject, "validateEnvironmentType", errorWrapper, notificationData,
				auditNotificationData);
	}

	@Test
	public void testMap2OpEnvKey() throws Exception {
		OperationalEnvironmentEntry entry = new OperationalEnvironmentEntry();
		String result;

		// default test
		result = Deencapsulation.invoke(testSubject, "map2OpEnvKey", entry);
	}

	@Test
	public void testReadEnvFromConfig() throws Exception {
		OperationalEnvironmentEntry result;

		// default test
		result = Deencapsulation.invoke(testSubject, "readEnvFromConfig");
	}

	@Test
	public void testCreateUebTopicsForEnvironment() throws Exception {
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();

		// default test
		testSubject.createUebTopicsForEnvironment(opEnvEntry);
	}

	@Test
	public void testSetConfigurationManager() throws Exception {
		ConfigurationManager configurationManager = null;

		// default test
		Deencapsulation.invoke(testSubject, "setConfigurationManager", new Object[] { ConfigurationManager.class });
	}

	@Test
	public void testGetEnvironments() throws Exception {
		Map<String, OperationalEnvironmentEntry> result;

		// default test
		result = testSubject.getEnvironments();
	}

	@Test
	public void testIsInMap() throws Exception {
		OperationalEnvironmentEntry env = new OperationalEnvironmentEntry();
		env.setEnvironmentId("mock");
		Map<String, OperationalEnvironmentEntry> mockEnvironments = new HashMap<>();
		mockEnvironments.put("mock", new OperationalEnvironmentEntry());
		boolean result;

		// default test
		ReflectionTestUtils.setField(testSubject, "environments", mockEnvironments);
		result = testSubject.isInMap(env);
	}
}