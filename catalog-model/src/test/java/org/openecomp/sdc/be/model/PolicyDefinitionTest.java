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
import org.openecomp.sdc.be.datatypes.elements.PolicyDataDefinition;

import java.util.HashMap;
import java.util.LinkedList;

public class PolicyDefinitionTest {

	private PolicyDefinition createTestSubject() {
		return new PolicyDefinition();
	}

	@Test
	public void testCtor() throws Exception {
		new PolicyDefinition(new HashMap<>());
		new PolicyDefinition(new PolicyDataDefinition());
		PolicyTypeDefinition policyType = new PolicyTypeDefinition();
		policyType.setProperties(new LinkedList<>());
		new PolicyDefinition(policyType);
	}
	
	@Test
	public void testGetNormalizedName() throws Exception {
		PolicyDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNormalizedName();
	}
}
