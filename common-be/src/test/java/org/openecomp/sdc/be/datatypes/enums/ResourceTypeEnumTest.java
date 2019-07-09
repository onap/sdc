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

package org.openecomp.sdc.be.datatypes.enums;

import org.junit.Test;

public class ResourceTypeEnumTest {

	private ResourceTypeEnum createTestSubject() {
		return ResourceTypeEnum.ABSTRACT;
	}

	@Test
	public void testGetValue() throws Exception {
		ResourceTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testIsAtomicType() throws Exception {
		ResourceTypeEnum testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isAtomicType();
	}

	@Test
	public void testGetType() throws Exception {
		String type = "";
		ResourceTypeEnum result;

		// default test
		result = ResourceTypeEnum.getType(type);
		result = ResourceTypeEnum.getType(ResourceTypeEnum.ABSTRACT.name());
	}

	@Test
	public void testGetTypeByName() throws Exception {
		String type = "";
		ResourceTypeEnum result;

		// default test
		result = ResourceTypeEnum.getType(type);
		result = ResourceTypeEnum.getTypeByName(ResourceTypeEnum.ABSTRACT.name());
	}

	@Test
	public void testGetTypeIgnoreCase() throws Exception {
		String type = "";
		ResourceTypeEnum result;

		// default test
		result = ResourceTypeEnum.getTypeIgnoreCase(type);
		result = ResourceTypeEnum.getTypeIgnoreCase(ResourceTypeEnum.ABSTRACT.name());
	}

	@Test
	public void testContainsName() throws Exception {
		String type = "";
		boolean result;

		// default test
		result = ResourceTypeEnum.containsName(type);
		result = ResourceTypeEnum.containsName(ResourceTypeEnum.ABSTRACT.name());
	}

	@Test
	public void testContainsIgnoreCase() throws Exception {
		String type = "";
		boolean result;

		// default test
		result = ResourceTypeEnum.containsIgnoreCase(type);
		result = ResourceTypeEnum.containsIgnoreCase(ResourceTypeEnum.ABSTRACT.name());
	}
}
