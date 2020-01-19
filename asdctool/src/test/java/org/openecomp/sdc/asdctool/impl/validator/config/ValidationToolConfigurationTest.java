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

package org.openecomp.sdc.asdctool.impl.validator.config;

import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.validator.ArtifactToolBL;
import org.openecomp.sdc.asdctool.impl.validator.ValidationToolBL;
import org.openecomp.sdc.asdctool.impl.validator.executers.NodeToscaArtifactsValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.executers.ServiceToscaArtifactsValidatorExecutor;
import org.openecomp.sdc.asdctool.impl.validator.executers.ServiceValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.executers.VFToscaArtifactValidatorExecutor;
import org.openecomp.sdc.asdctool.impl.validator.executers.VfValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.ArtifactValidationUtils;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.ServiceArtifactValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts.VfArtifactValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.tasks.moduleJson.ModuleJsonTask;
import org.openecomp.sdc.be.dao.JanusGraphClientStrategy;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraClient;
import org.openecomp.sdc.be.dao.impl.HealingPipelineDao;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphClient;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.DerivedNodeTypeResolver;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.GroupsOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.NodeTypeOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;

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
		result = testSubject.artifactCassandraDao(mock(CassandraClient.class));
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
		HealingPipelineDao healingPipelineDao = null;
		JanusGraphDao result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.healingJanusGraphDao(healingPipelineDao, janusGraphClient);
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
