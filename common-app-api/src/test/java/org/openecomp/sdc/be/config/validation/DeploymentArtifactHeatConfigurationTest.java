package org.openecomp.sdc.be.config.validation;

import java.util.Map;

import org.junit.Test;


public class DeploymentArtifactHeatConfigurationTest {

	private DeploymentArtifactHeatConfiguration createTestSubject() {
		return new DeploymentArtifactHeatConfiguration();
	}

	
	@Test
	public void testGetHeat_template_version() throws Exception {
		DeploymentArtifactHeatConfiguration testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHeat_template_version();
	}

	
	@Test
	public void testSetHeat_template_version() throws Exception {
		DeploymentArtifactHeatConfiguration testSubject;
		String heat_template_version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setHeat_template_version(heat_template_version);
	}

	
	@Test
	public void testGetResources() throws Exception {
		DeploymentArtifactHeatConfiguration testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResources();
	}

	
	@Test
	public void testSetResources() throws Exception {
		DeploymentArtifactHeatConfiguration testSubject;
		Map<String, Object> resources = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setResources(resources);
	}
}