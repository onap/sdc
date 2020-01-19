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


public class ResourceInstanceMetadataTest {

	private ResourceInstanceMetadata createTestSubject() {
		return new ResourceInstanceMetadata();
	}

	
	@Test
	public void testGetResourceInstanceName() throws Exception {
		ResourceInstanceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceInstanceName();
	}

	
	@Test
	public void testSetResourceInstanceName() throws Exception {
		ResourceInstanceMetadata testSubject;
		String resourceInstanceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceInstanceName(resourceInstanceName);
	}

	
	@Test
	public void testGetResourceName() throws Exception {
		ResourceInstanceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceName();
	}

	
	@Test
	public void testSetResourceName() throws Exception {
		ResourceInstanceMetadata testSubject;
		String resourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceName(resourceName);
	}

	
	@Test
	public void testGetResourceInvariantUUID() throws Exception {
		ResourceInstanceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceInvariantUUID();
	}

	
	@Test
	public void testSetResourceInvariantUUID() throws Exception {
		ResourceInstanceMetadata testSubject;
		String resourceInvariantUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceInvariantUUID(resourceInvariantUUID);
	}

	
	@Test
	public void testGetResourceVersion() throws Exception {
		ResourceInstanceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceVersion();
	}

	
	@Test
	public void testSetResourceVersion() throws Exception {
		ResourceInstanceMetadata testSubject;
		String resourceVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceVersion(resourceVersion);
	}

	
	@Test
	public void testGetResoucreType() throws Exception {
		ResourceInstanceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResoucreType();
	}

	
	@Test
	public void testSetResoucreType() throws Exception {
		ResourceInstanceMetadata testSubject;
		String resoucreType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResoucreType(resoucreType);
	}

	
	@Test
	public void testGetResourceUUID() throws Exception {
		ResourceInstanceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceUUID();
	}

	
	@Test
	public void testSetResourceUUID() throws Exception {
		ResourceInstanceMetadata testSubject;
		String resourceUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceUUID(resourceUUID);
	}

	
	@Test
	public void testGetArtifacts() throws Exception {
		ResourceInstanceMetadata testSubject;
		List<ArtifactMetadata> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	
	@Test
	public void testSetArtifacts() throws Exception {
		ResourceInstanceMetadata testSubject;
		List<ArtifactMetadata> artifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifacts);
	}
}
