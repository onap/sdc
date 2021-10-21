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

package org.openecomp.sdc.be.components.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class LifecycleChangeInfoBaseTest {

	private LifecycleChangeInfoBase createTestSubject() {
		return new LifecycleChangeInfoBase();
	}

	@Test
	public void testUserRemarks() throws Exception {
		String remarks = "ABC123*";
		LifecycleChangeInfoBase testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		testSubject.setUserRemarks(remarks);
		result = testSubject.getUserRemarks();
		assertThat(result).isNotEmpty();
		assertThat(result).isEqualTo(remarks);
	}
}
