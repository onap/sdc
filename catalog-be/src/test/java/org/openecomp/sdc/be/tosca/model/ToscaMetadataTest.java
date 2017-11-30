package org.openecomp.sdc.be.tosca.model;

import org.junit.Test;


public class ToscaMetadataTest {

	private ToscaMetadata createTestSubject() {
		return new ToscaMetadata();
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