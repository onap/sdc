package org.openecomp.sdc.asdctool.impl.validator.config;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.ArtifactToolBL;
import org.openecomp.sdc.asdctool.impl.validator.ValidationToolBL;
import org.openecomp.sdc.asdctool.impl.validator.executers.*;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.ArtifactValidationUtils;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.ServiceArtifactValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.VfArtifactValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.tasks.moduleJson.ModuleJsonTask;
import org.openecomp.sdc.be.dao.JanusGraphClientStrategy;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.model.DerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.*;

public class ValidationToolConfigurationTest {

	private ValidationToolConfiguration createTestSubject() {
		return new ValidationToolConfiguration();
	}

	@Test
	public void testBasicServiceValidator() {
		ValidationToolConfiguration testSubject;
		ServiceValidatorExecuter result;
		JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);

		// default test
		testSubject = createTestSubject();
		result = testSubject.basicServiceValidator(janusGraphDaoMock);
	}

	@Test
	public void testVfArtifactValidationTask() {
		ValidationToolConfiguration testSubject;
		VfArtifactValidationTask result;
		ArtifactValidationUtils artifactValidationUtilsMock = mock(ArtifactValidationUtils.class);

		// default test
		testSubject = createTestSubject();
		result = testSubject.vfArtifactValidationTask(artifactValidationUtilsMock);
	}

	@Test
	public void testServiceArtifactValidationTask() {
		ValidationToolConfiguration testSubject;
		ServiceArtifactValidationTask result;
		ArtifactValidationUtils artifactValidationUtilsMock = mock(ArtifactValidationUtils.class);

		// default test
		testSubject = createTestSubject();
		result = testSubject.serviceArtifactValidationTask(artifactValidationUtilsMock);
	}

	@Test
	public void testModuleJsonTask() {
		ValidationToolConfiguration testSubject;
		ModuleJsonTask result;
		TopologyTemplateOperation topologyTemplateOperationMock = mock(TopologyTemplateOperation.class);

		// default test
		testSubject = createTestSubject();
		result = testSubject.moduleJsonTask(topologyTemplateOperationMock);
	}

	@Test
	public void testValidationToolBL() {
		ValidationToolConfiguration testSubject;
		ValidationToolBL result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validationToolBL(new ArrayList<>());
	}

	@Test
	public void testBasicVfValidator() {
		ValidationToolConfiguration testSubject;
		VfValidatorExecuter result;
		JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);

		// default test
		testSubject = createTestSubject();
		result = testSubject.basicVfValidator(new ArrayList<>(), janusGraphDaoMock);
	}

	@Test
	public void testArtifactCassandraDao() {
		ValidationToolConfiguration testSubject;
		ArtifactCassandraDao result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.artifactCassandraDao();
	}

	@Test
	public void testArtifactValidationUtils() {
		ValidationToolConfiguration testSubject;
		ArtifactValidationUtils result;
		ArtifactCassandraDao artifactCassandraDaoMock = mock(ArtifactCassandraDao.class);
		TopologyTemplateOperation topologyTemplateOperationMock = mock(TopologyTemplateOperation.class);

		// default test
		testSubject = createTestSubject();
		result = testSubject.artifactValidationUtils(artifactCassandraDaoMock, topologyTemplateOperationMock);
	}

	@Test
	public void testJsonGroupsOperation() {
		ValidationToolConfiguration testSubject;
		GroupsOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.jsonGroupsOperation();
	}

	@Test
	public void testCassandraClient() {
		ValidationToolConfiguration testSubject;
		CassandraClient result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.cassandraClient();
	}

	@Test
	public void testDaoStrategy() {
		ValidationToolConfiguration testSubject;
		JanusGraphClientStrategy result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.daoStrategy();
	}

	@Test
	public void testToscaOperationFacade() {
		ValidationToolConfiguration testSubject;
		ToscaOperationFacade result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toscaOperationFacade();
	}

	@Test
	public void testNodeTypeOperation() {
		ValidationToolConfiguration testSubject;
		DerivedNodeTypeResolver migrationDerivedNodeTypeResolver = null;
		NodeTypeOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.nodeTypeOperation(migrationDerivedNodeTypeResolver);
	}

	@Test
	public void testTopologyTemplateOperation() {
		ValidationToolConfiguration testSubject;
		TopologyTemplateOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.topologyTemplateOperation();
	}

	@Test
	public void testMigrationDerivedNodeTypeResolver() {
		ValidationToolConfiguration testSubject;
		DerivedNodeTypeResolver result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.migrationDerivedNodeTypeResolver();
	}

	@Test
	public void testJanusGraphDao() {
		ValidationToolConfiguration testSubject;
		JanusGraphClient janusGraphClient = null;
		JanusGraphDao result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.healingJanusGraphDao(janusGraphClient);
	}

	@Test
	public void testNodeToscaArtifactsValidatorValidator() {
		ValidationToolConfiguration testSubject;
		NodeToscaArtifactsValidatorExecuter result;
		JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
		ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);

		// default test
		testSubject = createTestSubject();
		result = testSubject.NodeToscaArtifactsValidatorValidator(janusGraphDaoMock, toscaOperationFacade);
	}

	@Test
	public void testServiceToscaArtifactsValidator() {
		ValidationToolConfiguration testSubject;
		ServiceToscaArtifactsValidatorExecutor result;
		JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
		ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);

		// default test
		testSubject = createTestSubject();
		result = testSubject.ServiceToscaArtifactsValidator(janusGraphDaoMock, toscaOperationFacade);
	}

	@Test
	public void testVFToscaArtifactValidator() {
		ValidationToolConfiguration testSubject;
		VFToscaArtifactValidatorExecutor result;
		JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
		ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);

		// default test
		testSubject = createTestSubject();
		result = testSubject.VFToscaArtifactValidator(janusGraphDaoMock, toscaOperationFacade);
	}

	@Test
	public void testArtifactToolBL() {
		ValidationToolConfiguration testSubject;
		ArtifactToolBL result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.artifactToolBL(new ArrayList<>());
	}
}