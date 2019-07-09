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

package org.openecomp.sdc.be.resources.data;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;


public class UserFunctionalMenuDataTest {

	private UserFunctionalMenuData createTestSubject() {
		return new UserFunctionalMenuData();
	}

	@Test
	public void testCtor() throws Exception {
		new UserFunctionalMenuData(new HashMap<>());
		new UserFunctionalMenuData("mock", "mock");
	}
	
	@Test
	public void testGetFunctionalMenu() throws Exception {
		UserFunctionalMenuData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFunctionalMenu();
	}

	
	@Test
	public void testSetFunctionalMenu() throws Exception {
		UserFunctionalMenuData testSubject;
		String functionalMenu = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFunctionalMenu(functionalMenu);
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		UserFunctionalMenuData testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testToString() throws Exception {
		UserFunctionalMenuData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testToJson() throws Exception {
		UserFunctionalMenuData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toJson();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		UserFunctionalMenuData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testGetUniqueIdKey() throws Exception {
		UserFunctionalMenuData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIdKey();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		UserFunctionalMenuData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}
}
