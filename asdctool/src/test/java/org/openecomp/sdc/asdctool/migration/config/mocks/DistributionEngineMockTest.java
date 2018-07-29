package org.openecomp.sdc.asdctool.migration.config.mocks;

import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.sdc.be.components.distribution.engine.INotificationData;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

@Ignore ("This class does not test anything, there is not a single assertion and the code with reflection fails")  
public class DistributionEngineMockTest {

	private DistributionEngineMock createTestSubject() {
		return new DistributionEngineMock();
	}

	@Test
	public void testIsActive() throws Exception {
		DistributionEngineMock testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isActive();
	}

	@Test
	public void testNotifyService() throws Exception {
		DistributionEngineMock testSubject;
		String distributionId = "";
		Service service = null;
		INotificationData notificationData = null;
		String envName = "";
		String userId = "";
		ActionStatus result;
		User modifierName=new User();

		// default test
		testSubject = createTestSubject();
		result = testSubject.notifyService(distributionId, service, notificationData, envName, userId, modifierName);
	}

	@Test
	public void testNotifyService_1() throws Exception {
		DistributionEngineMock testSubject;
		String distributionId = "";
		Service service = null;
		INotificationData notificationData = null;
		String envId = "";
		String envName = "";
		String userId = "";
		User modifierName = new User();
		ActionStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.notifyService(distributionId, service, notificationData, envId, envName,
				modifierName);
	}

	@Test
	public void testIsEnvironmentAvailable() throws Exception {
		DistributionEngineMock testSubject;
		String envName = "";
		StorageOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isEnvironmentAvailable(envName);
	}

	@Test
	public void testIsEnvironmentAvailable_1() throws Exception {
		DistributionEngineMock testSubject;
		StorageOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isEnvironmentAvailable();
	}

	@Test
	public void testDisableEnvironment() throws Exception {
		DistributionEngineMock testSubject;
		String envName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.disableEnvironment(envName);
	}

	@Ignore
	@Test
	public void testIsReadyForDistribution() throws Exception {
		DistributionEngineMock testSubject;
		Service service = null;
		String envName = "";
		StorageOperationStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isReadyForDistribution(envName);
	}

	@Test
	public void testBuildServiceForDistribution() throws Exception {
		DistributionEngineMock testSubject;
		Service service = null;
		String distributionId = "";
		String workloadContext = "";
		INotificationData result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.buildServiceForDistribution(service, distributionId, workloadContext);
	}

	@Test
	public void testGetEnvironmentById() throws Exception {
		DistributionEngineMock testSubject;
		String opEnvId = "";
		OperationalEnvironmentEntry result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEnvironmentById(opEnvId);
	}
}