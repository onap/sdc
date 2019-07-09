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
import org.openecomp.sdc.be.datatypes.elements.DataTypeDataDefinition;

import java.util.List;


public class DataTypeDefinitionTest {

	private DataTypeDefinition createTestSubject() {
		return new DataTypeDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new DataTypeDefinition(new DataTypeDefinition());
		new DataTypeDefinition(new DataTypeDataDefinition());
	}
	
	@Test
	public void testGetConstraints() throws Exception {
		DataTypeDefinition testSubject;
		List<PropertyConstraint> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConstraints();
	}

	
	@Test
	public void testSetConstraints() throws Exception {
		DataTypeDefinition testSubject;
		List<PropertyConstraint> constraints = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setConstraints(constraints);
	}

	
	@Test
	public void testGetDerivedFrom() throws Exception {
		DataTypeDefinition testSubject;
		DataTypeDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerivedFrom();
	}

	
	@Test
	public void testSetDerivedFrom() throws Exception {
		DataTypeDefinition testSubject;
		DataTypeDefinition derivedFrom = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDerivedFrom(derivedFrom);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		DataTypeDefinition testSubject;
		List<PropertyDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		DataTypeDefinition testSubject;
		List<PropertyDefinition> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	
	@Test
	public void testToString() throws Exception {
		DataTypeDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
