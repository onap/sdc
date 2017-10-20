package org.openecomp.sdc.be.servlets;

import javax.annotation.Generated;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.DistributionMonitoringBusinessLogic;

public class DistributionServiceServletTest {

	private DistributionServiceServlet createTestSubject() {
		return new DistributionServiceServlet();
	}

	
	@Test
	public void testGetServiceById() throws Exception {
		DistributionServiceServlet testSubject;
		String serviceUUID = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetListOfDistributionStatuses() throws Exception {
		DistributionServiceServlet testSubject;
		String did = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testInit() throws Exception {
		DistributionServiceServlet testSubject;
		HttpServletRequest request = null;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetDistributionBL() throws Exception {
		DistributionServiceServlet testSubject;
		ServletContext context = null;
		DistributionMonitoringBusinessLogic result;

		// default test
		testSubject = createTestSubject();
	}
}