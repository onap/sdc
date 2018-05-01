package org.openecomp.sdc.be.resources.data.auditing.model;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData.Builder;

@Generated(value = "org.junit-tools-1.0.6")
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