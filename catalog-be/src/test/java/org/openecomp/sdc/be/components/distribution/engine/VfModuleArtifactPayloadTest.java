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

package org.openecomp.sdc.be.components.distribution.engine;

import org.junit.Test;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.GroupInstanceProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class VfModuleArtifactPayloadTest {

	private VfModuleArtifactPayload createTestSubject() {
		return new VfModuleArtifactPayload(new GroupDefinition());
	}

	@Test
	public void testConstructor() {
		new VfModuleArtifactPayload(new GroupInstance());
	}

	
	@Test
	public void testGetArtifacts() throws Exception {
		VfModuleArtifactPayload testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	
	@Test
	public void testSetArtifacts() throws Exception {
		VfModuleArtifactPayload testSubject;
		List<String> artifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifacts);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		VfModuleArtifactPayload testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		VfModuleArtifactPayload testSubject;
		List<GroupInstanceProperty> properties = new ArrayList<>();

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
	
	@Test
	public void testcompareByGroupName() throws Exception {
		VfModuleArtifactPayload testSubject;
		GroupDefinition groupDefinition = new GroupDefinition();
		groupDefinition.setName("module-1234.545");
		VfModuleArtifactPayload vfModuleArtifactPayload1 = new VfModuleArtifactPayload(groupDefinition);
		GroupDefinition groupDefinition2 = new GroupDefinition();
		groupDefinition.setName("module-3424.546");
		VfModuleArtifactPayload vfModuleArtifactPayload2 = new VfModuleArtifactPayload(groupDefinition);
		// default test
		testSubject = createTestSubject();
		testSubject.compareByGroupName(vfModuleArtifactPayload1, vfModuleArtifactPayload2);
		testSubject.compareByGroupName(vfModuleArtifactPayload1, vfModuleArtifactPayload1);
	}
}
