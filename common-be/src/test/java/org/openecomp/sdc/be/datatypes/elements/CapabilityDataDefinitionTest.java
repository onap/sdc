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

import java.util.LinkedList;
import java.util.List;


public class CapabilityDataDefinitionTest {

	private CapabilityDataDefinition createTestSubject() {
		return new CapabilityDataDefinition();
	}

	@Test
	public void testCopyConstructor() throws Exception {
		CapabilityDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		new CapabilityDataDefinition(testSubject);
		testSubject.setValidSourceTypes(new LinkedList<>());
		testSubject.setCapabilitySources(new LinkedList<>());
		testSubject.setPath(new LinkedList<>());
		new CapabilityDataDefinition(testSubject);
	}
	
	@Test
	public void testGetOwnerId() throws Exception {
		CapabilityDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOwnerId();
	}

	
	@Test
	public void testSetOwnerId() throws Exception {
		CapabilityDataDefinition testSubject;
		String ownerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOwnerId(ownerId);
	}

	
	@Test
	public void testGetOwnerName() throws Exception {
		CapabilityDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getOwnerName();
	}

	
	@Test
	public void testSetOwnerName() throws Exception {
		CapabilityDataDefinition testSubject;
		String ownerName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setOwnerName(ownerName);
	}

	
	@Test
	public void testGetMinOccurrences() throws Exception {
		CapabilityDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMinOccurrences();
	}

	
	@Test
	public void testSetMinOccurrences() throws Exception {
		CapabilityDataDefinition testSubject;
		String minOccurrences = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setMinOccurrences(minOccurrences);
	}

	
	@Test
	public void testGetMaxOccurrences() throws Exception {
		CapabilityDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMaxOccurrences();
	}

	
	@Test
	public void testSetMaxOccurrences() throws Exception {
		CapabilityDataDefinition testSubject;
		String maxOccurrences = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setMaxOccurrences(maxOccurrences);
	}

	
	@Test
	public void testGetLeftOccurrences() throws Exception {
		CapabilityDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLeftOccurrences();
	}

	
	@Test
	public void testSetLeftOccurrences() throws Exception {
		CapabilityDataDefinition testSubject;
		String leftOccurrences = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLeftOccurrences(leftOccurrences);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		CapabilityDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		CapabilityDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		CapabilityDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		CapabilityDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetName() throws Exception {
		CapabilityDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		CapabilityDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetParentName() throws Exception {
		CapabilityDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getParentName();
	}

	
	@Test
	public void testSetParentName() throws Exception {
		CapabilityDataDefinition testSubject;
		String parentName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setParentName(parentName);
	}

	
	@Test
	public void testGetType() throws Exception {
		CapabilityDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		CapabilityDataDefinition testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetValidSourceTypes() throws Exception {
		CapabilityDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValidSourceTypes();
	}

	
	@Test
	public void testSetValidSourceTypes() throws Exception {
		CapabilityDataDefinition testSubject;
		List<String> validSourceTypes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValidSourceTypes(validSourceTypes);
	}

	
	@Test
	public void testGetCapabilitySources() throws Exception {
		CapabilityDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCapabilitySources();
	}

	
	@Test
	public void testSetCapabilitySources() throws Exception {
		CapabilityDataDefinition testSubject;
		List<String> capabilitySources = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCapabilitySources(capabilitySources);
	}

	
	@Test
	public void testSetPath() throws Exception {
		CapabilityDataDefinition testSubject;
		List<String> path = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPath(path);
	}

	
	@Test
	public void testGetPath() throws Exception {
		CapabilityDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPath();
	}

	
	@Test
	public void testSetSource() throws Exception {
		CapabilityDataDefinition testSubject;
		String source = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSource(source);
	}

	
	@Test
	public void testGetSource() throws Exception {
		CapabilityDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSource();
	}

	
	@Test
	public void testAddToPath() throws Exception {
		CapabilityDataDefinition testSubject;
		String elementInPath = "";

		// default test
		testSubject = createTestSubject();
		testSubject.addToPath(elementInPath);
	}

	
	@Test
	public void testHashCode() throws Exception {
		CapabilityDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		CapabilityDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);
	}

	
	@Test
	public void testToString() throws Exception {
		CapabilityDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
