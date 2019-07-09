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
import org.openecomp.sdc.be.datatypes.elements.GroupDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.LinkedList;
import java.util.List;


public class GroupDefinitionTest {

	private GroupDefinition createTestSubject() {
		return new GroupDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new GroupDefinition(new GroupDefinition());
		new GroupDefinition(new GroupDataDefinition());
	}
	
	@Test
	public void testConvertToGroupProperties() throws Exception {
		GroupDefinition testSubject;
		List<GroupProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToGroupProperties();
		List<PropertyDataDefinition> properties = new LinkedList<>();
		properties.add(new PropertyDataDefinition());
		testSubject.setProperties(properties);
		result = testSubject.convertToGroupProperties();
	}

	
	@Test
	public void testConvertFromGroupProperties() throws Exception {
		GroupDefinition testSubject;
		List<GroupProperty> properties = null;

		// test 1
		testSubject = createTestSubject();
		testSubject.convertFromGroupProperties(properties);
		properties = new LinkedList<>();
		properties.add(new GroupProperty());
		testSubject.convertFromGroupProperties(properties);
	}

	
	@Test
	public void testIsSamePrefix() throws Exception {
		GroupDefinition testSubject;
		String resourceName = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isSamePrefix(resourceName);
		testSubject.setName("mock");
		result = testSubject.isSamePrefix("mock");
	}
}
