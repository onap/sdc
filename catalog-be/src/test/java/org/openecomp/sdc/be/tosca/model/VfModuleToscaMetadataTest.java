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

package org.openecomp.sdc.be.tosca.model;

import org.junit.Test;


public class VfModuleToscaMetadataTest {

	private VfModuleToscaMetadata createTestSubject() {
		return new VfModuleToscaMetadata();
	}

	
	@Test
	public void testSetName() throws Exception {
		VfModuleToscaMetadata testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testSetInvariantUUID() throws Exception {
		VfModuleToscaMetadata testSubject;
		String invariantUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInvariantUUID(invariantUUID);
	}

	
	@Test
	public void testSetUUID() throws Exception {
		VfModuleToscaMetadata testSubject;
		String uUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUUID(uUID);
	}

	
	@Test
	public void testSetVersion() throws Exception {
		VfModuleToscaMetadata testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	
	@Test
	public void testGetVfModuleModelName() throws Exception {
		VfModuleToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfModuleModelName();
	}

	
	@Test
	public void testGetVfModuleModelInvariantUUID() throws Exception {
		VfModuleToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfModuleModelInvariantUUID();
	}

	
	@Test
	public void testGetVfModuleModelUUID() throws Exception {
		VfModuleToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfModuleModelUUID();
	}

	
	@Test
	public void testGetVfModuleModelVersion() throws Exception {
		VfModuleToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfModuleModelVersion();
	}

	
	@Test
	public void testGetVfModuleModelCustomizationUUID() throws Exception {
		VfModuleToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfModuleModelCustomizationUUID();
	}

	
	@Test
	public void testSetCustomizationUUID() throws Exception {
		VfModuleToscaMetadata testSubject;
		String customizationUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCustomizationUUID(customizationUUID);
	}
}
