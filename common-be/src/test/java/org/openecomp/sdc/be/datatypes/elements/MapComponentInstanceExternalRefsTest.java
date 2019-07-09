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

package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Test;

import java.util.List;
import java.util.Map;

public class MapComponentInstanceExternalRefsTest {

	private MapComponentInstanceExternalRefs createTestSubject() {
		return new MapComponentInstanceExternalRefs();
	}

	@Test
	public void testGetComponentInstanceExternalRefs() throws Exception {
		MapComponentInstanceExternalRefs testSubject;
		Map<String, List<String>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentInstanceExternalRefs();
	}

	@Test
	public void testGetExternalRefsByObjectType() throws Exception {
		MapComponentInstanceExternalRefs testSubject;
		String objectType = "";
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getExternalRefsByObjectType(objectType);
	}

	@Test
	public void testSetComponentInstanceExternalRefs() throws Exception {
		MapComponentInstanceExternalRefs testSubject;
		Map<String, List<String>> componentInstanceExternalRefs = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentInstanceExternalRefs(componentInstanceExternalRefs);
	}

	@Test
	public void testAddExternalRef() throws Exception {
		MapComponentInstanceExternalRefs testSubject;
		String objectType = "";
		String ref = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addExternalRef(objectType, ref);
	}

	@Test
	public void testDeleteExternalRef() throws Exception {
		MapComponentInstanceExternalRefs testSubject;
		String objectType = "";
		String ref = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deleteExternalRef(objectType, ref);
	}

	@Test
	public void testReplaceExternalRef() throws Exception {
		MapComponentInstanceExternalRefs testSubject;
		String objectType = "";
		String oldRef = "";
		String newRef = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.replaceExternalRef(objectType, oldRef, newRef);
	}
}
