/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.tosca;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.tosca.CsarUtils.NonMetaArtifactInfo;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;

import fj.data.Either;

public class CsarUtilsTest {
	@Before
	public void setup() {
		ExternalConfiguration.setAppName("catalog-be");

		// Init Configuration
		String appConfigDir = "src/test/resources/config/catalog-be";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(),
				appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
	}

	@Test
	public void testValidateNonMetaArtifactHappyScenario() {
		String artifactPath = "Artifacts/Deployment/YANG_XML/myYang.xml";
		byte[] payloadData = "some payload data".getBytes();
		Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
		Either<NonMetaArtifactInfo, Boolean> eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath,
				payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isLeft());
		assertTrue(collectedWarningMessages.isEmpty());

		artifactPath = "Artifacts/Informational/OTHER/someArtifact.xml";
		eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath, payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isLeft());
		assertTrue(collectedWarningMessages.isEmpty());
	}

	@Test
	public void testValidateNonMetaArtifactScenarioWithWarnnings() {
		String artifactPath = "Artifacts/Deployment/Buga/myYang.xml";
		byte[] payloadData = "some payload data".getBytes();
		Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
		Either<NonMetaArtifactInfo, Boolean> eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath,
				payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isLeft());

		artifactPath = "Artifacts/Informational/Buga2/someArtifact.xml";
		eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath, payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isLeft());

		assertTrue(collectedWarningMessages.size() == 1);
		assertTrue(collectedWarningMessages.values().iterator().next().size() == 2);
	}

	@Test
	public void testValidateNonMetaArtifactUnhappyScenario() {
		String artifactPath = "Artifacts/Buga/YANG_XML/myYang.xml";
		byte[] payloadData = "some payload data".getBytes();
		Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();
		Either<NonMetaArtifactInfo, Boolean> eitherNonMetaArtifact = CsarUtils.validateNonMetaArtifact(artifactPath,
				payloadData, collectedWarningMessages);
		assertTrue(eitherNonMetaArtifact.isRight());
		assertTrue(!collectedWarningMessages.isEmpty());
	}

	private CsarUtils createTestSubject() {
		return new CsarUtils();
	}

	
	@Test
	public void testExtractVfcsArtifactsFromCsar() throws Exception {
		Map<String, byte[]> csar = null;
		Map<String, List<ArtifactDefinition>> result;

		// test 1
		csar = null;
		result = CsarUtils.extractVfcsArtifactsFromCsar(csar);
		Assert.assertEquals(new HashMap<String, List<ArtifactDefinition>>() , result);
	}

	
	@Test
	public void testHandleWarningMessages() throws Exception {
		Map<String, Set<List<String>>> collectedWarningMessages = new HashMap<>();

		// default test
		CsarUtils.handleWarningMessages(collectedWarningMessages);
	}

	
	@Test
	public void testValidateNonMetaArtifact() throws Exception {
		String artifactPath = "";
		byte[] payloadData = new byte[] { ' ' };
		Map<String, Set<List<String>>> collectedWarningMessages = null;
		Either<NonMetaArtifactInfo, Boolean> result;

		// default test
		result = CsarUtils.validateNonMetaArtifact(artifactPath, payloadData, collectedWarningMessages);
	}

}
