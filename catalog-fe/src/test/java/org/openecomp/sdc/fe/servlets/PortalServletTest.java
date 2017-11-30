package org.openecomp.sdc.fe.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;


public class PortalServletTest {

	private PortalServlet createTestSubject() {
		return new PortalServlet();
	}

	
	@Test
	public void testDoGet() throws Exception {
		PortalServlet testSubject;
		HttpServletRequest request = null;
		HttpServletResponse response = null;

		// default test
		testSubject = createTestSubject();
		testSubject.doGet(request, response);
	}

	
	
}