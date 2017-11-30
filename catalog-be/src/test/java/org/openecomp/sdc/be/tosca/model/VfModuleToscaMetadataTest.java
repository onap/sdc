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