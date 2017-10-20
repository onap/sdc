package org.openecomp.sdc.be.servlets;

import javax.annotation.Generated;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;

public class RequirementsServletTest {

	private RequirementsServlet createTestSubject() {
		return new RequirementsServlet();
	}

	
	@Test
	public void testUpdateRequirement() throws Exception {
		RequirementsServlet testSubject;
		String resourceId = "";
		String requirementId = "";
		String requirementData = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}
}