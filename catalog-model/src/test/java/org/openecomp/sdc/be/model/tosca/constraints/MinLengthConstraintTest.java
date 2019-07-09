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

import org.junit.Test;


public class MinLengthConstraintTest {

	private MinLengthConstraint createTestSubject() {
		return new MinLengthConstraint(null);
	}

	


	
	@Test
	public void testGetMinLength() throws Exception {
		MinLengthConstraint testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMinLength();
	}

	
	@Test
	public void testSetMinLength() throws Exception {
		MinLengthConstraint testSubject;
		Integer minLength = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setMinLength(minLength);
	}
}
