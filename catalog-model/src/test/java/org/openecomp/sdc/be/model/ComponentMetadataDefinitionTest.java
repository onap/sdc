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

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.components.ComponentMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;


public class ComponentMetadataDefinitionTest {

	private ComponentMetadataDefinition createTestSubject() {
		return new ComponentMetadataDefinition();
	}
	
	@Test
	public void testCtor() throws Exception {
		new ComponentMetadataDefinition(new ResourceMetadataDataDefinition());
	}
	
	@Test
	public void testGetMetadataDataDefinition() throws Exception {
		ComponentMetadataDefinition testSubject;
		ComponentMetadataDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMetadataDataDefinition();
	}

	
	@Test
	public void testHashCode() throws Exception {
		ComponentMetadataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		ComponentMetadataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		obj = new Object();
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(createTestSubject());
		Assert.assertEquals(true, result);
		ComponentMetadataDefinition testSubject2 = createTestSubject();
		testSubject.componentMetadataDataDefinition = new ResourceMetadataDataDefinition();
		result = testSubject.equals(testSubject2);
		Assert.assertEquals(false, result);
		testSubject2.componentMetadataDataDefinition = new ResourceMetadataDataDefinition();
		result = testSubject.equals(testSubject2);
		Assert.assertEquals(true, result);
	}
}
