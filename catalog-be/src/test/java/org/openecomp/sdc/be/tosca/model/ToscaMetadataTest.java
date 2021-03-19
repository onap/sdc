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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class ToscaMetadataTest {

	private ToscaMetadata createTestSubject() {
		return new ToscaMetadata();
	}


	@Test
	public void testServiceFunction() {
		ToscaMetadata testSubject = createTestSubject();
		testSubject.setServiceFunction("testServiceFunction");
		assertEquals("testServiceFunction", testSubject.getServiceFunction());
	}

	@Test
	public void testInstantiationType() {
		ToscaMetadata testSubject = createTestSubject();
		testSubject.setInstantiationType("testInstantiationType");
		assertEquals("testInstantiationType", testSubject.getInstantiationType());
	}

	@Test
	public void testEcompGeneratedNaming() {
		ToscaMetadata testSubject = createTestSubject();
		testSubject.setEcompGeneratedNaming(null);
		assertEquals(false, testSubject.isEcompGeneratedNaming());
		testSubject.setEcompGeneratedNaming(true);
		assertEquals(true, testSubject.isEcompGeneratedNaming());
	}

	@Test
	public void testServiceEcompNaming() {
		ToscaMetadata testSubject = createTestSubject();
		testSubject.setServiceEcompNaming(null);
		assertEquals(false, testSubject.getServiceEcompNaming());
		testSubject.setServiceEcompNaming(false);
		assertEquals(false, testSubject.getServiceEcompNaming());
	}

	@Test
	public void testSourceModelInvariant() {
		ToscaMetadata testSubject = createTestSubject();
		testSubject.setSourceModelInvariant("sourceModelInvariant");
		assertEquals("sourceModelInvariant", testSubject.getSourceModelInvariant());
	}

	@Test
	public void testSourceModelName() {
		ToscaMetadata testSubject = createTestSubject();
		testSubject.setSourceModelName("sourceModelName");
		assertEquals("sourceModelName", testSubject.getSourceModelName());
	}

	@Test
	public void testSourceModelUuid() {
		ToscaMetadata testSubject = createTestSubject();
		testSubject.setSourceModelUuid("sourceModelUuid");
		assertEquals("sourceModelUuid", testSubject.getSourceModelUuid());
	}

	@Test
	public void testEnvironmentContext() {
		ToscaMetadata testSubject = createTestSubject();
		testSubject.setEnvironmentContext("environmentContext");
		assertEquals("environmentContext", testSubject.getEnvironmentContext());
	}

	@Test
	public void testGetName() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		ToscaMetadata testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetInvariantUUID() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInvariantUUID();
	}

	
	@Test
	public void testSetInvariantUUID() throws Exception {
		ToscaMetadata testSubject;
		String invariantUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setInvariantUUID(invariantUUID);
	}

	
	@Test
	public void testGetUUID() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUUID();
	}

	
	@Test
	public void testSetUUID() throws Exception {
		ToscaMetadata testSubject;
		String uUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUUID(uUID);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		ToscaMetadata testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetType() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		ToscaMetadata testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetCategory() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategory();
	}

	
	@Test
	public void testSetCategory() throws Exception {
		ToscaMetadata testSubject;
		String category = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCategory(category);
	}

	
	@Test
	public void testGetSubcategory() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubcategory();
	}

	
	@Test
	public void testSetSubcategory() throws Exception {
		ToscaMetadata testSubject;
		String subcategory = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSubcategory(subcategory);
	}

	
	@Test
	public void testGetResourceVendor() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceVendor();
	}

	
	@Test
	public void testSetResourceVendor() throws Exception {
		ToscaMetadata testSubject;
		String resourceVendor = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceVendor(resourceVendor);
	}

	
	@Test
	public void testGetResourceVendorRelease() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceVendorRelease();
	}

	
	@Test
	public void testSetResourceVendorRelease() throws Exception {
		ToscaMetadata testSubject;
		String resourceVendorRelease = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceVendorRelease(resourceVendorRelease);
	}

	
	@Test
	public void testGetResourceVendorModelNumber() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceVendorModelNumber();
	}

	
	@Test
	public void testSetResourceVendorModelNumber() throws Exception {
		ToscaMetadata testSubject;
		String resourceVendorModelNumber = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceVendorModelNumber(resourceVendorModelNumber);
	}

	
	@Test
	public void testGetServiceType() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceType();
	}

	
	@Test
	public void testSetServiceType() throws Exception {
		ToscaMetadata testSubject;
		String serviceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceType(serviceType);
	}

	
	@Test
	public void testGetServiceRole() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceRole();
	}

	
	@Test
	public void testSetServiceRole() throws Exception {
		ToscaMetadata testSubject;
		String serviceRole = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceRole(serviceRole);
	}

	
	@Test
	public void testIsEcompGeneratedNaming() throws Exception {
		ToscaMetadata testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isEcompGeneratedNaming();
	}

	
	@Test
	public void testSetEcompGeneratedNaming() throws Exception {
		ToscaMetadata testSubject;
		Boolean ecompGeneratedNaming = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setEcompGeneratedNaming(ecompGeneratedNaming);
	}

	
	@Test
	public void testIsNamingPolicy() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isNamingPolicy();
	}

	
	@Test
	public void testSetNamingPolicy() throws Exception {
		ToscaMetadata testSubject;
		String namingPolicy = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNamingPolicy(namingPolicy);
	}

	
	@Test
	public void testGetServiceEcompNaming() throws Exception {
		ToscaMetadata testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceEcompNaming();
	}

	
	@Test
	public void testSetServiceEcompNaming() throws Exception {
		ToscaMetadata testSubject;
		Boolean serviceEcompNaming = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceEcompNaming(serviceEcompNaming);
	}

	
	@Test
	public void testGetVersion() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	
	@Test
	public void testSetVersion() throws Exception {
		ToscaMetadata testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	
	@Test
	public void testGetCustomizationUUID() throws Exception {
		ToscaMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCustomizationUUID();
	}

	
	@Test
	public void testSetCustomizationUUID() throws Exception {
		ToscaMetadata testSubject;
		String customizationUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCustomizationUUID(customizationUUID);
	}
}
