package org.openecomp.sdc.be.servlets;

import javax.annotation.Generated;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.resources.data.ESArtifactData;

import ch.qos.logback.classic.Logger;
import java.util.*;
import org.junit.Assert;


public class CsarBuildServletTest {

	private CsarBuildServlet createTestSubject() {
		return new CsarBuildServlet();
	}

	
	@Test
	public void testGetDefaultTemplate() throws Exception {
		CsarBuildServlet testSubject;
		HttpServletRequest request = null;
		String serviceName = "";
		String serviceVersion = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDefaultTemplate(request, serviceName, serviceVersion);
	}

	
	@Test
	public void testGetToscaCsarTemplate() throws Exception {
		CsarBuildServlet testSubject;
		HttpServletRequest request = null;
		String serviceName = "";
		String serviceVersion = "";
		Response result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getToscaCsarTemplate(request, serviceName, serviceVersion);
	}

	

}