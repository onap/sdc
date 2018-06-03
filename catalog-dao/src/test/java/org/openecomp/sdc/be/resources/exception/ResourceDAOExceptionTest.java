package org.openecomp.sdc.be.resources.exception;

import org.junit.Test;
import org.openecomp.sdc.be.dao.api.ResourceUploadStatus;


public class ResourceDAOExceptionTest {

	private ResourceDAOException createTestSubject() {
		return new ResourceDAOException("", null);
	}

	@Test
	public void testCtor() throws Exception {
		new ResourceDAOException("mock");
		new ResourceDAOException("mock", new Throwable());
		new ResourceDAOException(ResourceUploadStatus.ALREADY_EXIST, "mock");
		new ResourceDAOException(ResourceUploadStatus.ALREADY_EXIST, "mock", new Throwable());
	}
	
	@Test
	public void testGetStatus() throws Exception {
		ResourceDAOException testSubject;
		ResourceUploadStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		ResourceDAOException testSubject;
		ResourceUploadStatus status = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}
}