package org.openecomp.sdc.asdctool.impl.validator.config;

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

		// default test
		testSubject = createTestSubject();
		result = testSubject.basicServiceValidator();
	}

	@Test
	public void testVfArtifactValidationTask() {
		ValidationToolConfiguration testSubject;
		VfArtifactValidationTask result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.vfArtifactValidationTask();
	}

	@Test
	public void testServiceArtifactValidationTask() {
		ValidationToolConfiguration testSubject;
		ServiceArtifactValidationTask result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.serviceArtifactValidationTask();
	}

	@Test
	public void testModuleJsonTask() {
		ValidationToolConfiguration testSubject;
		ModuleJsonTask result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.moduleJsonTask();
	}

	@Test
	public void testValidationToolBL() {
		ValidationToolConfiguration testSubject;
		ValidationToolBL result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validationToolBL();
	}

	@Test
	public void testBasicVfValidator() {
		ValidationToolConfiguration testSubject;
		VfValidatorExecuter result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.basicVfValidator();
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

		// default test
		testSubject = createTestSubject();
		result = testSubject.artifactValidationUtils();
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
		result = testSubject.janusGraphDao(janusGraphClient);
	}

	@Test
	public void testCategoryOperation() {
		ValidationToolConfiguration testSubject;
		CategoryOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.categoryOperation();
	}

	@Test
	public void testArtifactsOperation() {
		ValidationToolConfiguration testSubject;
		ArtifactsOperations result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.artifactsOperation();
	}

	@Test
	public void testToscaDataOperation() {
		ValidationToolConfiguration testSubject;
		ToscaDataOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toscaDataOperation();
	}

	@Test
	public void testToscaElementLifecycleOperation() {
		ValidationToolConfiguration testSubject;
		ToscaElementLifecycleOperation result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toscaElementLifecycleOperation();
	}

	@Test
	public void testNodeToscaArtifactsValidatorValidator() throws Exception {
		ValidationToolConfiguration testSubject;
		NodeToscaArtifactsValidatorExecuter result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.NodeToscaArtifactsValidatorValidator();
	}

	@Test
	public void testServiceToscaArtifactsValidator() throws Exception {
		ValidationToolConfiguration testSubject;
		ServiceToscaArtifactsValidatorExecutor result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.ServiceToscaArtifactsValidator();
	}

	@Test
	public void testVFToscaArtifactValidator() throws Exception {
		ValidationToolConfiguration testSubject;
		VFToscaArtifactValidatorExecutor result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.VFToscaArtifactValidator();
	}

	@Test
	public void testArtifactToolBL() throws Exception {
		ValidationToolConfiguration testSubject;
		ArtifactToolBL result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.artifactToolBL();
	}
}