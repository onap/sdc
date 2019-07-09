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

package org.openecomp.sdc.be.dao.jsongraph.types;

import org.junit.Test;


public class EdgePropertyEnumTest {

	private EdgePropertyEnum createTestSubject() {
		return EdgePropertyEnum.STATE;
	}

	
	@Test
	public void testGetProperty() throws Exception {
		EdgePropertyEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperty();
	}

	
	@Test
	public void testGetByProperty() throws Exception {
		String property = "";
		EdgePropertyEnum result;

		// default test
		result = EdgePropertyEnum.getByProperty(property);
	}
}
