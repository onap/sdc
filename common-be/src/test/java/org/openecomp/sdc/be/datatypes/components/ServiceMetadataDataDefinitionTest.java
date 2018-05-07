package org.openecomp.sdc.be.datatypes.components;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;


public class ServiceMetadataDataDefinitionTest {

	private ServiceMetadataDataDefinition createTestSubject() {
		return new ServiceMetadataDataDefinition();
	}

	@Test
	public void testCopyConstructor() throws Exception {
		ServiceMetadataDataDefinition testSubject;

		// default test
		testSubject = createTestSubject();
		ServiceMetadataDataDefinition serviceMetadataDataDefinition = new ServiceMetadataDataDefinition(testSubject);
	}
	
	@Test
	public void testGetDistributionStatus() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatus();
	}

	
	@Test
	public void testSetDistributionStatus() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		String distributionStatus = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionStatus(distributionStatus);
	}

	
	@Test
	public void testGetServiceType() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceType();
	}

	
	@Test
	public void testSetServiceType() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		String serviceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceType(serviceType);
	}

	
	@Test
	public void testGetServiceRole() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceRole();
	}

	
	@Test
	public void testSetServiceRole() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		String serviceRole = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceRole(serviceRole);
	}

	
	@Test
	public void testIsEcompGeneratedNaming() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isEcompGeneratedNaming();
	}

	
	@Test
	public void testSetEcompGeneratedNaming() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		Boolean ecompGeneratedNaming = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setEcompGeneratedNaming(ecompGeneratedNaming);
	}

	
	@Test
	public void testGetNamingPolicy() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNamingPolicy();
	}

	
	@Test
	public void testSetNamingPolicy() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		String namingPolicy = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNamingPolicy(namingPolicy);
	}

	
	@Test
	public void testToString() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testHashCode() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testgetActualComponentType() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getActualComponentType();
		testSubject.setComponentType(ComponentTypeEnum.PRODUCT);
		result = testSubject.getActualComponentType();
	}
	
	@Test
	public void testEquals() throws Exception {
		ServiceMetadataDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(new ServiceMetadataDataDefinition());
		Assert.assertEquals(true, result);
	}
}