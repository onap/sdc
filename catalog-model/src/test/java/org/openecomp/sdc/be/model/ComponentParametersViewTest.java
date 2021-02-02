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
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentFieldsEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;

import java.util.LinkedList;

import static org.junit.Assert.assertSame;

public class ComponentParametersViewTest {

	private ComponentParametersView createTestSubject() {
		return new ComponentParametersView();
	}

	@Test
	public void testCtor() throws Exception {
		new ComponentParametersView(true);
		
		LinkedList<String> linkedList = new LinkedList<>();
		for (ComponentFieldsEnum iterable_element : ComponentFieldsEnum.values()) {
			linkedList.add(iterable_element.getValue());
		}
		new ComponentParametersView(linkedList);
	}
	
	@Test
	public void testFilter() throws Exception {
		ComponentParametersView testSubject;
		Component component = null;
		ComponentTypeEnum componentType = ComponentTypeEnum.RESOURCE;
		Component result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.filter(component, componentType);
		testSubject.disableAll();
		result = testSubject.filter(new Resource(), componentType);
	}

	
	@Test
	public void testDisableAll() throws Exception {
		ComponentParametersView testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.disableAll();
	}

	@Test
	public void testDetectParseFlag() throws Exception {
		ComponentParametersView testSubject;
		JsonParseFlagEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.detectParseFlag();
	}
}
