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

package org.openecomp.sdc.asdctool.impl.validator.tasks.artifacts;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.elements.ArtifactDataDefinition;

import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.TopologyTemplateOperation;

public class ArtifactValidationUtilsTest {

	private ArtifactValidationUtils createTestSubject() {
		ArtifactCassandraDao artifactCassandraDao = mock(ArtifactCassandraDao.class);
		TopologyTemplateOperation topologyTemplateOperation = mock(TopologyTemplateOperation.class);
		return new ArtifactValidationUtils(artifactCassandraDao, topologyTemplateOperation);
	}

	@Test(expected=NullPointerException.class)
	public void testValidateArtifactsAreInCassandra() throws Exception {
		ArtifactValidationUtils testSubject;
		GraphVertex vertex = null;
		String taskName = "";
		List<ArtifactDataDefinition> artifacts = null;
		ArtifactsVertexResult result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateArtifactsAreInCassandra(vertex, taskName, artifacts);
	}

	@Test(expected=NullPointerException.class)
	public void testIsArtifcatInCassandra() throws Exception {
		ArtifactValidationUtils testSubject;
		String uniueId = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isArtifcatInCassandra(uniueId);
	}

	@Test
	public void testAddRelevantArtifacts() throws Exception {
		ArtifactValidationUtils testSubject;
		Map<String, ArtifactDataDefinition> artifactsMap = null;
		List<ArtifactDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addRelevantArtifacts(artifactsMap);
	}

	@Test(expected=NullPointerException.class)
	public void testValidateTopologyTemplateArtifacts() throws Exception {
		ArtifactValidationUtils testSubject;
		GraphVertex vertex = null;
		String taskName = "";
		ArtifactsVertexResult result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateTopologyTemplateArtifacts(vertex, taskName);
	}
}
