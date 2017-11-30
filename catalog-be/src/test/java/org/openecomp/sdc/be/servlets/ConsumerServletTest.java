package org.openecomp.sdc.be.servlets;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.openecomp.sdc.be.components.impl.ConsumerBusinessLogic;
import org.openecomp.sdc.be.model.ConsumerDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

public class ConsumerServletTest {

	private ConsumerServlet createTestSubject() {
		return new ConsumerServlet();
	}

	
	@Test
	public void testCreateConsumer() throws Exception {
		ConsumerServlet testSubject;
		String data = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetConsumer() throws Exception {
		ConsumerServlet testSubject;
		String consumerId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testDeleteConsumer() throws Exception {
		ConsumerServlet testSubject;
		String consumerId = "";
		HttpServletRequest request = null;
		String userId = "";
		Response result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testGetConsumerBL() throws Exception {
		ConsumerServlet testSubject;
		ServletContext context = null;
		ConsumerBusinessLogic result;

		// default test
		testSubject = createTestSubject();
	}

	
	@Test
	public void testConvertJsonToObject() throws Exception {
		ConsumerServlet testSubject;
		String data = "";
		User user = null;
		AuditingActionEnum actionEnum = null;
		Either<ConsumerDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}
}