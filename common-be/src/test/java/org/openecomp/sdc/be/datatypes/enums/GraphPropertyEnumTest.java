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

import java.util.List;

public class GraphPropertyEnumTest {

	private GraphPropertyEnum createTestSubject() {
		return GraphPropertyEnum.COMPONENT_TYPE;
	}

	@Test
	public void testGetByProperty() throws Exception {
		String property = "";
		GraphPropertyEnum result;

		// default test
		result = GraphPropertyEnum.getByProperty(property);
		result = GraphPropertyEnum.getByProperty(GraphPropertyEnum.COMPONENT_TYPE.getProperty());
	}

	@Test
	public void testGetProperty() throws Exception {
		GraphPropertyEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperty();
	}

	@Test
	public void testSetProperty() throws Exception {
		GraphPropertyEnum testSubject;
		String property = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setProperty(property);
	}

	@Test
	public void testGetClazz() throws Exception {
		GraphPropertyEnum testSubject;
		Class result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getClazz();
	}

	@Test
	public void testSetClazz() throws Exception {
		GraphPropertyEnum testSubject;
		Class clazz = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setClazz(clazz);
	}

	@Test
	public void testIsUnique() throws Exception {
		GraphPropertyEnum testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isUnique();
	}

	@Test
	public void testSetUnique() throws Exception {
		GraphPropertyEnum testSubject;
		boolean unique = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setUnique(unique);
	}

	@Test
	public void testIsIndexed() throws Exception {
		GraphPropertyEnum testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isIndexed();
	}

	@Test
	public void testSetIndexed() throws Exception {
		GraphPropertyEnum testSubject;
		boolean indexed = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIndexed(indexed);
	}

	@Test
	public void testGetAllProperties() throws Exception {
		List<String> result;

		// default test
		result = GraphPropertyEnum.getAllProperties();
	}
}
