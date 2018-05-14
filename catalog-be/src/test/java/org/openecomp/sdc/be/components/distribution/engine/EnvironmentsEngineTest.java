package org.openecomp.sdc.be.components.distribution.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.openecomp.sdc.be.auditing.impl.AuditingManager;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.components.distribution.engine.IDmaapNotificationData.DmaapActionEnum;
import org.openecomp.sdc.be.components.distribution.engine.IDmaapNotificationData.OperationaEnvironmentTypeEnum;
import org.openecomp.sdc.be.components.distribution.engine.report.DistributionCompleteReporter;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.cassandra.AuditCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.OperationalEnvironmentDao;
import org.openecomp.sdc.be.dao.impl.AuditingDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.info.OperationalEnvInfo;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;
import org.openecomp.sdc.common.datastructure.Wrapper;
import org.springframework.test.util.ReflectionTestUtils;

import fj.data.Either;
import mockit.Deencapsulation;

public class EnvironmentsEngineTest extends BeConfDependentTest {

	private EnvironmentsEngine createTestSubject() {
		return new EnvironmentsEngine(new DmaapConsumer(new ExecutorFactory(), new DmaapClientFactory()),
				new OperationalEnvironmentDao(), new DME2EndpointIteratorCreator(), new AaiRequestHandler(),
				new ComponentsUtils(new AuditingManager(new AuditingDao(), new AuditCassandraDao())),
				new CambriaHandler(), new DistributionEngineClusterHealth(), new DistributionCompleteReporterMock());
	}

	@Test
	public void testInit() throws Exception {
		EnvironmentsEngine testSubject;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "init");
	}

	@Test
	public void testConnectUebTopicTenantIsolation() throws Exception {
		EnvironmentsEngine testSubject;
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();
		opEnvEntry.setEnvironmentId("mock");
		AtomicBoolean status = null;
		Map<String, DistributionEngineInitTask> envNamePerInitTask = new HashMap<>();
		Map<String, DistributionEnginePollingTask> envNamePerPollingTask = new HashMap<>();

		// default test
		testSubject = createTestSubject();
		testSubject.connectUebTopicTenantIsolation(opEnvEntry, status, envNamePerInitTask, envNamePerPollingTask);
	}

	@Test
	public void testConnectUebTopic() throws Exception {
		EnvironmentsEngine testSubject;
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();
		AtomicBoolean status = new AtomicBoolean(true);
		Map<String, DistributionEngineInitTask> envNamePerInitTask = new HashMap<>();
		Map<String, DistributionEnginePollingTask> envNamePerPollingTask = new HashMap<>();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "connectUebTopic", opEnvEntry, status, envNamePerInitTask,
				envNamePerPollingTask);
	}

	@Test
	public void testHandleMessage() throws Exception {
		EnvironmentsEngine testSubject;
		String notification = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.handleMessage(notification);
	}

	@Test
	public void testHandleMessageLogic() throws Exception {
		EnvironmentsEngine testSubject;
		String notification = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.handleMessageLogic(notification);
	}

	@Test
	public void testValidateNotification() throws Exception {
		EnvironmentsEngine testSubject;
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		errorWrapper.setInnerElement(true);
		IDmaapNotificationDataMock notificationData = new IDmaapNotificationDataMock();
		IDmaapAuditNotificationDataMock auditNotificationData = new IDmaapAuditNotificationDataMock();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "validateNotification",
				errorWrapper, notificationData, auditNotificationData);
	}

	@Test(expected = NullPointerException.class)
	public void testSaveEntryWithFailedStatus() throws Exception {
		EnvironmentsEngine testSubject;
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "saveEntryWithFailedStatus", errorWrapper, opEnvEntry);
	}

	@Test
	public void testRetrieveUebAddressesFromAftDme() throws Exception {
		EnvironmentsEngine testSubject;
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "retrieveUebAddressesFromAftDme", errorWrapper, opEnvEntry);
	}

	@Test
	public void testCreateUebKeys() throws Exception {
		EnvironmentsEngine testSubject;
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();

		Set<String> dmaapUebAddress = new HashSet<>();
		dmaapUebAddress.add("mock");
		opEnvEntry.setDmaapUebAddress(dmaapUebAddress);

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "createUebKeys", errorWrapper, opEnvEntry);
	}

	/*@Test
	public void testRetrieveOpEnvInfoFromAAI() throws Exception {
		EnvironmentsEngine testSubject;
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();
		opEnvEntry.setEnvironmentId("mock");

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "retrieveOpEnvInfoFromAAI",
				Wrapper.class, opEnvEntry);
	}*/

	/*
	 * @Test public void testSaveEntryWithInProgressStatus() throws Exception {
	 * EnvironmentsEngine testSubject; Wrapper<Boolean> errorWrapper = new
	 * Wrapper<>(); Wrapper<OperationalEnvironmentEntry> opEnvEntryWrapper = new
	 * Wrapper<>(); IDmaapNotificationData notificationData = new
	 * IDmaapNotificationDataMock();
	 * 
	 * // default test testSubject = createTestSubject();
	 * Deencapsulation.invoke(testSubject, "saveEntryWithInProgressStatus",
	 * errorWrapper, opEnvEntryWrapper, notificationData); }
	 */

	/*
	 * @Test public void testValidateState() throws Exception { EnvironmentsEngine
	 * testSubject; Wrapper<Boolean> errorWrapper = null; IDmaapNotificationDataMock
	 * notificationData = new IDmaapNotificationDataMock();
	 * notificationData.setOperationalEnvironmentId("mock");
	 * 
	 * // default test testSubject = createTestSubject();
	 * Deencapsulation.invoke(testSubject, "validateState", Wrapper.class,
	 * notificationData); }
	 */

	@Test
	public void testValidateActionType() throws Exception {
		EnvironmentsEngine testSubject;
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		IDmaapNotificationDataMock notificationData = new IDmaapNotificationDataMock();
		notificationData.setDmaapActionEnum(DmaapActionEnum.DELETE);
		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "validateActionType", errorWrapper, notificationData);

		notificationData.setDmaapActionEnum(DmaapActionEnum.CREATE);
		Deencapsulation.invoke(testSubject, "validateActionType", errorWrapper, notificationData);
	}

	@Test(expected=NullPointerException.class)
	public void testValidateEnvironmentType() throws Exception {
		EnvironmentsEngine testSubject;
		Wrapper<Boolean> errorWrapper = new Wrapper<>();
		IDmaapNotificationDataMock notificationData = new IDmaapNotificationDataMock();
		IDmaapAuditNotificationDataMock auditNotificationData = new IDmaapAuditNotificationDataMock();
		auditNotificationData.operationalEnvironmentName = "mock";
		notificationData.operationaEnvironmentTypeEnum = OperationaEnvironmentTypeEnum.ECOMP;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "validateEnvironmentType", errorWrapper, notificationData,
				auditNotificationData);

		notificationData.operationaEnvironmentTypeEnum = OperationaEnvironmentTypeEnum.UNKONW;
		notificationData.setDmaapActionEnum(DmaapActionEnum.CREATE);
		Deencapsulation.invoke(testSubject, "validateEnvironmentType", errorWrapper, notificationData,
				auditNotificationData);
	}

	@Test
	public void testMap2OpEnvKey() throws Exception {
		EnvironmentsEngine testSubject;
		OperationalEnvironmentEntry entry = new OperationalEnvironmentEntry();
		String result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "map2OpEnvKey", entry);
	}

	@Test
	public void testReadEnvFromConfig() throws Exception {
		EnvironmentsEngine testSubject;
		OperationalEnvironmentEntry result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "readEnvFromConfig");
	}

	@Test
	public void testCreateUebTopicsForEnvironment() throws Exception {
		EnvironmentsEngine testSubject;
		OperationalEnvironmentEntry opEnvEntry = new OperationalEnvironmentEntry();

		// default test
		testSubject = createTestSubject();
		testSubject.createUebTopicsForEnvironment(opEnvEntry);
	}

	@Test
	public void testSetConfigurationManager() throws Exception {
		EnvironmentsEngine testSubject;
		ConfigurationManager configurationManager = null;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "setConfigurationManager", new Object[] { ConfigurationManager.class });
	}

	@Test
	public void testGetEnvironments() throws Exception {
		EnvironmentsEngine testSubject;
		Map<String, OperationalEnvironmentEntry> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEnvironments();
	}

	@Test
	public void testIsInMap() throws Exception {
		EnvironmentsEngine testSubject;
		OperationalEnvironmentEntry env = new OperationalEnvironmentEntry();
		env.setEnvironmentId("mock");
		Map<String, OperationalEnvironmentEntry> mockEnvironments = new HashMap<>();
		mockEnvironments.put("mock", new OperationalEnvironmentEntry());
		boolean result;

		// default test
		testSubject = createTestSubject();
		ReflectionTestUtils.setField(testSubject, "environments", mockEnvironments);
		result = testSubject.isInMap(env);
	}

	private class DistributionCompleteReporterMock implements DistributionCompleteReporter {
		@Override
		public void reportDistributionComplete(DistributionStatusNotification distributionStatusNotification) {
			// TODO Auto-generated method stub

		}
	}

	private class IDmaapNotificationDataMock implements IDmaapNotificationData {

		private String operationalEnvironmentId;
		private OperationaEnvironmentTypeEnum operationaEnvironmentTypeEnum;
		private DmaapActionEnum dmaapActionEnum;

		public OperationaEnvironmentTypeEnum getOperationaEnvironmentTypeEnum() {
			return operationaEnvironmentTypeEnum;
		}

		public void setOperationaEnvironmentTypeEnum(OperationaEnvironmentTypeEnum operationaEnvironmentTypeEnum) {
			this.operationaEnvironmentTypeEnum = operationaEnvironmentTypeEnum;
		}

		public DmaapActionEnum getDmaapActionEnum() {
			return dmaapActionEnum;
		}

		public void setDmaapActionEnum(DmaapActionEnum dmaapActionEnum) {
			this.dmaapActionEnum = dmaapActionEnum;
		}

		public void setOperationalEnvironmentId(String operationalEnvironmentId) {
			this.operationalEnvironmentId = operationalEnvironmentId;
		}

		@Override
		public String getOperationalEnvironmentId() {
			return operationalEnvironmentId;
		}

		@Override
		public OperationaEnvironmentTypeEnum getOperationalEnvironmentType() {
			return operationaEnvironmentTypeEnum;
		}

		@Override
		public DmaapActionEnum getAction() {
			return dmaapActionEnum;
		}
	}

	private class IDmaapAuditNotificationDataMock implements IDmaapAuditNotificationData {
		private String operationalEnvironmentName;
		private String tenantContext;

		@Override
		public String getOperationalEnvironmentName() {
			return null;
		}

		@Override
		public String getTenantContext() {
			return null;
		}

	}
}