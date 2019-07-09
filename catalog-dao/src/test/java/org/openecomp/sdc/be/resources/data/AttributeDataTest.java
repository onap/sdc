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

package org.openecomp.sdc.be.resources.data;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.HashMap;
import java.util.Map;


public class AttributeDataTest {

	private AttributeData createTestSubject() {
		return new AttributeData();
	}

	@Test
	public void testCtor() throws Exception {
		new AttributeData(new HashMap<>());
		new AttributeData(new PropertyDataDefinition());
	}
	
	@Test
	public void testToString() throws Exception {
		AttributeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		AttributeData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetAttributeDataDefinition() throws Exception {
		AttributeData testSubject;
		PropertyDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAttributeDataDefinition();
	}

	
	@Test
	public void testSetAttributeDataDefinition() throws Exception {
		AttributeData testSubject;
		PropertyDataDefinition attributeDataDefinition = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setAttributeDataDefinition(attributeDataDefinition);
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		AttributeData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}
}
