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

package org.openecomp.sdc.be.tosca.model;

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class ToscaLifecycleOperationDefinitionTest {

	private ToscaLifecycleOperationDefinition createTestSubject() {
		return new ToscaLifecycleOperationDefinition();
	}

	@Test
	public void testGetImplementation() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getImplementation();
	}

	@Test
	public void testSetImplementation() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.setImplementation(new ToscaInterfaceOperationImplementation());
	}

	@Test
	public void testGetInputs() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		Map<String, ToscaProperty> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInputs();
	}

	@Test
	public void testSetInputs() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		Map<String, ToscaProperty> inputs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInputs(inputs);
	}

	@Test
	public void testEquals() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		Object o = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		o = null;
		result = testSubject.equals(o);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);
	}

	@Test
	public void testHashCode() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testGetDescription() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testSetDescription() throws Exception {
		ToscaLifecycleOperationDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}
}
