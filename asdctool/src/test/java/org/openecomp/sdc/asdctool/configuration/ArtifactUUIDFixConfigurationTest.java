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

package org.openecomp.sdc.asdctool.configuration;

import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.ArtifactUuidFix;
import org.openecomp.sdc.be.components.distribution.engine.ServiceDistributionArtifactsBuilder;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.tosca.CsarUtils;
import org.openecomp.sdc.be.tosca.ToscaExportHandler;

import static org.mockito.Mockito.mock;

public class ArtifactUUIDFixConfigurationTest {

	private ArtifactUUIDFixConfiguration createTestSubject() {
		return new ArtifactUUIDFixConfiguration();
	}

	private static ToscaExportHandler toscaExportHandler;
	private static CsarUtils csarUtils;

	@Test
	public void testArtifactUuidFix() throws Exception {
		ArtifactUUIDFixConfiguration testSubject;
		ArtifactUuidFix result;

		// default test
		testSubject = createTestSubject();
		JanusGraphDao janusGraphDao = mock(JanusGraphDao.class);
		ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);
		ArtifactCassandraDao artifactCassandraDao = mock(ArtifactCassandraDao.class);

		result = testSubject.artifactUuidFix(janusGraphDao, toscaOperationFacade,
			toscaExportHandler, artifactCassandraDao, csarUtils);
	}

	@Test
	public void testServiceDistributionArtifactsBuilder() throws Exception {
		ArtifactUUIDFixConfiguration testSubject;
		ServiceDistributionArtifactsBuilder result;

		// default test
		testSubject = createTestSubject();
	}

}
