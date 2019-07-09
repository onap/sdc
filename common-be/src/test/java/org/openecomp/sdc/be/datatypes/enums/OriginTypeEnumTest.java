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

public class OriginTypeEnumTest {

	private OriginTypeEnum createTestSubject() {
		return OriginTypeEnum.CP;
	}

	@Test
	public void testGetValue() throws Exception {
		OriginTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getValue();
	}

	@Test
	public void testGetDisplayValue() throws Exception {
		OriginTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDisplayValue();
	}

	@Test
	public void testGetInstanceType() throws Exception {
		OriginTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInstanceType();
	}

	@Test
	public void testGetComponentType() throws Exception {
		OriginTypeEnum testSubject;
		ComponentTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentType();
	}

	@Test
	public void testFindByValue() throws Exception {
		String value = "";
		OriginTypeEnum result;

		// default test
		result = OriginTypeEnum.findByValue(value);
		result = OriginTypeEnum.findByValue(OriginTypeEnum.CP.getValue());
	}
}
