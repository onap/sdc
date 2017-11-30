package org.openecomp.sdc.be.ui.model;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;


public class UiServiceMetadataTest {

	private UiServiceMetadata createTestSubject() {
		return new UiServiceMetadata(null, new ServiceMetadataDataDefinition());
	}

	
	@Test
	public void testGetDistributionStatus() throws Exception {
		UiServiceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatus();
	}

	
	@Test
	public void testSetDistributionStatus() throws Exception {
		UiServiceMetadata testSubject;
		String distributionStatus = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionStatus(distributionStatus);
	}

	
	@Test
	public void testGetEcompGeneratedNaming() throws Exception {
		UiServiceMetadata testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEcompGeneratedNaming();
	}

	
	@Test
	public void testSetEcompGeneratedNaming() throws Exception {
		UiServiceMetadata testSubject;
		Boolean ecompGeneratedNaming = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setEcompGeneratedNaming(ecompGeneratedNaming);
	}

	
	@Test
	public void testGetNamingPolicy() throws Exception {
		UiServiceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNamingPolicy();
	}

	
	@Test
	public void testSetNamingPolicy() throws Exception {
		UiServiceMetadata testSubject;
		String namingPolicy = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNamingPolicy(namingPolicy);
	}

	
	@Test
	public void testGetServiceType() throws Exception {
		UiServiceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceType();
	}

	
	@Test
	public void testSetServiceType() throws Exception {
		UiServiceMetadata testSubject;
		String serviceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceType(serviceType);
	}

	
	@Test
	public void testGetServiceRole() throws Exception {
		UiServiceMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceRole();
	}

	
	@Test
	public void testSetServiceRole() throws Exception {
		UiServiceMetadata testSubject;
		String serviceRole = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceRole(serviceRole);
	}
}