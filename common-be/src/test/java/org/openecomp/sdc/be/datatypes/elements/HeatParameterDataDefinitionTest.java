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


public class HeatParameterDataDefinitionTest {

	private HeatParameterDataDefinition createTestSubject() {
		return new HeatParameterDataDefinition();
	}

	@Test
	public void testCopyConstructor() throws Exception {
		HeatParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		new HeatParameterDataDefinition(testSubject);
	}
	
	@Test
	public void testGetUniqueId() throws Exception {
		HeatParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		HeatParameterDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetName() throws Exception {
		HeatParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		HeatParameterDataDefinition testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetType() throws Exception {
		HeatParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		HeatParameterDataDefinition testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		HeatParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		HeatParameterDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetCurrentValue() throws Exception {
		HeatParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCurrentValue();
	}

	
	@Test
	public void testSetCurrentValue() throws Exception {
		HeatParameterDataDefinition testSubject;
		String currentValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCurrentValue(currentValue);
	}

	
	@Test
	public void testGetDefaultValue() throws Exception {
		HeatParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDefaultValue();
	}

	
	@Test
	public void testSetDefaultValue() throws Exception {
		HeatParameterDataDefinition testSubject;
		String defaultValue = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDefaultValue(defaultValue);
	}

	
	@Test
	public void testToString() throws Exception {
		HeatParameterDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testHashCode() throws Exception {
		HeatParameterDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		HeatParameterDataDefinition testSubject;
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
}
