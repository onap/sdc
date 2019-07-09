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

import java.util.List;
import java.util.Map;

import org.junit.Test;


public class NodeTypeInfoTest {

	private NodeTypeInfo createTestSubject() {
		return new NodeTypeInfo();
	}

	
	@Test
	public void testGetUnmarkedCopy() throws Exception {
		NodeTypeInfo testSubject;
		NodeTypeInfo result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUnmarkedCopy();
	}

	
	@Test
	public void testGetType() throws Exception {
		NodeTypeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		NodeTypeInfo testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetTemplateFileName() throws Exception {
		NodeTypeInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTemplateFileName();
	}

	
	@Test
	public void testSetTemplateFileName() throws Exception {
		NodeTypeInfo testSubject;
		String templateFileName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTemplateFileName(templateFileName);
	}

	
	@Test
	public void testGetDerivedFrom() throws Exception {
		NodeTypeInfo testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerivedFrom();
	}

	
	@Test
	public void testSetDerivedFrom() throws Exception {
		NodeTypeInfo testSubject;
		List<String> derivedFrom = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDerivedFrom(derivedFrom);
	}

	
	@Test
	public void testIsNested() throws Exception {
		NodeTypeInfo testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isNested();
	}

	
	@Test
	public void testSetNested() throws Exception {
		NodeTypeInfo testSubject;
		boolean isNested = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setNested(isNested);
	}

	
	@Test
	public void testGetMappedToscaTemplate() throws Exception {
		NodeTypeInfo testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMappedToscaTemplate();
	}

	
	@Test
	public void testSetMappedToscaTemplate() throws Exception {
		NodeTypeInfo testSubject;
		Map<String, Object> mappedToscaTemplate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMappedToscaTemplate(mappedToscaTemplate);
	}
}
