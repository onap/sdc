package org.openecomp.sdc.be.resources.data.auditing.model;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData.Builder;

public class ResourceAuditDataTest {

	private ResourceAuditData createTestSubject() {
		Builder newBuilder = ResourceAuditData.newBuilder();
		return newBuilder.build();
	}

	
	@Test
	public void testNewBuilder() throws Exception {
		Builder result;

		// default test
		result = ResourceAuditData.newBuilder();
	}

	@Test
	public void testArtifactUuid() throws Exception {
		Builder result;

		// default test
		result = ResourceAuditData.newBuilder();
		result.artifactUuid("mock");
	}
	
	@Test
	public void testState() throws Exception {
		Builder result;

		// default test
		result = ResourceAuditData.newBuilder();
		result.state("mock");
	}
	
	@Test
	public void testvVersion() throws Exception {
		Builder result;

		// default test
		result = ResourceAuditData.newBuilder();
		result.version("mock");
	}
	
	@Test
	public void testDistributionStatus() throws Exception {
		Builder result;

		// default test
		result = ResourceAuditData.newBuilder();
		result.distributionStatus("mock");
	}
	
	@Test
	public void testGetArtifactUuid() throws Exception {
		ResourceAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactUuid();
	}

	
	@Test
	public void testGetState() throws Exception {
		ResourceAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getState();
	}

	
	@Test
	public void testGetVersion() throws Exception {
		ResourceAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	
	@Test
	public void testGetDistributionStatus() throws Exception {
		ResourceAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatus();
	}
}