package org.openecomp.sdc.asdctool.impl.validator.utils;

import org.junit.Test;

import java.util.List;
import java.util.Map;

public class VfModuleArtifactPayloadExTest {

	private VfModuleArtifactPayloadEx createTestSubject() {
		return new VfModuleArtifactPayloadEx();
	}

	@Test
	public void testGetVfModuleModelName() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfModuleModelName();
	}

	@Test
	public void testSetVfModuleModelName() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		String vfModuleModelName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVfModuleModelName(vfModuleModelName);
	}

	@Test
	public void testGetVfModuleModelInvariantUUID() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfModuleModelInvariantUUID();
	}

	@Test
	public void testSetVfModuleModelInvariantUUID() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		String vfModuleModelInvariantUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVfModuleModelInvariantUUID(vfModuleModelInvariantUUID);
	}

	@Test
	public void testGetVfModuleModelVersion() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfModuleModelVersion();
	}

	@Test
	public void testSetVfModuleModelVersion() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		String vfModuleModelVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVfModuleModelVersion(vfModuleModelVersion);
	}

	@Test
	public void testGetVfModuleModelUUID() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfModuleModelUUID();
	}

	@Test
	public void testSetVfModuleModelUUID() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		String vfModuleModelUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVfModuleModelUUID(vfModuleModelUUID);
	}

	@Test
	public void testGetVfModuleModelCustomizationUUID() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfModuleModelCustomizationUUID();
	}

	@Test
	public void testSetVfModuleModelCustomizationUUID() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		String vfModuleModelCustomizationUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVfModuleModelCustomizationUUID(vfModuleModelCustomizationUUID);
	}

	@Test
	public void testGetVfModuleModelDescription() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVfModuleModelDescription();
	}

	@Test
	public void testSetVfModuleModelDescription() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		String vfModuleModelDescription = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVfModuleModelDescription(vfModuleModelDescription);
	}

	@Test
	public void testGetIsBase() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsBase();
	}

	@Test
	public void testSetIsBase() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		Boolean isBase = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setIsBase(isBase);
	}

	@Test
	public void testGetArtifacts() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifacts();
	}

	@Test
	public void testSetArtifacts() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		List<String> artifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifacts(artifacts);
	}

	@Test
	public void testGetProperties() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProperties();
	}

	@Test
	public void testSetProperties() throws Exception {
		VfModuleArtifactPayloadEx testSubject;
		Map<String, Object> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setProperties(properties);
	}
}