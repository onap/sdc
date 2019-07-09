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

package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GroupDataDefinitionTest {

	private GroupDataDefinition createTestSubject() {
		return new GroupDataDefinition();
	}

	@Test
	public void testConstructors() throws Exception {
		GroupDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new GroupDataDefinition(testSubject);
		new GroupDataDefinition(new HashMap<>());
	}
	
	@Test
	public void testGetName() throws Exception {
		GroupDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		GroupDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		GroupDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		GroupDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetType() throws Exception {
		GroupDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		GroupDataDefinition testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetVersion() throws Exception {
		GroupDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	
	@Test
	public void testSetVersion() throws Exception {
		GroupDataDefinition testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	
	@Test
	public void testGetInvariantUUID() throws Exception {
		GroupDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInvariantUUID();
	}

	
	@Test
	public void testSetInvariantUUID() throws Exception {
		GroupDataDefinition testSubject;
		String invariantUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInvariantUUID(invariantUUID);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		GroupDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		GroupDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetPropertyValueCounter() throws Exception {
		GroupDataDefinition testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPropertyValueCounter();
	}

	
	@Test
	public void testSetPropertyValueCounter() throws Exception {
		GroupDataDefinition testSubject;
		Integer propertyValueCounter = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setPropertyValueCounter(propertyValueCounter);
	}

	
	@Test
	public void testGetGroupUUID() throws Exception {
		GroupDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupUUID();
	}

	
	@Test
	public void testSetGroupUUID() throws Exception {
		GroupDataDefinition testSubject;
		String groupUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupUUID(groupUUID);
	}

	
	@Test
	public void testGetMembers() throws Exception {
		GroupDataDefinition testSubject;
		Map<String, String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMembers();
	}

	
	@Test
	public void testSetMembers() throws Exception {
		GroupDataDefinition testSubject;
		Map<String, String> members = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMembers(members);
	}

	
	@Test
	public void testGetArtifacts() throws Exception {
		GroupDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	
	@Test
	public void testSetArtifacts() throws Exception {
		GroupDataDefinition testSubject;
		List<String> artifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifacts);
	}

	
	@Test
	public void testGetArtifactsUuid() throws Exception {
		GroupDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactsUuid();
	}

	
	@Test
	public void testSetArtifactsUuid() throws Exception {
		GroupDataDefinition testSubject;
		List<String> artifactsUuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactsUuid(artifactsUuid);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		GroupDataDefinition testSubject;
		List<PropertyDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		GroupDataDefinition testSubject;
		List<PropertyDataDefinition> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	
	@Test
	public void testGetTypeUid() throws Exception {
		GroupDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTypeUid();
	}

	
	@Test
	public void testSetTypeUid() throws Exception {
		GroupDataDefinition testSubject;
		String typeUid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTypeUid(typeUid);
	}

	
	@Test
	public void testToString() throws Exception {
		GroupDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
	
	@Test
	public void testContainsInstanceAsMember() throws Exception {
		GroupDataDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.containsInstanceAsMember("");
	}
}
