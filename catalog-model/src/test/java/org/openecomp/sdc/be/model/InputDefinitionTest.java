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
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;

import java.util.List;


public class InputDefinitionTest {

	private InputDefinition createTestSubject() {
		return new InputDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new InputDefinition(new PropertyDefinition());
		new InputDefinition(new PropertyDataDefinition());
	}
	
	@Test
	public void testGetInputs() throws Exception {
		InputDefinition testSubject;
		List<ComponentInstanceInput> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputs();
	}

	
	@Test
	public void testSetInputs() throws Exception {
		InputDefinition testSubject;
		List<ComponentInstanceInput> inputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputs(inputs);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		InputDefinition testSubject;
		List<ComponentInstanceProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		InputDefinition testSubject;
		List<ComponentInstanceProperty> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}
