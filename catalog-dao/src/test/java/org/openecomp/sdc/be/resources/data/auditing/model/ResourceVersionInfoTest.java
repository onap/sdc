package org.openecomp.sdc.be.resources.data.auditing.model;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo.Builder;


public class ResourceVersionInfoTest {

	private ResourceVersionInfo createTestSubject() {
		Builder newBuilder = ResourceVersionInfo.newBuilder();
		return newBuilder.build();
	}

	
	@Test
	public void testNewBuilder() throws Exception {
		Builder result;

		// default test
		result = ResourceVersionInfo.newBuilder();
	}

	@Test
	public void testArtifactUuid() throws Exception {
		Builder result;

		// default test
		result = ResourceVersionInfo.newBuilder();
		result.artifactUuid("mock");
	}
	
	@Test
	public void testState() throws Exception {
		Builder result;

		// default test
		result = ResourceVersionInfo.newBuilder();
		result.state("mock");
	}
	
	@Test
	public void testvVersion() throws Exception {
		Builder result;

		// default test
		result = ResourceVersionInfo.newBuilder();
		result.version("mock");
	}
	
	@Test
	public void testDistributionStatus() throws Exception {
		Builder result;

		// default test
		result = ResourceVersionInfo.newBuilder();
		result.distributionStatus("mock");
	}
	
	@Test
	public void testGetArtifactUuid() throws Exception {
		ResourceVersionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactUuid();
	}

	
	@Test
	public void testGetState() throws Exception {
		ResourceVersionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getState();
	}

	
	@Test
	public void testGetVersion() throws Exception {
		ResourceVersionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	
	@Test
	public void testGetDistributionStatus() throws Exception {
		ResourceVersionInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatus();
	}
}