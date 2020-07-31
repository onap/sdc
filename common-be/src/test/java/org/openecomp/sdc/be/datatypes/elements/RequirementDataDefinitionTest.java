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

import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;


public class RequirementDataDefinitionTest {

	private RequirementDataDefinition createTestSubject() {
		return new RequirementDataDefinition();
	}

	@Test
	public void testCopyConstructor() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new RequirementDataDefinition(testSubject);
	}
	
	@Test
	public void testGetUniqueId() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		RequirementDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetName() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		RequirementDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetParentName() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParentName();
	}

	
	@Test
	public void testSetParentName() throws Exception {
		RequirementDataDefinition testSubject;
		String parentName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setParentName(parentName);
	}

	
	@Test
	public void testGetCapability() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapability();
	}

	
	@Test
	public void testSetCapability() throws Exception {
		RequirementDataDefinition testSubject;
		String capability = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCapability(capability);
	}

	
	@Test
	public void testGetNode() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNode();
	}

	
	@Test
	public void testSetNode() throws Exception {
		RequirementDataDefinition testSubject;
		String node = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNode(node);
	}

	
	@Test
	public void testGetRelationship() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelationship();
	}

	
	@Test
	public void testSetRelationship() throws Exception {
		RequirementDataDefinition testSubject;
		String relationship = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRelationship(relationship);
	}

	
	@Test
	public void testGetOwnerId() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOwnerId();
	}

	
	@Test
	public void testSetOwnerId() throws Exception {
		RequirementDataDefinition testSubject;
		String ownerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOwnerId(ownerId);
	}

	
	@Test
	public void testGetOwnerName() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOwnerName();
	}

	
	@Test
	public void testSetOwnerName() throws Exception {
		RequirementDataDefinition testSubject;
		String ownerName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOwnerName(ownerName);
	}

	
	@Test
	public void testGetMinOccurrences() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMinOccurrences();
	}

	
	@Test
	public void testSetMinOccurrences() throws Exception {
		RequirementDataDefinition testSubject;
		String minOccurrences = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setMinOccurrences(minOccurrences);
	}

	
	@Test
	public void testGetLeftOccurrences() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLeftOccurrences();
	}

	
	@Test
	public void testSetLeftOccurrences() throws Exception {
		RequirementDataDefinition testSubject;
		String leftOccurrences = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLeftOccurrences(leftOccurrences);
	}

	
	@Test
	public void testGetMaxOccurrences() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMaxOccurrences();
	}

	
	@Test
	public void testSetMaxOccurrences() throws Exception {
		RequirementDataDefinition testSubject;
		String maxOccurrences = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setMaxOccurrences(maxOccurrences);
	}

	
	@Test
	public void testSetPath() throws Exception {
		RequirementDataDefinition testSubject;
		List<String> path = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPath(path);
	}

	
	@Test
	public void testGetPath() throws Exception {
		RequirementDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPath();
	}

	
	@Test
	public void testSetSource() throws Exception {
		RequirementDataDefinition testSubject;
		String source = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSource(source);
	}

	
	@Test
	public void testGetSource() throws Exception {
		RequirementDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSource();
	}

	
	@Test
	public void testAddToPath() throws Exception {
		RequirementDataDefinition testSubject;
		String elementInPath = "";

		// default test
		testSubject = createTestSubject();
		testSubject.addToPath(elementInPath);
	}

}
