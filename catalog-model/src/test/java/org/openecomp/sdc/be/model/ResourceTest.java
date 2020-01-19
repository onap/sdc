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

import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.unittests.utils.ModelConfDependentTest;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ResourceTest extends ModelConfDependentTest{

	private Resource createTestSubject() {
		return new Resource();
	}

	@Test
	public void testCtor() throws Exception {
		ComponentMetadataDefinition componentMetadataDefinition = new ResourceMetadataDefinition();
		new Resource(componentMetadataDefinition);
	}
	
	@Test
	public void testGetDerivedFrom() throws Exception {
		Resource testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerivedFrom();
	}

	
	@Test
	public void testSetDerivedFrom() throws Exception {
		Resource testSubject;
		List<String> derivedFrom = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDerivedFrom(derivedFrom);
	}

	
	@Test
	public void testGetDerivedList() throws Exception {
		Resource testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerivedList();
	}

	
	@Test
	public void testSetDerivedList() throws Exception {
		Resource testSubject;
		List<String> derivedList = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDerivedList(derivedList);
	}

	
	@Test
	public void testGetProperties() throws Exception {
		Resource testSubject;
		List<PropertyDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	
	@Test
	public void testSetProperties() throws Exception {
		Resource testSubject;
		List<PropertyDefinition> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}

	
	@Test
	public void testGetAttributes() throws Exception {
		Resource testSubject;
		List<PropertyDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAttributes();
	}

	
	@Test
	public void testSetAttributes() throws Exception {
		Resource testSubject;
		List<PropertyDefinition> attributes = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setAttributes(attributes);
	}

	
	@Test
	public void testGetInterfaces() throws Exception {
		Resource testSubject;
		Map<String, InterfaceDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getInterfaces();
	}

	
	@Test
	public void testSetInterfaces() throws Exception {
		Resource testSubject;
		Map<String, InterfaceDefinition> interfaces = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setInterfaces(interfaces);
	}

	
	@Test
	public void testIsAbstract() throws Exception {
		Resource testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isAbstract();
	}

	
	@Test
	public void testSetAbstract() throws Exception {
		Resource testSubject;
		Boolean isAbstract = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setAbstract(isAbstract);
	}

	
	@Test
	public void testGetDefaultCapabilities() throws Exception {
		Resource testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDefaultCapabilities();
	}

	
	@Test
	public void testSetDefaultCapabilities() throws Exception {
		Resource testSubject;
		List<String> defaultCapabilities = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDefaultCapabilities(defaultCapabilities);
	}

	
	@Test
	public void testGetCost() throws Exception {
		Resource testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCost();
	}

	
	@Test
	public void testSetCost() throws Exception {
		Resource testSubject;
		String cost = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCost(cost);
	}

	
	@Test
	public void testGetLicenseType() throws Exception {
		Resource testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLicenseType();
	}

	
	@Test
	public void testSetLicenseType() throws Exception {
		Resource testSubject;
		String licenseType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLicenseType(licenseType);
	}

	
	@Test
	public void testHashCode() throws Exception {
		Resource testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		Resource testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		obj = new Object();
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		
		Resource testSubject2 = createTestSubject();
		result = testSubject.equals(testSubject2);
		Assert.assertEquals(true, result);
	}

	
	@Test
	public void testToString() throws Exception {
		Resource testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testGetToscaResourceName() throws Exception {
		Resource testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaResourceName();
	}

	
	@Test
	public void testSetToscaResourceName() throws Exception {
		Resource testSubject;
		String toscaResourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaResourceName(toscaResourceName);
	}

	
	@Test
	public void testGetResourceType() throws Exception {
		Resource testSubject;
		ResourceTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceType();
	}

	
	@Test
	public void testSetResourceType() throws Exception {
		Resource testSubject;
		ResourceTypeEnum resourceType = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceType(resourceType);
	}

	
	@Test
	public void testSetVendorName() throws Exception {
		Resource testSubject;
		String vendorName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVendorName(vendorName);
	}

	
	@Test
	public void testSetVendorRelease() throws Exception {
		Resource testSubject;
		String vendorRelease = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVendorRelease(vendorRelease);
	}

	
	@Test
	public void testSetResourceVendorModelNumber() throws Exception {
		Resource testSubject;
		String resourceVendorModelNumber = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceVendorModelNumber(resourceVendorModelNumber);
	}

	
	@Test
	public void testGetVendorName() throws Exception {
		Resource testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVendorName();
	}

	
	@Test
	public void testGetVendorRelease() throws Exception {
		Resource testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVendorRelease();
	}

	
	@Test
	public void testGetResourceVendorModelNumber() throws Exception {
		Resource testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceVendorModelNumber();
	}

	@Test
	public void testFetchGenericTypeToscaNameFromConfig() throws Exception {
		Resource testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.fetchGenericTypeToscaNameFromConfig();
	}
	
	@Test
	public void testAssetType() throws Exception {
		Resource testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.assetType();
	}

	
	@Test
	public void testShouldGenerateInputs() throws Exception {
		Resource testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.shouldGenerateInputs();
	}

	
	@Test
	public void testDeriveFromGeneric() throws Exception {
		Resource testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.deriveFromGeneric();
	}

	@Test
	public void testGroupRelationsByInstanceName() throws Exception {
		Resource testSubject;
		Map<String, List<RequirementCapabilityRelDef>> result;

		// default test
		testSubject = createTestSubject();
		Resource resource = new Resource();
		resource.setComponentInstancesRelations(new LinkedList<RequirementCapabilityRelDef>());
		result = testSubject.groupRelationsFromCsarByInstanceName(resource);
	}

	@Test
	public void testGetInstanceNameFromInstanceId() throws Exception {
		Resource testSubject;
		Resource resource = new Resource();
		String instId = "mock";
		boolean result;

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "getInstanceNameFromInstanceId", resource, instId);
	}
}
