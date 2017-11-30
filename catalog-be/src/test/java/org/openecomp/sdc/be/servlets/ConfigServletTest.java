package org.openecomp.sdc.be.servlets;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

public class ConfigServletTest {

	private ConfigServlet createTestSubject() {
		return new ConfigServlet();
	}

	
	@Test
	public void testGetConfig() throws Exception {
		ConfigServlet testSubject;
		HttpServletRequest request = null;
		String result;

		// default test
		testSubject = createTestSubject();
	}
}