package org.openecomp.sdc.be.model;

import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ForwardingPathDataDefinition;

public class ServiceTest {

	private Service createTestSubject() {
		return new Service();
	}
	
	@Test
	public void testCtor() throws Exception {
		new Service(new ComponentMetadataDefinition());
	}
	
	@Test
	public void testGetServiceApiArtifacts() throws Exception {
		Service testSubject;
		Map<String, ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceApiArtifacts();
	}

	
	@Test
	public void testSetServiceApiArtifacts() throws Exception {
		Service testSubject;
		Map<String, ArtifactDefinition> serviceApiArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceApiArtifacts(serviceApiArtifacts);
	}

	
	@Test
	public void testGetProjectCode() throws Exception {
		Service testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getProjectCode();
	}

	
	@Test
	public void testGetForwardingPaths() throws Exception {
		Service testSubject;
		Map<String, ForwardingPathDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getForwardingPaths();
	}

	
	@Test
	public void testSetForwardingPaths() throws Exception {
		Service testSubject;
		Map<String, ForwardingPathDataDefinition> forwardingPaths = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setForwardingPaths(forwardingPaths);
	}

	
	@Test
	public void testAddForwardingPath() throws Exception {
		Service testSubject;
		ForwardingPathDataDefinition forwardingPathDataDefinition = new ForwardingPathDataDefinition();
		ForwardingPathDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.addForwardingPath(forwardingPathDataDefinition);
	}

	
	@Test
	public void testSetProjectCode() throws Exception {
		Service testSubject;
		String projectName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setProjectCode(projectName);
	}

	
	@Test
	public void testGetDistributionStatus() throws Exception {
		Service testSubject;
		DistributionStatusEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatus();
	}

	
	@Test
	public void testSetDistributionStatus() throws Exception {
		Service testSubject;
		DistributionStatusEnum distributionStatus = null;

		// test 1
		testSubject = createTestSubject();
		distributionStatus = null;
		testSubject.setDistributionStatus(distributionStatus);
		testSubject.setDistributionStatus(DistributionStatusEnum.DISTRIBUTED);
	}

	
	@Test
	public void testSetEcompGeneratedNaming() throws Exception {
		Service testSubject;
		Boolean ecompGeneratedNaming = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setEcompGeneratedNaming(ecompGeneratedNaming);
	}

	
	@Test
	public void testIsEcompGeneratedNaming() throws Exception {
		Service testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.isEcompGeneratedNaming();
	}

	
	@Test
	public void testSetNamingPolicy() throws Exception {
		Service testSubject;
		String namingPolicy = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setNamingPolicy(namingPolicy);
	}

	
	@Test
	public void testGetNamingPolicy() throws Exception {
		Service testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getNamingPolicy();
	}

	
	@Test
	public void testGetEnvironmentContext() throws Exception {
		Service testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEnvironmentContext();
	}

	
	@Test
	public void testSetEnvironmentContext() throws Exception {
		Service testSubject;
		String environmentContext = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEnvironmentContext(environmentContext);
	}

	
	@Test
	public void testSetServiceType() throws Exception {
		Service testSubject;
		String serviceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceType(serviceType);
	}

	
	@Test
	public void testGetServiceType() throws Exception {
		Service testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceType();
	}

	
	@Test
	public void testSetServiceRole() throws Exception {
		Service testSubject;
		String serviceRole = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceRole(serviceRole);
	}

	
	@Test
	public void testGetServiceRole() throws Exception {
		Service testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceRole();
	}

	


	
	@Test
	public void testToString() throws Exception {
		Service testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testSetSpecificComponetTypeArtifacts() throws Exception {
		Service testSubject;
		Map<String, ArtifactDefinition> specificComponentTypeArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setSpecificComponetTypeArtifacts(specificComponentTypeArtifacts);
	}
}