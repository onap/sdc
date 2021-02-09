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

package org.openecomp.sdc.be.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.GroupInstanceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

public class GroupInstanceTest {

	private GroupInstance createTestSubject() {
		return new GroupInstance();
	}

	@Test
	public void testCtor() throws Exception {
		new GroupInstance(new GroupInstanceDataDefinition());
	}
	
	@Test
	public void testConvertToGroupInstancesProperties() throws Exception {
		GroupInstance testSubject;
		List<GroupInstanceProperty> result;

		// default test
		testSubject = createTestSubject();
		testSubject.convertToGroupInstancesProperties();
		List<PropertyDataDefinition> properties = new LinkedList<>();
		properties.add(new PropertyDataDefinition());
		testSubject.setProperties(properties);
		testSubject.convertToGroupInstancesProperties();
	}

	@Test
	public void testConvertFromGroupInstancesProperties() throws Exception {
		GroupInstance testSubject;
		List<GroupInstanceProperty> groupInstancesProperties = null;

		// test 1
		testSubject = createTestSubject();
		groupInstancesProperties = null;
		testSubject.convertFromGroupInstancesProperties(groupInstancesProperties);
		groupInstancesProperties = new LinkedList<>();
		groupInstancesProperties.add(new GroupInstanceProperty());
		testSubject.convertFromGroupInstancesProperties(groupInstancesProperties);
	}

	@Test
	public void testRemoveArtifactsDuplicates() throws Exception {
		GroupInstance testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.removeArtifactsDuplicates();
		LinkedList<String> artifacts = new LinkedList<>();
		artifacts.add("mock");
		testSubject.setArtifacts(artifacts);
		LinkedList<String> groupInstanceArtifacts = new LinkedList<>();
		groupInstanceArtifacts.add("mock");
		testSubject.setGroupInstanceArtifacts(groupInstanceArtifacts);
		testSubject.removeArtifactsDuplicates();
	}

	@Test
	public void testClearArtifactsUuid() throws Exception {
		GroupInstance testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.clearArtifactsUuid();
	}

	@Test
	public void testAlignArtifactsUuid() throws Exception {
		GroupInstance testSubject;
		Map<String, ArtifactDefinition> deploymentArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.alignArtifactsUuid(deploymentArtifacts);
		LinkedList<String> artifacts = new LinkedList<>();
		artifacts.add("mock");
		testSubject.setArtifacts(artifacts);
		testSubject.alignArtifactsUuid(deploymentArtifacts);
		deploymentArtifacts = new HashMap<>();
		deploymentArtifacts.put("mock", new ArtifactDefinition());
		testSubject.alignArtifactsUuid(deploymentArtifacts);
	}

	@Test
	public void testAddArtifactsIdToCollection() throws Exception {
		GroupInstance testSubject;
		List<String> artifactUuids = new LinkedList<>();
		ArtifactDefinition artifact = new ArtifactDefinition();

		// default test
		testSubject = createTestSubject();
		testSubject.addArtifactsIdToCollection(artifactUuids, artifact);
		artifact.setArtifactUUID("mock");
		testSubject.addArtifactsIdToCollection(artifactUuids, artifact);
	}
}
