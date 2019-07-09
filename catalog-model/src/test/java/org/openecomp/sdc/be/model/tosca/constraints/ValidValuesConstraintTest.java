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

package org.openecomp.sdc.be.model.tosca.constraints;

import java.util.List;

import org.junit.Test;


public class ValidValuesConstraintTest {

	private ValidValuesConstraint createTestSubject() {
		return new ValidValuesConstraint(null);
	}

	

	


	
	@Test
	public void testGetValidValues() throws Exception {
		ValidValuesConstraint testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValidValues();
	}

	
	@Test
	public void testSetValidValues() throws Exception {
		ValidValuesConstraint testSubject;
		List<String> validValues = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setValidValues(validValues);
	}
}
