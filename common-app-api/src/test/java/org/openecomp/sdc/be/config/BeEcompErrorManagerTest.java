package org.openecomp.sdc.be.config;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.config.BeEcompErrorManager.ErrorSeverity;
import org.openecomp.sdc.common.config.IEcompConfigurationManager;


public class BeEcompErrorManagerTest {

	private BeEcompErrorManager createTestSubject() {
		return  BeEcompErrorManager.getInstance();
	}

	
	@Test
	public void testGetInstance() throws Exception {
		BeEcompErrorManager result;

		// default test
		result = BeEcompErrorManager.getInstance();
	}

	

	
	@Test
	public void testGetConfigurationManager() throws Exception {
		BeEcompErrorManager testSubject;
		IEcompConfigurationManager result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConfigurationManager();
	}

	
	@Test
	public void testLogBeUebAuthenticationError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String reason = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeUebAuthenticationError(context, reason);
	}

	
	@Test
	public void testLogBeHealthCheckRecovery() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeHealthCheckRecovery(context);
	}

	
	@Test
	public void testLogBeHealthCheckTitanRecovery() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeHealthCheckTitanRecovery(context);
	}

	
	@Test
	public void testLogBeHealthCheckElasticSearchRecovery() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeHealthCheckElasticSearchRecovery(context);
	}

	
	@Test
	public void testLogBeHealthCheckUebClusterRecovery() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeHealthCheckUebClusterRecovery(context);
	}

	
	@Test
	public void testLogFeHealthCheckRecovery() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logFeHealthCheckRecovery(context);
	}

	
	@Test
	public void testLogBeHealthCheckError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeHealthCheckError(context);
	}

	
	@Test
	public void testLogBeHealthCheckTitanError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeHealthCheckTitanError(context);
	}

	
	@Test
	public void testLogBeHealthCheckElasticSearchError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeHealthCheckElasticSearchError(context);
	}

	
	@Test
	public void testLogBeHealthCheckUebClusterError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeHealthCheckUebClusterError(context);
	}

	
	@Test
	public void testLogFeHealthCheckError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logFeHealthCheckError(context);
	}

	
	@Test
	public void testLogBeUebConnectionError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String reason = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeUebConnectionError(context, reason);
	}

	
	@Test
	public void testLogBeUebUnkownHostError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String host = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeUebUnkownHostError(context, host);
	}

	
	@Test
	public void testLogBeComponentMissingError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String componentType = "";
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeComponentMissingError(context, componentType, name);
	}

	
	@Test
	public void testLogBeIncorrectComponentError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String componentType = "";
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeIncorrectComponentError(context, componentType, name);
	}

	
	@Test
	public void testLogBeInvalidConfigurationError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String parameterName = "";
		String parameterValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeInvalidConfigurationError(context, parameterName, parameterValue);
	}

	
	@Test
	public void testLogBeUebObjectNotFoundError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String notFoundObjectName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeUebObjectNotFoundError(context, notFoundObjectName);
	}

	
	@Test
	public void testLogBeDistributionEngineInvalidArtifactType() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String artifactType = "";
		String validArtifactTypes = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeDistributionEngineInvalidArtifactType(context, artifactType, validArtifactTypes);
	}

	
	@Test
	public void testLogBeMissingConfigurationError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String parameterName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeMissingConfigurationError(context, parameterName);
	}

	
	@Test
	public void testLogBeConfigurationInvalidListSizeError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String parameterName = "";
		int listMinimumSize = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.logBeConfigurationInvalidListSizeError(context, parameterName, listMinimumSize);
	}

	
	@Test
	public void testLogErrorConfigFileFormat() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logErrorConfigFileFormat(context, description);
	}

	
	@Test
	public void testLogBeMissingArtifactInformationError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String missingInfo = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeMissingArtifactInformationError(context, missingInfo);
	}

	
	@Test
	public void testLogBeArtifactMissingError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String artifactName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeArtifactMissingError(context, artifactName);
	}

	
	@Test
	public void testLogBeUserMissingError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String userId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeUserMissingError(context, userId);
	}

	
	@Test
	public void testLogBeInvalidTypeError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String type = "";
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeInvalidTypeError(context, type, name);
	}

	
	@Test
	public void testLogBeInvalidValueError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String value = "";
		String name = "";
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeInvalidValueError(context, value, name, type);
	}

	
	@Test
	public void testLogBeArtifactPayloadInvalid() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeArtifactPayloadInvalid(context);
	}

	
	@Test
	public void testLogBeArtifactInformationInvalidError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeArtifactInformationInvalidError(context);
	}

	
	@Test
	public void testLogBeDistributionMissingError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String distributionName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeDistributionMissingError(context, distributionName);
	}

	
	@Test
	public void testLogBeGraphObjectMissingError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String objectType = "";
		String objectName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeGraphObjectMissingError(context, objectType, objectName);
	}

	
	@Test
	public void testLogBeInvalidJsonInput() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeInvalidJsonInput(context);
	}

	
	@Test
	public void testLogBeInitializationError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeInitializationError(context);
	}

	
	@Test
	public void testLogBeFailedAddingResourceInstanceError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String resourceName = "";
		String serviceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeFailedAddingResourceInstanceError(context, resourceName, serviceId);
	}

	
	@Test
	public void testLogBeUebSystemError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String operation = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeUebSystemError(context, operation);
	}

	
	@Test
	public void testLogBeDistributionEngineSystemError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String operation = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeDistributionEngineSystemError(context, operation);
	}

	
	@Test
	public void testLogBeFailedAddingNodeTypeError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String nodeType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeFailedAddingNodeTypeError(context, nodeType);
	}

	
	@Test
	public void testLogBeDaoSystemError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeDaoSystemError(context);
	}

	
	@Test
	public void testLogBeSystemError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeSystemError(context);
	}

	
	@Test
	public void testLogBeExecuteRollbackError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeExecuteRollbackError(context);
	}

	
	@Test
	public void testLogBeFailedLockObjectError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String type = "";
		String id = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeFailedLockObjectError(context, type, id);
	}

	
	@Test
	public void testLogBeFailedCreateNodeError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String nodeName = "";
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeFailedCreateNodeError(context, nodeName, status);
	}

	
	@Test
	public void testLogBeFailedUpdateNodeError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String nodeName = "";
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeFailedUpdateNodeError(context, nodeName, status);
	}

	
	@Test
	public void testLogBeFailedDeleteNodeError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String nodeName = "";
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeFailedDeleteNodeError(context, nodeName, status);
	}

	
	@Test
	public void testLogBeFailedRetrieveNodeError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String nodeName = "";
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeFailedRetrieveNodeError(context, nodeName, status);
	}

	
	@Test
	public void testLogBeFailedFindParentError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String node = "";
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeFailedFindParentError(context, node, status);
	}

	
	@Test
	public void testLogBeFailedFindAllNodesError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String nodeType = "";
		String parentNode = "";
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeFailedFindAllNodesError(context, nodeType, parentNode, status);
	}

	
	@Test
	public void testLogBeFailedFindAssociationError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String nodeType = "";
		String fromNode = "";
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeFailedFindAssociationError(context, nodeType, fromNode, status);
	}

	
	@Test
	public void testLogBeComponentCleanerSystemError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String operation = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeComponentCleanerSystemError(context, operation);
	}

	
	@Test
	public void testLogBeRestApiGeneralError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logBeRestApiGeneralError(context);
	}

	
	@Test
	public void testLogFqdnResolveError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logFqdnResolveError(context, description);
	}

	
	@Test
	public void testLogSiteSwitchoverInfo() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.logSiteSwitchoverInfo(context, description);
	}

	
	@Test
	public void testLogInternalAuthenticationError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String description = "";
		ErrorSeverity severity = null;

		// test 1
		testSubject = createTestSubject();
		severity = null;
		testSubject.logInternalAuthenticationError(context, description, severity);
	}

	
	@Test
	public void testLogInternalConnectionError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String description = "";
		ErrorSeverity severity = null;

		// test 1
		testSubject = createTestSubject();
		severity = null;
		testSubject.logInternalConnectionError(context, description, severity);
	}

	
	@Test
	public void testLogInternalDataError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String description = "";
		ErrorSeverity severity = null;

		// test 1
		testSubject = createTestSubject();
		severity = null;
		testSubject.logInternalDataError(context, description, severity);
	}

	
	@Test
	public void testLogInvalidInputError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String description = "";
		ErrorSeverity severity = null;

		// test 1
		testSubject = createTestSubject();
		severity = null;
		testSubject.logInvalidInputError(context, description, severity);
	}

	
	@Test
	public void testLogInternalFlowError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String description = "";
		ErrorSeverity severity = null;

		// test 1
		testSubject = createTestSubject();
		severity = null;
		testSubject.logInternalFlowError(context, description, severity);
	}

	
	@Test
	public void testLogInternalUnexpectedError() throws Exception {
		BeEcompErrorManager testSubject;
		String context = "";
		String description = "";
		ErrorSeverity severity = null;

		// test 1
		testSubject = createTestSubject();
		severity = null;
		testSubject.logInternalUnexpectedError(context, description, severity);
	}
}