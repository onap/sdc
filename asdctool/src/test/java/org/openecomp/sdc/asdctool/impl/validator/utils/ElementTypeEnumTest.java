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

package org.openecomp.sdc.asdctool.impl.validator.utils;

import org.junit.Test;

import java.util.List;


public class ElementTypeEnumTest {

	private ElementTypeEnum createTestSubject() {
		return  ElementTypeEnum.VF;
		}

	
	@Test
	public void testGetByType() {
		String elementType = "";
		ElementTypeEnum result;

		// default test
		result = ElementTypeEnum.getByType(elementType);
		result = ElementTypeEnum.getByType(ElementTypeEnum.VF.getElementType());
	}

	
	@Test
	public void testGetAllTypes() {
		List<String> result;

		// default test
		result = ElementTypeEnum.getAllTypes();
	}

	
	@Test
	public void testGetElementType() {
		ElementTypeEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getElementType();
	}

	
	@Test
	public void testSetElementType() {
		ElementTypeEnum testSubject;
		String elementType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setElementType(elementType);
	}

	
	@Test
	public void testGetClazz() {
		ElementTypeEnum testSubject;
		Class result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getClazz();
	}

	
	@Test
	public void testSetClazz() {
		ElementTypeEnum testSubject;
		Class clazz = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setClazz(clazz);
	}
}
