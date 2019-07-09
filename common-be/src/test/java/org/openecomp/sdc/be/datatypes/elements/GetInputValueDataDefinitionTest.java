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

import java.util.HashMap;


public class GetInputValueDataDefinitionTest {

	private GetInputValueDataDefinition createTestSubject() {
		return new GetInputValueDataDefinition();
	}

	@Test
	public void testConstructors() throws Exception {
		GetInputValueDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		new GetInputValueDataDefinition(testSubject);
		new GetInputValueDataDefinition(new HashMap<>());
	}
	
	@Test
	public void testGetPropName() throws Exception {
		GetInputValueDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPropName();
	}

	
	@Test
	public void testSetPropName() throws Exception {
		GetInputValueDataDefinition testSubject;
		String propName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setPropName(propName);
	}

	
	@Test
	public void testGetInputName() throws Exception {
		GetInputValueDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputName();
	}

	
	@Test
	public void testSetInputName() throws Exception {
		GetInputValueDataDefinition testSubject;
		String inputName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInputName(inputName);
	}

	
	@Test
	public void testGetIndexValue() throws Exception {
		GetInputValueDataDefinition testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIndexValue();
	}

	
	@Test
	public void testSetIndexValue() throws Exception {
		GetInputValueDataDefinition testSubject;
		Integer indexValue = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setIndexValue(indexValue);
	}

	
	@Test
	public void testGetGetInputIndex() throws Exception {
		GetInputValueDataDefinition testSubject;
		GetInputValueDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGetInputIndex();
	}

	
	@Test
	public void testSetGetInputIndex() throws Exception {
		GetInputValueDataDefinition testSubject;
		GetInputValueDataDefinition getInputIndex = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGetInputIndex(getInputIndex);
	}

	
	@Test
	public void testIsList() throws Exception {
		GetInputValueDataDefinition testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isList();
	}

	
	@Test
	public void testSetList() throws Exception {
		GetInputValueDataDefinition testSubject;
		boolean isList = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setList(isList);
	}

	
	@Test
	public void testGetInputId() throws Exception {
		GetInputValueDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputId();
	}

	
	@Test
	public void testSetInputId() throws Exception {
		GetInputValueDataDefinition testSubject;
		String inputId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInputId(inputId);
	}

	
	@Test
	public void testToString() throws Exception {
		GetInputValueDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testHashCode() throws Exception {
		GetInputValueDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		GetInputValueDataDefinition testSubject;
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
