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

package org.openecomp.sdc.be.ui.model;

import java.util.List;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;


public class UiResourceMetadataTest {

	private UiResourceMetadata createTestSubject() {
		return new UiResourceMetadata(null, null, new ResourceMetadataDataDefinition());
	}

	
	@Test
	public void testGetDerivedFrom() throws Exception {
		UiResourceMetadata testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDerivedFrom();
	}

	
	@Test
	public void testSetDerivedFrom() throws Exception {
		UiResourceMetadata testSubject;
		List<String> derivedFrom = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDerivedFrom(derivedFrom);
	}

	
	@Test
	public void testGetVendorName() throws Exception {
		UiResourceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVendorName();
	}

	
	@Test
	public void testSetVendorName() throws Exception {
		UiResourceMetadata testSubject;
		String vendorName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVendorName(vendorName);
	}

	
	@Test
	public void testGetVendorRelease() throws Exception {
		UiResourceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVendorRelease();
	}

	
	@Test
	public void testSetVendorRelease() throws Exception {
		UiResourceMetadata testSubject;
		String vendorRelease = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVendorRelease(vendorRelease);
	}

	
	@Test
	public void testGetResourceVendorModelNumber() throws Exception {
		UiResourceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceVendorModelNumber();
	}

	
	@Test
	public void testSetResourceVendorModelNumber() throws Exception {
		UiResourceMetadata testSubject;
		String resourceVendorModelNumber = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceVendorModelNumber(resourceVendorModelNumber);
	}

	
	@Test
	public void testGetResourceType() throws Exception {
		UiResourceMetadata testSubject;
		ResourceTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceType();
	}

	
	@Test
	public void testSetResourceType() throws Exception {
		UiResourceMetadata testSubject;
		ResourceTypeEnum resourceType = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceType(resourceType);
	}

	
	@Test
	public void testGetIsAbstract() throws Exception {
		UiResourceMetadata testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsAbstract();
	}

	
	@Test
	public void testSetIsAbstract() throws Exception {
		UiResourceMetadata testSubject;
		Boolean isAbstract = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIsAbstract(isAbstract);
	}

	
	@Test
	public void testGetCost() throws Exception {
		UiResourceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCost();
	}

	
	@Test
	public void testSetCost() throws Exception {
		UiResourceMetadata testSubject;
		String cost = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCost(cost);
	}

	
	@Test
	public void testGetLicenseType() throws Exception {
		UiResourceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLicenseType();
	}

	
	@Test
	public void testSetLicenseType() throws Exception {
		UiResourceMetadata testSubject;
		String licenseType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLicenseType(licenseType);
	}

	
	@Test
	public void testGetToscaResourceName() throws Exception {
		UiResourceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaResourceName();
	}

	
	@Test
	public void testSetToscaResourceName() throws Exception {
		UiResourceMetadata testSubject;
		String toscaResourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaResourceName(toscaResourceName);
	}
}
