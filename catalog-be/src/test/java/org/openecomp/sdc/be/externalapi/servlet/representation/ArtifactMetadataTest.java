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

package org.openecomp.sdc.be.externalapi.servlet.representation;

import org.junit.Test;


public class ArtifactMetadataTest {

	private ArtifactMetadata createTestSubject() {
		return new ArtifactMetadata();
	}

	
	@Test
	public void testGetArtifactName() throws Exception {
		ArtifactMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactName();
	}

	
	@Test
	public void testSetArtifactName() throws Exception {
		ArtifactMetadata testSubject;
		String artifactName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactName(artifactName);
	}

	
	@Test
	public void testGetArtifactType() throws Exception {
		ArtifactMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactType();
	}

	
	@Test
	public void testSetArtifactType() throws Exception {
		ArtifactMetadata testSubject;
		String artifactType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactType(artifactType);
	}

	
	@Test
	public void testGetArtifactURL() throws Exception {
		ArtifactMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactURL();
	}

	
	@Test
	public void testSetArtifactURL() throws Exception {
		ArtifactMetadata testSubject;
		String artifactURL = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactURL(artifactURL);
	}

	
	@Test
	public void testGetArtifactDescription() throws Exception {
		ArtifactMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactDescription();
	}

	
	@Test
	public void testSetArtifactDescription() throws Exception {
		ArtifactMetadata testSubject;
		String artifactDescription = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactDescription(artifactDescription);
	}

	
	@Test
	public void testGetArtifactTimeout() throws Exception {
		ArtifactMetadata testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactTimeout();
	}

	
	@Test
	public void testSetArtifactTimeout() throws Exception {
		ArtifactMetadata testSubject;
		Integer artifactTimeout = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactTimeout(artifactTimeout);
	}

	
	@Test
	public void testGetArtifactChecksum() throws Exception {
		ArtifactMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactChecksum();
	}

	
	@Test
	public void testSetArtifactChecksum() throws Exception {
		ArtifactMetadata testSubject;
		String artifactChecksum = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactChecksum(artifactChecksum);
	}

	
	@Test
	public void testGetArtifactUUID() throws Exception {
		ArtifactMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactUUID();
	}

	
	@Test
	public void testSetArtifactUUID() throws Exception {
		ArtifactMetadata testSubject;
		String artifactUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactUUID(artifactUUID);
	}

	
	@Test
	public void testGetArtifactVersion() throws Exception {
		ArtifactMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactVersion();
	}

	
	@Test
	public void testSetArtifactVersion() throws Exception {
		ArtifactMetadata testSubject;
		String artifactVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactVersion(artifactVersion);
	}

	
	@Test
	public void testGetGeneratedFromUUID() throws Exception {
		ArtifactMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGeneratedFromUUID();
	}

	
	@Test
	public void testSetGeneratedFromUUID() throws Exception {
		ArtifactMetadata testSubject;
		String generatedFromUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setGeneratedFromUUID(generatedFromUUID);
	}

	
	@Test
	public void testGetArtifactLabel() throws Exception {
		ArtifactMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactLabel();
	}

	
	@Test
	public void testSetArtifactLabel() throws Exception {
		ArtifactMetadata testSubject;
		String artifactLabel = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactLabel(artifactLabel);
	}

	
	@Test
	public void testGetArtifactGroupType() throws Exception {
		ArtifactMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactGroupType();
	}

	
	@Test
	public void testSetArtifactGroupType() throws Exception {
		ArtifactMetadata testSubject;
		String artifactGroupType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactGroupType(artifactGroupType);
	}
}
