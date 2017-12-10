package org.openecomp.sdc.be.datatypes.components;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;


public class ResourceMetadataDataDefinitionTest {

	private ResourceMetadataDataDefinition createTestSubject() {
		return new ResourceMetadataDataDefinition();
	}

	
	@Test
	public void testGetVendorName() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVendorName();
	}

	
	@Test
	public void testSetVendorName() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String vendorName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVendorName(vendorName);
	}

	
	@Test
	public void testGetVendorRelease() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVendorRelease();
	}

	
	@Test
	public void testSetVendorRelease() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String vendorRelease = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVendorRelease(vendorRelease);
	}

	
	@Test
	public void testGetResourceVendorModelNumber() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceVendorModelNumber();
	}

	
	@Test
	public void testSetResourceVendorModelNumber() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String resourceVendorModelNumber = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceVendorModelNumber(resourceVendorModelNumber);
	}

	
	@Test
	public void testGetResourceType() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		ResourceTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceType();
	}

	
	@Test
	public void testSetResourceType() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		ResourceTypeEnum resourceType = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceType(resourceType);
	}

	
	@Test
	public void testIsAbstract() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isAbstract();
	}

	
	@Test
	public void testSetAbstract() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		Boolean isAbstract = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setAbstract(isAbstract);
	}

	
	@Test
	public void testGetCost() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCost();
	}

	
	@Test
	public void testSetCost() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String cost = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCost(cost);
	}

	
	@Test
	public void testGetLicenseType() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLicenseType();
	}

	
	@Test
	public void testSetLicenseType() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String licenseType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLicenseType(licenseType);
	}

	
	@Test
	public void testGetToscaResourceName() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaResourceName();
	}

	
	@Test
	public void testSetToscaResourceName() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String toscaResourceName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setToscaResourceName(toscaResourceName);
	}

	
	@Test
	public void testToString() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testHashCode() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		ResourceMetadataDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.equals(obj);
	}
}