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

public class LifeCycleTransitionEnumTest {

	private LifeCycleTransitionEnum createTestSubject() {
		return LifeCycleTransitionEnum.CERTIFY;
	}

	@Test
	public void testGetDisplayName() throws Exception {
		LifeCycleTransitionEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDisplayName();
	}

	@Test
	public void testGetFromDisplayName() throws Exception {
		String name = LifeCycleTransitionEnum.CHECKIN.getDisplayName();
		LifeCycleTransitionEnum result;

		// default test
		for (LifeCycleTransitionEnum iterable_element : LifeCycleTransitionEnum.values()) {
			result = LifeCycleTransitionEnum.getFromDisplayName(iterable_element.getDisplayName());
		}
	}

	@Test
	public void testGetFromDisplayNameException() throws Exception {
		String name = LifeCycleTransitionEnum.CHECKIN.getDisplayName();
		LifeCycleTransitionEnum result;

		// default test
		try {
			result = LifeCycleTransitionEnum.getFromDisplayName("mock");
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}

	@Test
	public void testValuesAsString() throws Exception {
		String result;

		// default test
		result = LifeCycleTransitionEnum.valuesAsString();
	}
}
