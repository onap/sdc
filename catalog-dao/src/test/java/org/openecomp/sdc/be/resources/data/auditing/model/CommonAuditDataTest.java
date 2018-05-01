package org.openecomp.sdc.be.resources.data.auditing.model;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData.Builder;

@Generated(value = "org.junit-tools-1.0.6")
public class CommonAuditDataTest {

	private CommonAuditData createTestSubject() {
		Builder newBuilder = CommonAuditData.newBuilder();
		return newBuilder.build();
	}
	
	

	
	@Test
	public void testGetStatus() throws Exception {
		CommonAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testGetDescription() throws Exception {
		CommonAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testGetRequestId() throws Exception {
		CommonAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	
	@Test
	public void testGetServiceInstanceId() throws Exception {
		CommonAuditData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	
	@Test
	public void testSetServiceInstanceId() throws Exception {
		CommonAuditData testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	
	@Test
	public void testNewBuilder() throws Exception {
		Builder result;

		// default test
		result = CommonAuditData.newBuilder();
	}
}