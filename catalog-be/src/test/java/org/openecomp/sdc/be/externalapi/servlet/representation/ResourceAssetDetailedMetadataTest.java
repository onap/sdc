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

import java.util.List;

public class ResourceAssetDetailedMetadataTest {

	private ResourceAssetDetailedMetadata createTestSubject() {
		return new ResourceAssetDetailedMetadata();
	}

	
	@Test
	public void testGetLastUpdaterFullName() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdaterFullName();
	}

	
	@Test
	public void testSetLastUpdaterFullName() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		String lastUpdaterFullName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdaterFullName(lastUpdaterFullName);
	}

	
	@Test
	public void testGetToscaResourceName() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaResourceName();
	}

	
	@Test
	public void testSetToscaResourceName() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		String toscaResourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaResourceName(toscaResourceName);
	}

	
	@Test
	public void testGetResources() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		List<ResourceInstanceMetadata> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResources();
	}

	
	@Test
	public void testSetResources() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		List<ResourceInstanceMetadata> resources = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResources(resources);
	}

	
	@Test
	public void testGetArtifacts() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		List<ArtifactMetadata> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	
	@Test
	public void testSetArtifacts() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		List<ArtifactMetadata> artifactMetaList = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifactMetaList);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		ResourceAssetDetailedMetadata testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}
}
