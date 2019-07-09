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

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition;

import java.util.LinkedList;
import java.util.List;


public class CapabilityDefinitionTest {

	private CapabilityDefinition createTestSubject() {
		return new CapabilityDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		CapabilityDefinition other = new CapabilityDefinition();
		new CapabilityDefinition(other);
		other.setProperties(new LinkedList<>());
		new CapabilityDefinition(other);
		new CapabilityDefinition(new CapabilityDataDefinition());
	}
	
	@Test
	public void testHashCode() throws Exception {
		CapabilityDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		CapabilityDefinition testSubject;
		Object obj = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
		result = testSubject.equals(new Object());
		result = testSubject.equals(testSubject);
		CapabilityDefinition createTestSubject = createTestSubject();
		result = testSubject.equals(createTestSubject);
		createTestSubject.setProperties(new LinkedList<>());
		result = testSubject.equals(createTestSubject);
		testSubject.setProperties(new LinkedList<>());
		result = testSubject.equals(createTestSubject);
	}

	
	@Test
	public void testToString() throws Exception {
		CapabilityDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetProperties() throws Exception {
		CapabilityDefinition testSubject;
		List<ComponentInstanceProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		CapabilityDefinition testSubject;
		List<ComponentInstanceProperty> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}
