package org.openecomp.sdc.be.servlets;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;

public class TypesFetchServletTest {

	private TypesFetchServlet createTestSubject() {
		return new TypesFetchServlet();
	}

	
	@Test
	public void testGetAllDataTypesServlet() throws Exception {
		TypesFetchServlet testSubject;
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetPropertyBL() throws Exception {
		TypesFetchServlet testSubject;
		ServletContext context = null;
		PropertyBusinessLogic result;

		// default test
		testSubject = createTestSubject();
	}
}