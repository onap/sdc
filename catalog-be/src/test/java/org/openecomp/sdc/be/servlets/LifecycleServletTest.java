package org.openecomp.sdc.be.servlets;

import javax.annotation.Generated;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.model.LifeCycleTransitionEnum;
import org.openecomp.sdc.be.model.User;

import fj.data.Either;

public class LifecycleServletTest {

	private LifecycleServlet createTestSubject() {
		return new LifecycleServlet();
	}

	
	@Test
	public void testChangeResourceState() throws Exception {
		LifecycleServlet testSubject;
		String jsonChangeInfo = "";
		String componentCollection = "";
		String lifecycleTransition = "";
		String componentId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testValidateTransitionEnum() throws Exception {
		LifecycleServlet testSubject;
		String lifecycleTransition = "";
		User user = null;
		Either<LifeCycleTransitionEnum, Response> result;

		// default test
		testSubject = createTestSubject();
	}
}